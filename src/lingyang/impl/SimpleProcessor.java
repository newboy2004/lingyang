package lingyang.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import lingyang.ChannelVisitor;
import lingyang.Event;
import lingyang.Processor;
import lingyang.Session;
import lingyang.err.ChannelException;


public class SimpleProcessor extends Processor {

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
	public void onRecive(Session session) {	

			ByteBuffer btbuffer;
			try {
				btbuffer = (ByteBuffer)session.acceptVisitor(this,ChannelVisitor.Type.read);
				session.getConfigure().getHeadFilter().in(session, btbuffer);	
			} catch (ChannelException e) {
				session.getConfigure().getHeadFilter().onEvent(session, Event.err);
			}
			
	}

	@Override
	public void onWriteble(Session session) {
		session.getConfigure().getTailFilter().out(session, null);

	}

	@Override
	public Object visit(Session session, SocketChannel socketChannel, ChannelVisitor.Type type) throws ChannelException {
		switch (type) {
		case read:
			ByteBuffer btb = session.getBuffer(session.getConfigure().getBufferSize());
			btb.reset();
			try {
				socketChannel.read(btb);
				btb.flip();
				return btb;
			} catch (IOException e) {
				throw new ChannelException();
			}
		case write:
			int writed=0;
			while(true){			
				ByteBuffer bb=session.peekWriteQueue();	
				if(bb==null){
					session.getSelectionKey().interestOps(session.getSelectionKey().interestOps() & (~SelectionKey.OP_WRITE));
					break;
				}
				if(bb.remaining()<=0){
					session.poolWriteQueue();
					continue;
				}
				try {
					 int bytes=socketChannel.write(bb);
					 writed+=bytes;
					if(bytes==0){
						break;
					}
				} catch (IOException e) {
					throw new ChannelException();
				}
			}
			return writed;
		default:
			break;
		}
		return null;
	}

}
