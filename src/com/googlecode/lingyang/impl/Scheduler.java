package com.googlecode.lingyang.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.googlecode.lingyang.Schedule;
import com.googlecode.lingyang.Service;
import com.googlecode.lingyang.Session;


public class Scheduler implements Runnable {
	public static class ScheduleEvent{
		List<Object> params=null;
		public ScheduleEvent(Object... param){
			for(Object o:param){
				if(params==null){
					params=new ArrayList<Object>();
				}
				params.add(o);
			}
		}
		public Object poolParam(){ 
			if(params==null)
				return null;
			if(params.size()==0)
				return null;
			return params.remove(0);
		}
		
		
	}
	Service contextService;
	
	Schedule schedule;
	
	BlockingQueue<ScheduleEvent> eventQueue=new LinkedBlockingQueue<ScheduleEvent>();
	
	long timeout=-1;//60*1000;
	public Scheduler(Service service, Schedule schedule,long timeout) {
		this.contextService=service;
		this.schedule=schedule;
		this.timeout=timeout;
	}

	public void run() {
		while(contextService!=null&&contextService.isRunning()){
			ScheduleEvent event=null;
			try {
				if(timeout<0){
					event=eventQueue.poll();
				}else{		
					event=eventQueue.poll(timeout, TimeUnit.MILLISECONDS);
				}
			} catch (InterruptedException e) {
				return;
			}
			Set<Session> snap=contextService.snapshotSessions();
			if(event==null){			
				for(Session session:snap){
					if(schedule.needScedule(session)){
						schedule.onTimeOut(session);
					}
				}
			}else{
				for(Session session:snap){
					if(schedule.needScedule(session)){
						schedule.process(session,event);
					}
				}
			}
		}
	}

}
