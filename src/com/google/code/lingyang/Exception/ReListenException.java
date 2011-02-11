package com.google.code.lingyang.Exception;

public class ReListenException extends RuntimeException {

	private static final long serialVersionUID = -6393927983002540963L;

	public ReListenException() {
		super();
	}

	public ReListenException(String s) {
		super(s);
	}

	public ReListenException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReListenException(Throwable cause) {
		super(cause);
	}

}
