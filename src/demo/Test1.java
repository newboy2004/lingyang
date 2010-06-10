package demo;

import java.io.IOException;

import com.googlecode.lingyang.Factory.ServiceFactory;
import com.googlecode.lingyang.err.ChannelException;
import com.googlecode.lingyang.err.SelectorException;


public class Test1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ServiceFactory.getSevice().listenAt(88);
		} catch (ChannelException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SelectorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
