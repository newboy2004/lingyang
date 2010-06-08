package lingyang.impl;

import java.nio.channels.SelectionKey;

import lingyang.Event;
import lingyang.Session;
import lingyang.configure.Configure;

@Deprecated
public class PoolWorker implements Runnable {
	SelectionKey key;
	private Configure configure;

	public PoolWorker(SelectionKey key, Configure configure) {
//		this.key = key;
//		this.configure = configure;
//		Session session = (Session) key.attachment();
//		session.hangUp(this);
	}

	public void run() {
//		Session session = (Session) key.attachment();
//		if (this != session.peekHeadWorker()) {
//			return;
//		}
//		try {
//			session.getPoolSemaphore().acquire();
//			while (true) {
//				PoolWorker worker = (PoolWorker) session.poolHeadWorker();
//				if (worker == null) {
//					break;
//				}
//				worker.work();
//			}
//			session.getPoolSemaphore().release();
//		} catch (InterruptedException e) {
//		}
	}

	private void work() {
		Session session = (Session) key.attachment();

		if (key.isConnectable()) {
			configure.getHeadFilter().onEvent(session, Event.create);
		}
		if (key.isReadable()) {
			configure.getHeadFilter().in(session, null);
		}
		if (key.isWritable()) {
			configure.getTailFilter().out(session, null);
		}
	}

}
