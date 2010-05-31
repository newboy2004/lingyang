package lingyang;

import java.nio.channels.SocketChannel;



public abstract class Processor  implements ChannelVisitor{

	public abstract void onRecive(Session session);

	public abstract void onWriteble(Session session);
	
	public abstract void onIdle(Session session);
	
	public abstract void onConnect(Session session);
	
	public abstract void onCreate(Session session);
}
