package com.google.code.lingyang.Impl;

import java.nio.channels.AsynchronousServerSocketChannel;

public class Event {
	private Object channel;

	private TcpServerCore core;

	private EventType evtType;

	private Object attachment;

	public Event(TcpServerCore core, Object channel, EventType evtType,
			Object attachment) {
		super();
		this.core = core;
		this.channel = channel;
		this.evtType = evtType;
		this.attachment = attachment;
	}

	public Channel0 getChannel() {
		return (Channel0) channel;
	}



	public TcpServerCore getTcpServerCore() {
		return  core;
	}

	public EventType getEvtType() {
		return evtType;
	}

	public Object getAttachment() {
		return attachment;
	}

	public AsynchronousServerSocketChannel getAsynServerSocketSocketChannel() {
		return (AsynchronousServerSocketChannel) channel;
	}
}

enum EventType {
	CLOSE, CONNECTED, EXCEPTION, RECIEVE, WRITED, LISTEN, CONNECT, IDLE
}
