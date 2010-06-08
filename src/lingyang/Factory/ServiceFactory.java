package lingyang.Factory;

import lingyang.Service;
import lingyang.err.SelectorException;
import lingyang.impl.FlowService;

public class ServiceFactory {
	public static Service getSevice() throws SelectorException{
		return new FlowService();
	}
}
