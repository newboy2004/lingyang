package lingyang.impl;

import lingyang.Session;
import lingyang.future.AbstractFuture;

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
