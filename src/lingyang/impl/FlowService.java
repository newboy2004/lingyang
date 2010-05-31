package lingyang.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lingyang.Event;
import lingyang.Service;
import lingyang.Session;
import lingyang.Factory.SessionFactory;
import lingyang.configure.Configure;
import lingyang.err.InitException;
import lingyang.err.SelectorException;


public class FlowService implements Service {
	ExecutorService executor = Executors.newCachedThreadPool();
	Selector lisenSelector = null;
	Selector[] selectors = null;
	Configure configure = null;
	ExecutorService pool = null;

	public FlowService() throws SelectorException {
		try {
			lisenSelector = Selector.open();
		} catch (IOException e) {
			throw new SelectorException();
		}
	}

	public void connectTo(String address, int port) {
		executor.execute(null);
	}

	public void listenAt(int port) {
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
		for (i--; i >= 0; i--) {
			selectors[i] = Selector.open();
			executor.execute(new Worker(i));
		}
		if (configure.usePool()) {
			pool = Executors.newCachedThreadPool();
		}

	}

	protected void wakeup() {

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
				InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), port);
				serverSocketChannel.socket().bind(address);
				serverSocketChannel.configureBlocking(false);
				serverSocketChannel.register(lisenSelector, SelectionKey.OP_ACCEPT, null);
				while (true) {
					lisenSelector.select();
					Iterator<SelectionKey> it = lisenSelector.selectedKeys().iterator();
					while (it.hasNext()) {
						SelectionKey key = it.next();
						SocketChannel channel = (((ServerSocketChannel) key.channel()).accept());
						Session session = SessionFactory.newSession();
						session.setChannel(channel);
						session.setConfigure(configure);
						channel.configureBlocking(false);
						Selector choised=selectors[configure.getStrategy().getIndex(selectors)];
						configure.getProcessor().onConnect(session);
						channel.register(choised,
								SelectionKey.OP_READ, session);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	class Worker implements Runnable {
		int index = 0;

		public Worker(int i) {
			index = i;
		}

		public void run() {
			long timeSpan1 = configure.getReceiveIdle();
			long timeSpan2 =configure.getWriteIdle();
			long timeSpan=timeSpan1-timeSpan2;
			timeSpan =Math.min(timeSpan2, Math.abs(timeSpan));
			while (true) {
				try {
					selectors[index].select(timeSpan);
					Iterator<SelectionKey> keys = selectors[index].selectedKeys().iterator();
						while (keys.hasNext()) {
							SelectionKey key = keys.next();
							keys.remove();
							Session session = (Session) key.attachment();
							if (key.isConnectable()) {
								// ArrayList<Filter>
								// list=configure.getFilterChain();
								configure.getProcessor().onConnect(session);
							}
							if (key.isReadable()) {
//								configure.getHeadFilter().in(session, null);
								configure.getProcessor().onRecive(session);
							}
							if (key.isWritable()) {
//								configure.getTailFilter().out(session, null);
								configure.getProcessor().onWriteble(session);
							}

//						}
					}
					
					if(timeSpan>0){
						Set<SelectionKey> allKeys = selectors[index].keys();
						for(SelectionKey key:allKeys){
							Session session = (Session) key.attachment();
							if (configure.usePool()) {								
							}else{
								long lr=session.getLastReadTime();
								long rspan=configure.getReceiveIdle();
								long lw=session.getLastWriteTime();
								long wspan=configure.getWriteIdle();
								long current=System.currentTimeMillis();
								if(current-lr>=rspan||current-lw>wspan){
//									configure.getHeadFilter().onEvent(session, Event.idle);
									configure.getProcessor().onIdle(session);
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
