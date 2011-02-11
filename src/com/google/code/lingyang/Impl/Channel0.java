package com.google.code.lingyang.Impl;

import java.io.IOException;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import com.google.code.lingyang.Channel;

public class Channel0 implements Channel {
	private final LinkedList<Event> eventQueue = new LinkedList<Event>();
	private final LinkedList<ByteBuffer> inputQueue = new LinkedList<ByteBuffer>();
	private Boolean isReading = false;
	private Boolean isWrting = false;
	private long lastRead;

	private long lastWrite;
	private final LinkedList<ByteBuffer[]> outputQueue = new LinkedList<ByteBuffer[]>();

	private int readBufferSize = 1024;
	private boolean rejectData = false;

	private AsynchronousSocketChannel socketChannel = null;

	TcpServerCore tcpServerCore = null;
	private long timeOut = 512L;
	private ByteBuffer[] writePendingByteBuf = null;
	private boolean closed = false;
	private boolean monitorReadIdle = true;
	private boolean monitorWriteIdle = false;

	public Channel0(TcpServerCore tcpServerCore,
			AsynchronousSocketChannel achannel) {
		socketChannel = achannel;
		this.tcpServerCore = tcpServerCore;
		lastRead = lastWrite = System.currentTimeMillis();
	}

	@Override
	public void close() {
		if(closed)
			return;
		try {
			this.closed = true;
			this.socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tcpServerCore._onClose(this);
	}

	@Override
	public long getLastRead() {
		return lastRead;
	}

	@Override
	public long getLastWrite() {
		return lastWrite;
	}

	public int getReadBufferSize() {
		return readBufferSize;
	}

	public long getTimeOut() {
		return timeOut;
	}

	@Override
	final public int hashCode() {
		return socketChannel.hashCode();
	}

	@Override
	public boolean isRejectData() {
		return rejectData;
	}

	public Event poolEvent() {
		try {
			return eventQueue.remove(0);
		} catch (Exception e) {
			return null;
		}
	}

	public void pushEvent(Event e) {
		eventQueue.add(e);
	}

	@Override
	final public void read() {
		if (rejectData)
			return;
		synchronized (isReading) {
			if (isReading) {
				return;
			}
			isReading = true;
		}
		ByteBuffer bb = tcpServerCore.getBufferCacher().get(readBufferSize);
		inputQueue.add(bb);
		socketChannel.read(bb, this,
				new CompletionHandler<Integer, Channel0>() {
					@Override
					public void completed(Integer result, Channel0 channel) {
						synchronized (isReading) {
							isReading = false;
						}
						channel.read();
						ByteBuffer bb = inputQueue.poll();
						tcpServerCore._onReceive(channel, bb);
					}

					@Override
					public void failed(Throwable exc, Channel0 channel) {
						synchronized (isReading) {
							isReading = false;
						}
						if (exc instanceof IOException) {
							close();
							return;
						}
						ByteBuffer bb = inputQueue.poll();
						tcpServerCore.getBufferCacher().release(bb);
						tcpServerCore._onThrowable(channel, exc);
					}
				});
	}

	public void setReadBufferSize(int readBufferSize) {
		this.readBufferSize = readBufferSize;
	}

	public void setRejectData(boolean rejectData) {
		this.rejectData = rejectData;
	}

	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}

	@Override
	final public void write(ByteBuffer buf) {
		write(new ByteBuffer[] { buf });
	}

	@Override
	final public void write(ByteBuffer[] bufs) {
		outputQueue.add(bufs);
		write0();
	}

	final private void write0() {
		synchronized (isWrting) {
			if (isWrting)
				return;
			isWrting = true;
		}
		ByteBuffer[] bytebufs = outputQueue.poll();
		if (bytebufs == null) {
			synchronized (isWrting) {
				isWrting = false;
				return;
			}
		}
		writePendingByteBuf = bytebufs;
		socketChannel.write(bytebufs, 0, bytebufs.length, timeOut,
				TimeUnit.SECONDS, this,
				new CompletionHandler<Long, Channel0>() {
					@Override
					public void completed(Long result, Channel0 attachment) {
						synchronized (isWrting) {
							isWrting = false;
						}
						write0();
						tcpServerCore._onWrited(attachment, result);
						if (writePendingByteBuf != null) {
							tcpServerCore.getBufferCacher().release(
									writePendingByteBuf);
						}
					}

					@Override
					public void failed(Throwable exc, Channel0 attachment) {
						synchronized (isWrting) {
							isWrting = false;
						}
						if (exc instanceof IOException) {
							close();
							return;
						}
						tcpServerCore._onThrowable(attachment, exc);
						if (writePendingByteBuf != null) {
							tcpServerCore.getBufferCacher().release(
									writePendingByteBuf);
						}
					}
				});
	}

	@Override
	public boolean isOpen() {
		return !closed;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	public void updateReadTime() {
		this.lastRead = System.currentTimeMillis();
	}

	public void updateWriteTime() {
		this.lastWrite = System.currentTimeMillis();
	}

	@Override
	public void setMonitorReadIdle(boolean monitorReadIdle) {
		this.monitorReadIdle = monitorReadIdle;
	}

	@Override
	public boolean isMonitorReadIdle() {
		return monitorReadIdle;
	}

	@Override
	public void setMonitorWriteIdle(boolean monitorWriteIdle) {
		this.monitorWriteIdle = monitorWriteIdle;
	}

	@Override
	public boolean isMonitorWriteIdle() {
		return monitorWriteIdle;
	}
	public <T> void setOption(SocketOption<T> name, T value) throws IOException{
		this.socketChannel.setOption(name, value);
	}
	public <T> T getOption(SocketOption<T> name) throws IOException{
		return this.socketChannel.getOption(name);
	}
}
