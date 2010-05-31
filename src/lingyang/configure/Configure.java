package lingyang.configure;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import lingyang.Filter;
import lingyang.Handler;
import lingyang.Processor;


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
	
	public void setIdleUnit(TimeUnit idleUnit);
	
	public long getReceiveIdle();
	
	public void setReceiveIdle(int timeSpan);
	
	public long getWriteIdle();
	
	public void setWriteIdle(int timeSpan);
	
	public Processor getProcessor();
	
	public void setProcessor(Processor processor);
	
}
