package demo;

import lingyang.Factory.ServiceFactory;
import lingyang.err.ChannelException;

public class Test1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ServiceFactory.getSevice().listenAt(88);
		} catch (ChannelException e) {
			e.printStackTrace();
		}
	}

}
