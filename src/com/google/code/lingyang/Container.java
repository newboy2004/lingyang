package com.google.code.lingyang;

import java.nio.ByteBuffer;

public interface Container {
	void onReceive(Channel channel, ByteBuffer byteBuf);

	void onClose(Channel channel);

	void onConnect(Channel channel);

	void onThrowable(Channel channel, Throwable e);

	void onWrited(Channel channel, Long writed);

	void onThrowable(Throwable e);

	void onIdle(Channel channel);
}
