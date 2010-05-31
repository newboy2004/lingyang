package lingyang.impl;

import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lingyang.Service;
import lingyang.configure.Configure;
import lingyang.err.ChannelException;
import lingyang.err.InitException;


public class ThreadPoolService implements Service{
	ExecutorService executor = Executors.newCachedThreadPool();
	Selector lisenSelector = null;
	Selector selector= null;
	public void connectTo(String address, int port) {
		
	}

	public void listenAt(int port) throws ChannelException {
		
	}

	public void setConfigure(Configure configure) throws InitException {
		
	}

}
