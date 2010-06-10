package com.googlecode.lingyang;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Semaphore;

import com.googlecode.lingyang.configure.Configure;
import com.googlecode.lingyang.err.ChannelException;


public interface Session extends Registable {
	public void write(Object message);

	public void _write(ByteBuffer byteBuffer);

	public void setAttr(Object key, Object value);
	
	public Object clearAttr(Object key); 
	
	public void clearAllAttr();

	public Object getAttr(Object key);

	public InetAddress getRemoteAddress();

	public int getRemotePort();

	public void setChannel(SocketChannel channel);

	public Semaphore getPoolSemaphore();

	public long getLastWriteTime();

	public long getLastReadTime();

	public long updateWriteTime();

	public long updateReadTime();

	public void close();

	public void setConfigure(Configure configure);

	public Configure getConfigure();

	public void addEvent(Event e);

	public Event pollEvent();

	public Event peekEvent();

	public Object acceptVisitor(ChannelVisitor visitor, ChannelVisitor.Type type)
			throws ChannelException;

	public ByteBuffer poolWriteQueue();

	public ByteBuffer peekWriteQueue();

	public ByteBuffer poolReadQueue();

	public ByteBuffer peekReadQueue();

	public boolean addReadQueue(ByteBuffer byteBuffer);

	public void setSelectionKey(SelectionKey key);

	public SelectionKey getSelectionKey();

	public void updateWriteBytes(long bytes);

	public void updateReadBytes(long bytes);

	public long writeBytes();

	public long readBytes();

	public long getSessionId();

	public void broadCast(Object message, BroadCastProber prober);

	public int getProcessorId();

	public void setProcessorId(int pid);

	public Object poolOutPutMessage();

	public void register(Selector choised, int opt) throws ChannelException;

	public void _close() throws ChannelException;
	
	public void _clearBuffer();
}