package lingyang;

import lingyang.impl.Scheduler.ScheduleEvent;

public interface Schedule {
	public boolean needScedule(Session session); 
	public void process(Session session, ScheduleEvent event);
	public void onTimeOut(Session session);
}
