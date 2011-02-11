package com.google.code.lingyang;

import com.google.code.lingyang.Exception.ReListenException;
import com.google.code.lingyang.Impl.TcpServerCore;

abstract public class TcpServer implements Container{
	TcpServerCore tcpServerCore;

	public final TcpServer listen(int port)   {
		Profile profile=new Profile();
		profile.setListenPort(port);
		return listen(profile);
	}
	public final TcpServer listen(Profile profile)  {
		if(tcpServerCore!=null){
			throw new ReListenException();
		}
		tcpServerCore =new TcpServerCore(this,profile);
		tcpServerCore.listen();
		return this;
	}
	public final void shutdown() {
		tcpServerCore.shutdown();
	}

}
