package lingyang.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lingyang.BroadCastMessage;
import lingyang.BroadCastProber;
import lingyang.NoSession;
import lingyang.Service;
import lingyang.ServiceStatistic;
import lingyang.Session;
import lingyang.Factory.SessionFactory;
import lingyang.configure.Configure;
import lingyang.err.ChannelException;
import lingyang.err.ExceptionEvent;
import lingyang.err.InitException;

public class FlowService implements Service {
	ExecutorService executor = Executors.newCachedThreadPool();
	Selector lisenSelector = null;
	Selector[] selectors = null;
	Configure configure = null;
	ExecutorService pool = null;
	HashSet<Session> sessions = new HashSet<Session>();
	ArrayList<ConcurrentLinkedQueue<Session>> newSessions = null;
	ArrayList<ConcurrentLinkedQueue<Session>> cancelSessions = null;
	ArrayList<ConcurrentLinkedQueue<NoSession>> noSessions = null;

	// ArrayList<ConcurrentLinkedQueue<Session>> connectingSessions = null;
	boolean _run=true;
	private ServiceStatistic serviceStatistic;
	public FlowService() {
		newSessions = new ArrayList<ConcurrentLinkedQueue<Session>>();
		cancelSessions = new ArrayList<ConcurrentLinkedQueue<Session>>();
		noSessions = new ArrayList<ConcurrentLinkedQueue<NoSession>>();
		serviceStatistic=new ServiceStatistic();
		serviceStatistic.startTime=System.currentTimeMillis();
	}

	public void connectTo(String address, int port) throws ChannelException {
		SocketAddress addr = new InetSocketAddress(address, port);
		connectTo(addr);
	}

	@Override
	public void connectTo(SocketAddress addr) throws ChannelException {
		serviceStatistic.connected++;
		executor.execute(new Connector(addr));
	}

	public void listenAt(int port) throws IOException {
		if (lisenSelector == null) {
			lisenSelector = Selector.open();
		}
		executor.execute(new ListenWorker(port));
	}

	public void setConfigure(Configure configure) throws InitException {
		this.configure = configure;
		try {
			init();
		} catch (IOException e) {
			throw new InitException();
		}
	}

	private void init() throws IOException {
		int i = configure.getSelectorNum();
		selectors = new Selector[i];
		for (int j = 0; j < i; ++j) {
			newSessions.add(new ConcurrentLinkedQueue<Session>());
			cancelSessions.add(new ConcurrentLinkedQueue<Session>());
			noSessions.add(new ConcurrentLinkedQueue<NoSession>());
		}
		for (i--; i >= 0; i--) {
			selectors[i] = Selector.open();
			executor.execute(new Worker(i));
		}
		if (configure.usePool()) {
			pool = Executors.newCachedThreadPool();
		}
		if (configure.getSchedule() != null) {
			executor.execute(new Scheduler(this, configure.getSchedule(),
					configure.getScheduleTimeOut()));
		}
	}

	@Override
	public void wakeUp(int index) {
		if (selectors == null)
			return;
		if (selectors.length <= index)
			return;
		selectors[index].wakeup();
	}

	@Override
	public void addSession(Session session) {
		synchronized (sessions) {
			serviceStatistic.totalSessions++;
			sessions.add(session);
		}
	}

	@Override
	public boolean removeSession(Session session) {
		synchronized (sessions) {
			return sessions.remove(session);
		}
	}

	@Override
	public void broadCast(BroadCastMessage bmessage, BroadCastProber prober) {
		synchronized (sessions) {
			for (Session session : sessions) {
				if (prober.probe(session)) {
					session.write(bmessage.getMessage());
				}
			}
		}
	}

	@Override
	public Set<Session> snapshotSessions() {
		Set<Session> snapshot = new HashSet<Session>();
		synchronized (sessions) {
			for (Session session : sessions) {
				snapshot.add(session);
			}
		}
		return snapshot;
	}

	protected void addNewSession(Session session, int index) {
		ConcurrentLinkedQueue<Session> queue = newSessions.get(index);
		boolean wake = false;
		if (queue.size() == 0) {
			wake = true;
		}
		queue.add(session);
		if (wake) {
			wakeUp(index);
		}
	}

	public void closeSession(Session session, int index) {
		ConcurrentLinkedQueue<Session> queue = cancelSessions.get(index);
		boolean wake = false;
		if (queue.size() == 0) {
			wake = true;
		}
		queue.add(session);
		if (wake) {
			wakeUp(index);
		}
	}
	@Override
	public void _addReadBytes(long readBytes) {
		serviceStatistic.totalReciveBytes+=readBytes;
	}

	@Override
	public void _addWriteBytes(long writeBytes) {
		serviceStatistic.totalSendBytes+=writeBytes;
	}

	@Override
	public ServiceStatistic shutDown() {
		_run=false;
		try {
			Thread.sleep(50);
		} catch (InterruptedException e1) {
		}
		if(lisenSelector!=null&&lisenSelector.isOpen()){
			try {
				lisenSelector.close();
			} catch (IOException e) {
			}
		}
		if(sessions!=null){			
			for(Session session:sessions){
				try {
					session._close();
				} catch (ChannelException e) {
				}
			}
		}
		if(selectors!=null){		
			for(Selector selector:selectors){
				if(selector!=null&&selector.isOpen()){
					try {
						selector.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(pool!=null){
			pool.shutdown();
		}
		if(executor!=null){
			executor.shutdown();
		}
		return serviceStatistic;
	}
	class ListenWorker implements Runnable {
		private int port;

		public ListenWorker(int port) {
			this.port = port;
		}

		public void run() {
			ServerSocketChannel serverSocketChannel;
			try {
				serverSocketChannel = ServerSocketChannel.open();
				InetSocketAddress address = new InetSocketAddress(InetAddress
						.getLocalHost(), port);
				serverSocketChannel.socket().bind(address);
				serverSocketChannel.socket().setReuseAddress(true);
				serverSocketChannel.configureBlocking(false);
				serverSocketChannel.register(lisenSelector,
						SelectionKey.OP_ACCEPT, null);
				while (_run) {
					lisenSelector.select();
					Iterator<SelectionKey> it = lisenSelector.selectedKeys()
							.iterator();
					while (it.hasNext()) {
						serviceStatistic.accepted++;
						SelectionKey key = it.next();
						it.remove();
						SocketChannel channel = (((ServerSocketChannel) key
								.channel()).accept());
						Session session = SessionFactory.newSession();
						session.setChannel(channel);
						session.setConfigure(configure);
						session.register(FlowService.this);
						channel.configureBlocking(false);
						channel.socket().setTcpNoDelay(true);
						int pid = configure.getStrategy().getIndex(selectors);
						session.setProcessorId(pid);
						configure.getProcessor().onConnect(session);
						addNewSession(session, pid);
					}
				}
			} catch (IOException e) {
			}

		}
	}

	class Connector implements Runnable {
		SocketChannel channel;
		SocketAddress address;

		public Connector(SocketAddress address) throws ChannelException {
			try {
				serviceStatistic.connected++;
				channel = SocketChannel.open();
				this.address = address;
			} catch (IOException e) {
				throw new ChannelException();
			}
		}

		@Override
		public void run() {
			int pid;
			pid = configure.getStrategy().getIndex(selectors);
			try {
				// block call
				channel.configureBlocking(true);
				channel.connect(address);
				channel.socket().setTcpNoDelay(true);
				channel.configureBlocking(false);
				Session session = SessionFactory.newSession();
				session.setChannel(channel);
				session.setConfigure(configure);
				session.setProcessorId(pid);
				session.register(FlowService.this);
				FlowService.this.addNewSession(session, pid);
			} catch (SocketException e) {
				noSessions.get(pid).add(new ExceptionEvent(address, e));
			} catch (IOException e) {
				noSessions.get(pid).add(new ExceptionEvent(address, e));
			}
		}

	}

	class Worker implements Runnable {
		int index = 0;

		public Worker(int index) {
			this.index = index;
		}

		protected void processNewSessions() {
			do {
				Session session = newSessions.get(index).poll();
				if (session == null) {
					break;
				}
				Selector choised = selectors[index];
				try {
					session.register(choised, SelectionKey.OP_READ
							| SelectionKey.OP_WRITE);
				} catch (ChannelException e) {
				}
			} while (true);
		}
		/**
		 * 
		 */
		protected void processClosedSessions() {
			do {
				Session session = cancelSessions.get(index).poll();
				if (session == null)
					break;
				FlowService.this.removeSession(session);
				try {
					session._close();
					configure.getProcessor().onClose(session);
				} catch (ChannelException e) {
					
				}
			} while (true);
		}

		protected void processNoSessions() {
			do {
				NoSession noSession = noSessions.get(index).poll();
				if (noSession == null)
					break;
				configure.getProcessor().onNoSession(null, noSession);

			} while (true);
		}

		public void run() {
			long timeSpan1 = configure.getReceiveIdle();
			long timeSpan2 = configure.getWriteIdle();
			long timeSpan = timeSpan1 - timeSpan2;
			timeSpan = Math.min(timeSpan2, Math.abs(timeSpan));
			do {
				processNewSessions();
				processClosedSessions();
				try {
					if (timeSpan > 0) {
						selectors[index].select(timeSpan);
					} else {
						selectors[index].select();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				Iterator<SelectionKey> keys = selectors[index].selectedKeys()
						.iterator();
				while (keys.hasNext()) {
					SelectionKey key = keys.next();
					keys.remove();
					Session session = (Session) key.attachment();
					if (key.isConnectable()) {
						configure.getProcessor().onConnect(session);
					}
					if (key.isReadable()) {
						configure.getProcessor().onRecive(session);
					}
					if (key.isWritable()) {
						configure.getProcessor().onWriteble(session);
					}
				}

				if (timeSpan > 0) {
					Set<SelectionKey> allKeys = selectors[index].keys();
					for (SelectionKey key : allKeys) {
						Session session = (Session) key.attachment();
						if (configure.usePool()) {
						} else {
							long lr = session.getLastReadTime();
							long rspan = configure.getReceiveIdle();
							long lw = session.getLastWriteTime();
							long wspan = configure.getWriteIdle();
							long current = System.currentTimeMillis();
							if (current - lr >= rspan || current - lw > wspan) {
								configure.getProcessor().onIdle(session);
							}
						}
					}
				}

			} while (_run);
		}
	}
}
