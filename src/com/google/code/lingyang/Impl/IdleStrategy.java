package com.google.code.lingyang.Impl;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.code.lingyang.Channel;

public class IdleStrategy {
	Collection<Channel> channels;
	TcpServerCore tcpServerCore;
	int period;
	long span;
	private ScheduledExecutorService scheduledExecutorService = Executors
			.newSingleThreadScheduledExecutor();

	public IdleStrategy(TcpServerCore tcpServerCore, int period) {
		this.period = period;
		channels = null;
		this.tcpServerCore = tcpServerCore;
		span = period *1000 ;
	}

	public void start() {
		scheduledExecutorService.scheduleAtFixedRate(new ScheduleWorker(this),
				5*60, period, TimeUnit.SECONDS);
	}

	public void shutdown() {
		scheduledExecutorService.shutdown();
	}

	public void updateFlash(Collection<Channel> newChannels) {
		channels = newChannels;
	}
}

class ScheduleWorker implements Runnable {
	IdleStrategy idleStrategy;

	public ScheduleWorker(IdleStrategy idleStrategy) {
		this.idleStrategy = idleStrategy;
	}

	@Override
	public void run() {
		if (idleStrategy.channels == null) {
			return;
		}
		long now = System.currentTimeMillis();
		for (Channel channel : idleStrategy.channels) {
			if (!channel.isOpen()) {
				continue;
			}
			long rt = channel.getLastRead();
			if (channel.isMonitorReadIdle() && (now - rt) >= idleStrategy.span) {
				idleStrategy.tcpServerCore._onIdle((Channel0) channel);
				continue;
			}
			long wt = channel.getLastWrite();
			if (channel.isMonitorWriteIdle() && (now - wt) > idleStrategy.span) {
				idleStrategy.tcpServerCore._onIdle((Channel0) channel);
			}
		}
	}

}