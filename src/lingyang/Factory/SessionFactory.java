package lingyang.Factory;

import lingyang.Session;
import lingyang.impl.SessionImpl;

public class SessionFactory {
	public static Session newSession(){
		return new SessionImpl();
	}
}
