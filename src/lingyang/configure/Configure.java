package lingyang.configure;

import lingyang.Filter;
import lingyang.Handler;
import lingyang.Processor;
import lingyang.Schedule;

public interface Configure {
	public void setHandler(Handler handler);

	public void addFilter(Filter filter);

	// public ArrayList<Filter> getFilterChain();

	public int getSelectorNum();

	public void setSelectorNum(int num);

	public void setWaitingThreadNum(int num);

	public int getWaitingThreadNum();

	public Strategy getStrategy();

	public void setStrategy(Strategy strategy);

	public Filter getHeadFilter();

	public Filter getTailFilter();

	public void setBufferSize(int size);

	public int getBufferSize();

	public boolean usePool();

	public void setUsePool(boolean use);

	public long getReceiveIdle();

	public void setReceiveIdle(int timeSpan);

	public long getWriteIdle();

	public void setWriteIdle(int timeSpan);

	public Processor getProcessor();

	public void setProcessor(Processor processor);

	public void setSchedule(Schedule schedule);

	public Schedule getSchedule();
	
	public long getScheduleTimeOut();
	public void setScheduleTimeOut(long scheduleTimeOut);

}
