package lingyang.impl;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import lingyang.ChannelVisitor;
import lingyang.Event;
import lingyang.Session;
import lingyang.ChannelVisitor.Type;
import lingyang.configure.Configure;
import lingyang.err.ChannelException;


public class DefaultSessionImpl  implements Session {

	private HashMap<Object, Object> attributes = new HashMap<Object, Object>();
	SocketChannel socketChannel = null;
	WeakReference<ByteBuffer> rBuffer = null;

	ConcurrentLinkedQueue<Runnable> workers = new ConcurrentLinkedQueue<Runnable>();
	Semaphore poolSemaphore = new Semaphore(1);

	ConcurrentLinkedQueue<Event> events = new ConcurrentLinkedQueue<Event>();
	ConcurrentLinkedQueue<ByteBuffer> writePendingQueue = new ConcurrentLinkedQueue<ByteBuffer>();
	ConcurrentLinkedQueue<Object> outPutQueue = new ConcurrentLinkedQueue<Object>();
	long lastReadTime = System.currentTimeMillis();
	long lastWriteTime = System.currentTimeMillis();
	Configure configure;
	SelectionKey selectionKey;
	public Object getAttr(Object key) {
		return attributes.get(key);
	}

	public String getRemoteAddress() {
		return null;
	}

	public int getRemotePort() {
		return 0;
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

	public ByteBuffer getBuffer(int size) {
		if (rBuffer == null || rBuffer.get() == null) {
			rBuffer = new WeakReference<ByteBuffer>(ByteBuffer.allocate(size));
		}
		return rBuffer.get();
	}

	public void hangUp(Runnable worker) {
		workers.add(worker);
	}

	public Runnable peekHeadWorker() {
		return workers.peek();
	}

	public Runnable poolHeadWorker() {
		return workers.poll();
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

	}

	@Override
	public Configure getConfigure() {
		// TODO Auto-generated method stub
		return configure;
	}

	@Override
	public void setConfigure(Configure configure) {
		this.configure=configure;
	}

	@Override
	public void addEvent(Event e) {
		// TODO Auto-generated method stub
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
	public Object acceptVisitor(ChannelVisitor visitor, Type type) throws ChannelException {
		return visitor.visit(this,this.getSocketChannel(), type);
	}

	@Override
	public ByteBuffer poolWriteQueue() {
		return writePendingQueue.poll();
	}

	@Override
	public void _write(ByteBuffer byteBuffer) {
		this.writePendingQueue.poll();
	}

	@Override
	public void write(Object message) {
		this.outPutQueue.add(message);
		selectionKey.interestOps(selectionKey.interestOps()|SelectionKey.OP_WRITE);// & (~SelectionKey.OP_WRITE));
		
	}

	@Override
	public void setSelectionKey(SelectionKey key) {
		this.selectionKey=key;
		
	}

	@Override
	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	@Override
	public ByteBuffer peekWriteQueue() {
		return this.writePendingQueue.peek();
	}

}
