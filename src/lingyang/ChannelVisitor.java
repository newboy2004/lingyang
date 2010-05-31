package lingyang;

import java.nio.channels.SocketChannel;

import lingyang.err.ChannelException;


/**
 * simula C++ friend 
 * @author Administrator
 *
 */
public interface  ChannelVisitor {
	public static enum Type{
		read,write
	}
	Object visit(Session session,SocketChannel socketChannel, Type type) throws ChannelException;
}