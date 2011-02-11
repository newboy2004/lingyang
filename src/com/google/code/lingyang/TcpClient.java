package com.google.code.lingyang;

import java.net.InetSocketAddress;

import com.google.code.lingyang.Impl.TcpServerCore;

abstract public class TcpClient implements Container{
	TcpServerCore tcpServerCore;
	public TcpClient(){
		this(new Profile());
	}
	public TcpClient(Profile profile){
		tcpServerCore =new TcpServerCore(this,profile);
	}
	public final TcpClient connect(String address,int port){
		InetSocketAddress inetSocketAddress=new InetSocketAddress(address,port);
		return connect(inetSocketAddress);
	}
	public final TcpClient connect(InetSocketAddress address){
		tcpServerCore._Connect(address);
		return this;
	}
}
