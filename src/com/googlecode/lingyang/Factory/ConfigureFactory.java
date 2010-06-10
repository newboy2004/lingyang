package com.googlecode.lingyang.Factory;

import com.googlecode.lingyang.configure.Configure;
import com.googlecode.lingyang.impl.ConfigureImpl;

public class ConfigureFactory {
	public static Configure getConfigure() {
		return new ConfigureImpl();
	}
}
