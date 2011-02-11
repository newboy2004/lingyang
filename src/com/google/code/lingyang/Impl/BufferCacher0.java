package com.google.code.lingyang.Impl;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.google.code.lingyang.BufferCacher;

public class BufferCacher0 implements BufferCacher {
	private ThreadLocal<ArrayList<LinkedList<WeakReference<ByteBuffer>>>> localCache = null;

	@Override
	public ByteBuffer get(int size) {
		ArrayList<LinkedList<WeakReference<ByteBuffer>>> arrayList = getArrayList();
		int idex = getIndex(size);
		LinkedList<WeakReference<ByteBuffer>> linkedList = getLinkedList(
				arrayList, idex);
		while (true) {
			try {
				WeakReference<ByteBuffer> wr = linkedList.remove();
				ByteBuffer bb = wr.get();
				if (bb == null) {
					continue;
				}
				return bb;
			} catch (NoSuchElementException e) {
				int i = 512;
				for (; i < size; i = i << 1)
					;
				return ByteBuffer.allocateDirect(i);
			}
		}
	}

	@Override
	public ByteBuffer[] get(int[] sizes) {
		if (sizes == null) {
			return null;
		}
		ByteBuffer[] bbs = new ByteBuffer[sizes.length];
		for (int i = 0; i < sizes.length; ++i) {
			bbs[i] = get(sizes[i]);
		}
		return bbs;
	}

	private ArrayList<LinkedList<WeakReference<ByteBuffer>>> getArrayList() {
		if (localCache == null) {
			localCache = new ThreadLocal<ArrayList<LinkedList<WeakReference<ByteBuffer>>>>();
		}
		ArrayList<LinkedList<WeakReference<ByteBuffer>>> arrayList = localCache
				.get();
		if (arrayList == null) {
			arrayList = new ArrayList<LinkedList<WeakReference<ByteBuffer>>>(8);
			localCache.set(arrayList);
		}
		if (arrayList.size() <= 0) {
			for (int i = 0; i < 8; ++i) {
				LinkedList<WeakReference<ByteBuffer>> linkedList = new LinkedList<WeakReference<ByteBuffer>>();
				arrayList.add(linkedList);
			}
		}
		return arrayList;
	}

	private int getIndex(ByteBuffer buf) {
		int capacity = buf.capacity();
		return getIndex(capacity);
	}

	private int getIndex(int capacity) {
		capacity = (capacity - 1) >>> 8;
		int idex = 0;
		for (; capacity > 0; ++idex) {
			capacity = capacity >>> 1;
		}
		return idex;
	}

	private LinkedList<WeakReference<ByteBuffer>> getLinkedList(
			ArrayList<LinkedList<WeakReference<ByteBuffer>>> arrayList,
			int index) {
		LinkedList<WeakReference<ByteBuffer>> linkedList = arrayList.get(index);
		if (linkedList == null) {
			linkedList = new LinkedList<WeakReference<ByteBuffer>>();
			arrayList.add(linkedList);
		}
		return linkedList;
	}

	@Override
	public void release(ByteBuffer buf) {
		if (buf == null) {
			return;
		}
		buf.clear();
		ArrayList<LinkedList<WeakReference<ByteBuffer>>> arrayList = getArrayList();
		int idex = getIndex(buf);
		LinkedList<WeakReference<ByteBuffer>> linkedList = getLinkedList(
				arrayList, idex);
		linkedList.add(new WeakReference<ByteBuffer>(buf));
	}

	@Override
	public void release(ByteBuffer[] bufs) {
		for (ByteBuffer bb : bufs) {
			release(bb);
		}
	}
}
