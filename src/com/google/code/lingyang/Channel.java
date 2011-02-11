package com.google.code.lingyang;

import java.nio.ByteBuffer;

public interface Channel {

	void close();

	long getLastRead();

	long getLastWrite();

	boolean isRejectData();

	void read();

	void write(ByteBuffer buf);

	void write(ByteBuffer[] bufs);

	boolean isOpen();

	boolean isClosed();

	public void setMonitorReadIdle(boolean monitorReadIdle);

	public boolean isMonitorReadIdle();

	public void setMonitorWriteIdle(boolean monitorWriteIdle);

	public boolean isMonitorWriteIdle();
	
	
}
