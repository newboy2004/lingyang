package lingyang.impl;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lingyang.Event;
import lingyang.Processor;
import lingyang.Session;
import lingyang.err.ChannelException;


public class PoolProcessor extends Processor {
	ExecutorService executorService = Executors.newCachedThreadPool();

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
	public void onRecive(Session session) {
		session.addEvent(Event.create);
		process(session);
	}

	@Override
	public void onWriteble(Session session) {

	}

	protected void process(Session session) {

		if (session.getPoolSemaphore().tryAcquire()) {
			executorService.execute(new Worker(session));
		}
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
					session.getConfigure().getHeadFilter().onEvent(session, event);
					break;
				case receive:
					session.getConfigure().getHeadFilter().in(session, null);
					break;
				case writeble:
					break;
				default:
					break;
				}
			}
			session.getPoolSemaphore().release();
		}

	}

	@Override
	public Object visit(Session session, SocketChannel socketChannel, Type type)
			throws ChannelException {
		// TODO Auto-generated method stub
		return null;
	}

	// public static void main(String[] args){
	// ConcurrentLinkedQueue<String> events = new
	// ConcurrentLinkedQueue<String>();
	// if(events.poll()==null){
	// System.out.println("true");
	// }
	// }
}
