package lingyang.Factory;

import lingyang.configure.Configure;
import lingyang.impl.ConfigureImpl;

public class ConfigureFactory {
	public static Configure getConfigure() {
		return new ConfigureImpl();
	}
}
