package com.googlecode.lingyang.err;

import java.net.SocketAddress;

import com.googlecode.lingyang.NoSession;


public class ExceptionEvent implements NoSession {
	private SocketAddress remoteAddress;
	private Exception exception;
	public ExceptionEvent(SocketAddress addr, Exception e){
		this.remoteAddress=addr;
		this.exception=e;
	}
	public void setException(Exception exception) {
		this.exception = exception;
	}
	public Exception getException() {
		return exception;
	}
	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}
}
