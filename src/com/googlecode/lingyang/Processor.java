package com.googlecode.lingyang;

import com.googlecode.lingyang.configure.Configure;

public abstract class Processor implements ChannelVisitor {

	public abstract void onRecive(Session session);

	public abstract void onWriteble(Session session);

	public abstract void onIdle(Session session);

	public abstract void onConnect(Session session);

	public abstract void onCreate(Session session);

	public abstract void onClose(Session session);

	public abstract void onNoSession(Configure configure, NoSession noSessionEvent);

}
