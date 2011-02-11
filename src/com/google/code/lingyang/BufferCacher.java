package com.google.code.lingyang;

import java.nio.ByteBuffer;

public interface BufferCacher {
	public void release(ByteBuffer buf);
	public void release(ByteBuffer[] bufs);
	public ByteBuffer get(int size);
	public ByteBuffer[] get(int[] sizes);
}
