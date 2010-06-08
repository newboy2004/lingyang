package lingyang.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lingyang.ChannelVisitor;
import lingyang.Event;
import lingyang.NoSession;
import lingyang.Processor;
import lingyang.Session;
import lingyang.Factory.BufferProvider;
import lingyang.configure.Configure;
import lingyang.err.ChannelException;

public class PoolProcessor extends Processor {
	ExecutorService executorService = Executors.newCachedThreadPool();
	BufferProvider bufferProvider = new BufferProvider();

	@Override
	public void onConnect(Session session) {
		session.addEvent(Event.connect);
		process(session);
	}

	@Override
	public void onCreate(Session session) {
		session.addEvent(Event.create);
		process(session);
	}

	@Override
	public void onIdle(Session session) {
		session.addEvent(Event.idle);
		process(session);
	}
	@Override
	public void onClose(Session session) {
		session.addEvent(Event.close);
		process(session);
	}

	@Override
	public void onRecive(Session session) {
		process(session);
		ByteBuffer btbuffer;
		try {
			btbuffer = (ByteBuffer) session.acceptVisitor(this,
					ChannelVisitor.Type.read);
			session.addReadQueue(btbuffer);
			session.addEvent(Event.receive);
			process(session);
		} catch (ChannelException e) {
			session.getConfigure().getHeadFilter().onEvent(session, Event.err);
		}
	}

	@Override
	public void onWriteble(Session session) {
		session.getConfigure().getTailFilter().out(session, null);
		session.addEvent(Event.writeble);
		process(session);
	}

	@Override
	public void onNoSession(Configure configure, NoSession noSessionEvent) {
		configure.getHeadFilter().onNoSession(noSessionEvent);
	}
	protected void process(Session session) {
		if (session.getPoolSemaphore().tryAcquire()) {
			executorService.execute(new Worker(session));
		}
	}

	@Override
	public Object visit(Session session, SocketChannel socketChannel, Type type)
			throws ChannelException {

		switch (type) {
		case read:
			ByteBuffer btb = bufferProvider.salloc(session.getConfigure()
					.getBufferSize());
			btb.clear();
			try {
				socketChannel.read(btb);
				btb.flip();
				session.updateReadTime();
				session.updateWriteBytes(btb.remaining());
				return btb;
			} catch (IOException e) {
				throw new ChannelException();
			}
		case write:
			int writed = 0;
			while (true) {
				ByteBuffer bb = session.peekWriteQueue();
				if (bb == null) {
					session.getSelectionKey().interestOps(
							session.getSelectionKey().interestOps()
									& (~SelectionKey.OP_WRITE));
					break;
				}
				if (bb.remaining() <= 0) {
					bufferProvider.srelease(bb);
					session.poolWriteQueue();
					continue;
				}
				try {
					int bytes = socketChannel.write(bb);
					writed += bytes;
					if (bytes == 0) {
						break;
					}
				} catch (IOException e) {
					throw new ChannelException();
				}
			}
			session.updateWriteTime();
			session.updateWriteBytes(writed);
			return writed;
		default:
			break;
		}
		return null;

	}

	class Worker implements Runnable {
		Session session;

		public Worker(Session session) {
			this.session = session;
		}

		@Override
		public void run() {
			Event event = null;
			while (true) {
				event = session.pollEvent();
				if (event == null) {
					break;
				}
				switch (event) {
				case create:
				case connect:
				case close:
				case err:
				case idle:
					session.getConfigure().getHeadFilter().onEvent(session,
							event);
					break;
				case receive:
					while (true) {
						ByteBuffer bb = session.poolReadQueue();
						if (bb == null) {
							break;
						}
						session.getConfigure().getHeadFilter().in(session, bb);
						PoolProcessor.this.bufferProvider.srelease(bb);
					}
					break;
				case writeble:
					try {
						session.acceptVisitor(PoolProcessor.this,
								ChannelVisitor.Type.write);
					} catch (ChannelException e) {
						session.getConfigure().getHeadFilter().onEvent(session,
								Event.err);
					}
					break;
				default:
					break;
				}
			}
			session.getPoolSemaphore().release();
		}

	}

}
