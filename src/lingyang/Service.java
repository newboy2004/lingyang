package lingyang;

import lingyang.configure.Configure;
import lingyang.err.ChannelException;
import lingyang.err.InitException;

public interface Service {
	public void connectTo(String address, int port);

	public void listenAt(int port) throws ChannelException;

	public void setConfigure(Configure configure) throws InitException;
}
