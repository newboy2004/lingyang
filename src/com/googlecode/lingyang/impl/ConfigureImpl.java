package com.googlecode.lingyang.impl;

import java.nio.ByteBuffer;

import com.googlecode.lingyang.Event;
import com.googlecode.lingyang.Filter;
import com.googlecode.lingyang.Handler;
import com.googlecode.lingyang.NoSession;
import com.googlecode.lingyang.Processor;
import com.googlecode.lingyang.Schedule;
import com.googlecode.lingyang.Session;
import com.googlecode.lingyang.configure.Configure;
import com.googlecode.lingyang.configure.Strategy;



public class ConfigureImpl implements Configure {

	int selectorNum = 1;
	int waitingThreadNum = 2;
	int bufferSize = 1024;
	Strategy strategy = Strategy.next;
	HeadFilter headFilter = null;
	TailFilter tailFilter = null;
	int writeIdle = 0;
	int receiveIdle = 0;
	boolean usePool = false;
	Processor processor;
	long scheduleTimeOut=60*1000;
	Schedule schedule;

	public ConfigureImpl() {
		headFilter = new HeadFilter();
		tailFilter = new TailFilter();
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
			return next.in(session, data);
		}

		public Filter next() {
			return next;
		}

		public void onEvent(Session session, Event event) {
			next.onEvent(session, event);
		}

		public Object out(Session session, Object data) {
			session._write((ByteBuffer)data);
			return data;
		}

		public void setBefore(Filter filter) {

		}

		public void setNext(Filter filter) {
			this.next = filter;
		}

		@Override
		public void onNoSession(NoSession noSessionEvent) {
			next.onNoSession(noSessionEvent);
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
			handler.onWriteble(session);
			//notifyHandler();
			//用线程域变量
			//ArrayList<ByteBuffer> bufs=new ArrayList<ByteBuffer>();
			//for(;;){
				Object message=session.poolOutPutMessage();
				if(message==null)
					return null;
				return before.out(session, message);
				//session._write((ByteBuffer)before.out(session, message));
			//}
			//return null;
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

		@Override
		public void onNoSession(NoSession noSessionEvent) {
			handler.onNoSession(noSessionEvent);
		}

	}

	@Override
	public Schedule getSchedule() {
		return schedule;
	}

	@Override
	public void setSchedule(Schedule schedule) {
		this.schedule=schedule;
	}

	@Override
	public long getScheduleTimeOut() {
		return scheduleTimeOut;
	}

	@Override
	public void setScheduleTimeOut(long scheduleTimeOut) {
		this.scheduleTimeOut=scheduleTimeOut;
	}

}
