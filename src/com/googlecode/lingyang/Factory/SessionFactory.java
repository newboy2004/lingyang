package com.googlecode.lingyang.Factory;

import com.googlecode.lingyang.Session;
import com.googlecode.lingyang.impl.SessionImpl;

public class SessionFactory {
	public static Session newSession(){
		return new SessionImpl();
	}
}
