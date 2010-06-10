package com.googlecode.lingyang;

public class BroadCastMessage {
	private long sessionId;
	private Object message;

	public BroadCastMessage(long sessionId, Object message) {
		super();
		this.sessionId = sessionId;
		this.message = message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public Object getMessage() {
		return message;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public long getSessionId() {
		return sessionId;
	}
}
