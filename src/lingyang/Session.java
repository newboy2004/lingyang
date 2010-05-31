package lingyang;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Semaphore;

import lingyang.configure.Configure;
import lingyang.err.ChannelException;


public interface Session {
	public void write(Object message);

	public void _write(ByteBuffer byteBuffer);

	public void setAttr(Object key, Object value);

	public Object getAttr(Object key);

	public String getRemoteAddress();

	public int getRemotePort();

	public void setChannel(SocketChannel channel);

	// public SocketChannel getSocketChannel();

	public ByteBuffer getBuffer(int size);

	public void hangUp(Runnable worker);

	public Runnable peekHeadWorker();

	public Runnable poolHeadWorker();

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

	public void setSelectionKey(SelectionKey key);

	public SelectionKey getSelectionKey();
}