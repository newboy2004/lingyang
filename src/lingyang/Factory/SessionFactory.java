package lingyang.Factory;

import lingyang.Session;
import lingyang.impl.DefaultSessionImpl;

public class SessionFactory {
	public static Session newSession(){
		return new DefaultSessionImpl();
	}
}
