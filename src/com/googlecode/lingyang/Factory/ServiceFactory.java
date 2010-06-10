package com.googlecode.lingyang.Factory;

import com.googlecode.lingyang.Service;
import com.googlecode.lingyang.err.SelectorException;
import com.googlecode.lingyang.impl.FlowService;

public class ServiceFactory {
	public static Service getSevice() throws SelectorException{
		return new FlowService();
	}
}
