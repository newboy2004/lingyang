package com.googlecode.lingyang.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.googlecode.lingyang.ChannelVisitor;
import com.googlecode.lingyang.Event;
import com.googlecode.lingyang.NoSession;
import com.googlecode.lingyang.Processor;
import com.googlecode.lingyang.Session;
import com.googlecode.lingyang.Factory.BufferProvider;
import com.googlecode.lingyang.configure.Configure;
import com.googlecode.lingyang.err.ChannelException;


public class SimpleProcessor extends Processor {
	BufferProvider bufferProvider = new BufferProvider();

	@Override
	public void onConnect(Session session) {
		session.getConfigure().getHeadFilter().onEvent(session, Event.connect);
	}

	@Override
	public void onCreate(Session session) {
		session.getConfigure().getHeadFilter().onEvent(session, Event.create);
	}

	@Override
	public void onIdle(Session session) {
		session.getConfigure().getHeadFilter().onEvent(session, Event.idle);
	}

	@Override
	public void onClose(Session session) {
		session.getConfigure().getHeadFilter().onEvent(session, Event.close);
	}
	@Override
	public void onNoSession(Configure configure, NoSession noSessionEvent) {
		configure.getHeadFilter().onNoSession(noSessionEvent);
	}
	@Override
	public void onRecive(Session session) {
		try {
			ByteBuffer btbuffer = (ByteBuffer) session.acceptVisitor(this,
					ChannelVisitor.Type.read);
			session.getConfigure().getHeadFilter().in(session, btbuffer);
			bufferProvider.release(btbuffer);
		} catch (ChannelException e) {
			session.getConfigure().getHeadFilter().onEvent(session, Event.err);
		}

	}

	@Override
	public void onWriteble(Session session) {
		session.getConfigure().getTailFilter().out(session, null);
		try {
			int bytes = (Integer) session.acceptVisitor(this,
					ChannelVisitor.Type.write);
		} catch (ChannelException e) {
			session.getConfigure().getHeadFilter().onEvent(session, Event.err);
		}

	}

	@Override
	public Object visit(Session session, SocketChannel socketChannel,
			ChannelVisitor.Type type) throws ChannelException {
		switch (type) {
		case read:
			ByteBuffer btb = bufferProvider.alloc(session.getConfigure()
					.getBufferSize());
			btb.clear();
			try {
				socketChannel.read(btb);
				btb.flip();
				session.updateReadTime();
				session.updateWriteBytes(btb.remaining());
				return btb;
			} catch (IOException e) {
				throw new ChannelException();
			}
		case write:
			int writed = 0;
			while (true) {
				ByteBuffer bb = session.peekWriteQueue();
				if (bb == null) {
					session.getSelectionKey().interestOps(
							session.getSelectionKey().interestOps()
									& (~SelectionKey.OP_WRITE));
					break;
				}
				if (bb.remaining() <= 0) {
					bufferProvider.release(bb);
					session.poolWriteQueue();
					continue;
				}
				try {
					int bytes = socketChannel.write(bb);
					writed += bytes;
					if (bytes == 0) {
						break;
					}
				} catch (IOException e) {
					throw new ChannelException();
				}
			}
			session.updateWriteTime();
			session.updateWriteBytes(writed);
			return writed;
		default:
			break;
		}
		return null;
	}



}
