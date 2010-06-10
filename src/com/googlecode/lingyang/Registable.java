package com.googlecode.lingyang;

public interface Registable {
	public void register(Service service);
	public Service getContextService();
}
