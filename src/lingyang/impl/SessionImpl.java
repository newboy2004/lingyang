package lingyang.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import lingyang.BroadCastMessage;
import lingyang.BroadCastProber;
import lingyang.ChannelVisitor;
import lingyang.Event;
import lingyang.Service;
import lingyang.Session;
import lingyang.ChannelVisitor.Type;
import lingyang.configure.Configure;
import lingyang.err.ChannelException;

public class SessionImpl implements Session {

	private HashMap<Object, Object> attributes = new HashMap<Object, Object>();
	SocketChannel socketChannel = null;
	Semaphore poolSemaphore = new Semaphore(1);

	ConcurrentLinkedQueue<Event> events = new ConcurrentLinkedQueue<Event>();
	ConcurrentLinkedQueue<ByteBuffer> writePendingQueue = new ConcurrentLinkedQueue<ByteBuffer>();
	ConcurrentLinkedQueue<ByteBuffer> readPendingQueue = new ConcurrentLinkedQueue<ByteBuffer>();
	ConcurrentLinkedQueue<Object> outPutQueue = new ConcurrentLinkedQueue<Object>();
	long lastReadTime = System.currentTimeMillis();
	long lastWriteTime = System.currentTimeMillis();
	Configure configure;
	SelectionKey selectionKey;
	long readBytes = 0;
	long writeBytes = 0;
	private static AtomicLong idGenerator = new AtomicLong(1);
	long sessionId = idGenerator.getAndAdd(1);
	Service service;
	int processorId;

	public Object getAttr(Object key) {
		return attributes.get(key);
	}

	public InetAddress getRemoteAddress() {
		return socketChannel.socket().getInetAddress();
	}

	public int getRemotePort() {
		return socketChannel.socket().getPort();
	}

	public void setAttr(Object key, Object value) {
		attributes.put(key, value);
	}

	public void setChannel(SocketChannel channel) {
		socketChannel = channel;
	}

	protected SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public Semaphore getPoolSemaphore() {
		return poolSemaphore;
	}

	public long getLastReadTime() {

		return lastReadTime;
	}

	public long getLastWriteTime() {

		return lastWriteTime;
	}

	public long updateReadTime() {
		lastReadTime = System.currentTimeMillis();
		return lastReadTime;
	}

	public long updateWriteTime() {
		lastWriteTime = System.currentTimeMillis();
		return lastWriteTime;
	}

	@Override
	public void close() {
		service.closeSession(this, this.getProcessorId());
	}

	@Override
	public Configure getConfigure() {
		return configure;
	}

	@Override
	public void setConfigure(Configure configure) {
		this.configure = configure;
	}

	@Override
	public void addEvent(Event e) {
		events.add(e);
	}

	@Override
	public Event pollEvent() {
		return events.poll();
	}

	@Override
	public Event peekEvent() {
		return this.events.peek();
	}

	@Override
	public Object acceptVisitor(ChannelVisitor visitor, Type type)
			throws ChannelException {
		return visitor.visit(this, this.getSocketChannel(), type);
	}

	@Override
	public ByteBuffer poolWriteQueue() {
		return writePendingQueue.poll();
	}

	@Override
	public void _write(ByteBuffer byteBuffer) {
		this.writePendingQueue.add(byteBuffer);
	}

	@Override
	public void write(Object message) {
		this.outPutQueue.add(message);
		selectionKey.interestOps(selectionKey.interestOps()
				| SelectionKey.OP_WRITE);// & (~SelectionKey.OP_WRITE));
		wakeUp();

	}

	public Object poolOutPutMessage() {
		return this.outPutQueue.poll();
	}

	private void wakeUp() {
		service.wakeUp(processorId);
	}

	@Override
	public void setSelectionKey(SelectionKey key) {
		this.selectionKey = key;

	}

	@Override
	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	@Override
	public ByteBuffer peekWriteQueue() {
		return this.writePendingQueue.peek();
	}

	@Override
	public long readBytes() {
		return readBytes;
	}

	@Override
	public void updateReadBytes(long bytes) {
		this.readBytes += bytes;
	}

	@Override
	public void updateWriteBytes(long bytes) {
		this.writeBytes += bytes;
	}

	@Override
	public long writeBytes() {
		return writeBytes;
	}

	@Override
	public long getSessionId() {
		return sessionId;
	}

	@Override
	public ByteBuffer peekReadQueue() {
		return readPendingQueue.peek();
	}

	@Override
	public ByteBuffer poolReadQueue() {
		return readPendingQueue.poll();
	}

	@Override
	public boolean addReadQueue(ByteBuffer byteBuffer) {
		return readPendingQueue.add(byteBuffer);

	}

	@Override
	public void broadCast(Object message, BroadCastProber prober) {
		service.broadCast(new BroadCastMessage(this.getSessionId(), message),
				prober);

	}

	@Override
	public Service getContextService() {
		return service;
	}

	@Override
	public void register(Service service) {
		service.addSession(this);
		this.service = service;
	}

	@Override
	public void register(Selector selector, int ops) throws ChannelException {
		try {
			SelectionKey key = this.socketChannel.register(selector, ops, this);
			this.setSelectionKey(key);
		} catch (ClosedChannelException e) {
			throw new ChannelException();
		}
	}

	@Override
	public int getProcessorId() {
		return processorId;
	}

	@Override
	public void setProcessorId(int pid) {
		this.processorId = pid;
	}

	@Override
	public void _close() throws ChannelException {
		this.selectionKey.cancel();
		try {
			service._addReadBytes(this.readBytes());
			service._addWriteBytes(this.writeBytes());
			_clearBuffer();
			this.socketChannel.close();
		} catch (IOException e) {
			throw new ChannelException();
		}
	}

	@Override
	public void _clearBuffer() {
		for (;;) {
			if (events.poll() == null)
				break;
		}
		for (;;) {
			if (writePendingQueue.poll() == null)
				break;
		}
		for (;;) {
			if (readPendingQueue.poll() == null)
				break;
		}
		for (;;) {
			if (outPutQueue.poll() == null)
				break;
		}
	}

	@Override
	public void clearAllAttr() {
		attributes.clear();
	}

	@Override
	public Object clearAttr(Object key) {
		return attributes.remove(key);
	}

}
