package com.googlecode.lingyang.impl;

import com.googlecode.lingyang.Session;
import com.googlecode.lingyang.future.AbstractFuture;

public class ConnectFuture extends AbstractFuture<Session>{
	ConnectFuture(){
		super();
		lock();
	}
	public void setSession(Session session){
		super.setT(session);
		ready();
	}
}
