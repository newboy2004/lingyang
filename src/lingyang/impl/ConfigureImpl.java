package lingyang.impl;

import java.util.concurrent.TimeUnit;

import lingyang.Event;
import lingyang.Filter;
import lingyang.Handler;
import lingyang.Processor;
import lingyang.Session;
import lingyang.configure.Configure;
import lingyang.configure.Strategy;


public class ConfigureImpl implements Configure {

	int selectorNum = 1;
	int waitingThreadNum = 2;
	int bufferSize = 1024;
	Strategy strategy = Strategy.next;
	HeadFilter headFilter = null;
	TailFilter tailFilter = null;
	TimeUnit idleUint = TimeUnit.MILLISECONDS;
	int writeIdle = 0;
	int receiveIdle = 0;
	boolean usePool = false;
	Processor processor;

	public ConfigureImpl() {
		HeadFilter headFilter = new HeadFilter();
		TailFilter tailFilter = new TailFilter();
		headFilter.setNext(tailFilter);
		tailFilter.setBefore(headFilter);
		processor = new SimpleProcessor();
	}

	public void addFilter(Filter filter) {
		tailFilter.before().setNext(filter);
		tailFilter.setBefore(filter);
		filter.setBefore(tailFilter.before());
		filter.setNext(tailFilter);
	}

	public void setHandler(Handler handler) {
		tailFilter.setHandler(handler);
	}

	public int getSelectorNum() {
		return selectorNum;
	}

	public void setSelectorNum(int num) {

		selectorNum = num;
	}

	public int getWaitingThreadNum() {
		return waitingThreadNum;
	}

	public void setWaitingThreadNum(int num) {
		waitingThreadNum = num;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public Filter getHeadFilter() {
		return headFilter;
	}

	public Filter getTailFilter() {
		return tailFilter;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int size) {
		bufferSize = size;
	}

	public void setUsePool(boolean use) {
		usePool = use;
		if(usePool){
			this.processor=new PoolProcessor();
		}
	}

	public boolean usePool() {
		return usePool;
	}

	public long getReceiveIdle() {
		return this.receiveIdle;
	}

	public long getWriteIdle() {
		return this.writeIdle;
	}

	public void setReceiveIdle(int timeSpan) {
		this.receiveIdle = timeSpan;
	}

	public void setWriteIdle(int timeSpan) {
		this.writeIdle = timeSpan;
	}

	public void setIdleUnit(TimeUnit idleUnit) {
		this.idleUint = idleUnit;

	}

	@Override
	public Processor getProcessor() {
		return processor;
	}

	@Override
	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	class HeadFilter implements Filter {

		Filter next;

		public Filter before() {
			return null;
		}

		/***
		 * 
		 * data is null
		 */
		public Object in(Session session, Object data) {
//			ByteBuffer btb = session.getBuffer(ConfigureImpl.this.getBufferSize());
//			btb.reset();
//			try {
//				while (((NetChannel) session)._read(btb) > 0) {
//					btb.flip();
//					next.in(session, btb);
//					btb.reset();
//				}
//			} catch (IOException e) {
//				next.onEvent(session, Event.err);
//			}
			return data;
		}

		public Filter next() {
			return next;
		}

		public void onEvent(Session session, Event event) {
			next.onEvent(session, event);
		}

		public Object out(Session session, Object data) {
//			try {
//				((NetChannel) session)._write((ByteBuffer) data);
//			} catch (IOException e) {
//				next.onEvent(session, Event.err);
//			}
			return data;
		}

		public void setBefore(Filter filter) {

		}

		public void setNext(Filter filter) {
			this.next = filter;
		}

	}

	class TailFilter implements Filter {

		Filter before;
		private Handler handler;

		public Filter before() {

			return before;
		}

		public Object in(Session session, Object data) {
			handler.onRecive(session, data);
			return null;
		}

		public Filter next() {
			return null;
		}

		public void onEvent(Session session, Event event) {
			switch (event) {
			case create:
				handler.onCreate(session);
				break;
			case connect:
				handler.onConnect(session);
				break;
			case idle:
				handler.onIdle(session);
				break;
			case err:
				handler.onErr(session);
				break;
			case close:
				handler.onClose(session);
				break;
			}
		}

		public Object out(Session session, Object data) {
			return before.out(session, data);
		}

		public void setBefore(Filter filter) {
			this.before = filter;
		}

		public void setNext(Filter filter) {

		}

		public void setHandler(Handler handler) {
			this.handler = handler;
		}

		public Handler getHandler() {
			return handler;
		}

	}

}
