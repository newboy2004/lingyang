package lingyang;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Set;

import lingyang.configure.Configure;
import lingyang.err.ChannelException;
import lingyang.err.InitException;

public interface Service {
	public void connectTo(String address, int port) throws ChannelException;
	
	public void connectTo(SocketAddress addr) throws ChannelException;

	public void listenAt(int port) throws ChannelException, IOException;

	public void setConfigure(Configure configure) throws InitException;

	public void broadCast(BroadCastMessage bmessage, BroadCastProber prober);

	public void addSession(Session session);
	public boolean removeSession(Session session);
	public Set<Session> snapshotSessions();
	
	public void wakeUp(int index);

	public void closeSession(Session session, int index);
	
	public ServiceStatistic shutDown();

	public void _addReadBytes(long readBytes);

	public void _addWriteBytes(long writeBytes);
}
