package lingyang.Factory;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;

public class BufferProvider {

	ArrayDeque<WeakReference<ByteBuffer>> pool=new ArrayDeque<WeakReference<ByteBuffer>>();

	public ByteBuffer alloc(int capability) {
		WeakReference<ByteBuffer> wr = pool.pollLast();
		if (wr == null) {
			return ByteBuffer.allocate(capability);
		}
		ByteBuffer ret = wr.get();
		if (ret == null) {
			return ByteBuffer.allocate(capability);
		}
		return ret;
	}

	public void release(ByteBuffer buffer) {
		if (buffer == null) {
			return;
		}
		pool.addLast(new WeakReference<ByteBuffer>(buffer));
	}

	public synchronized ByteBuffer salloc(int capability) {
		WeakReference<ByteBuffer> wr = pool.pollLast();
		if (wr == null) {
			return ByteBuffer.allocate(capability);
		}
		ByteBuffer ret = wr.get();
		if (ret == null) {
			return ByteBuffer.allocate(capability);
		}
		return ret;
	}

	public synchronized void srelease(ByteBuffer buffer) {
		if (buffer == null) {
			return;
		}
		pool.addLast(new WeakReference<ByteBuffer>(buffer));
	}

}
