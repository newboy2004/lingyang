package com.google.code.lingyang;

public class Profile {
	public static enum ListenModel {
		CALLBACK, Furtue
	}

	final int c_strategy_current = 0;
	final int c_strategy_pool = 1;
	private boolean keepalive = true;

	private ListenModel listenModel = ListenModel.Furtue;

	private int listenPort = 8080;

	private boolean reuseAddr = true;

	private int strategy = c_strategy_pool;

	private boolean tcp_NoDelay = true;
	
	private int bufSize=1024;
	
	private int idlePeriod=5*60;

	public ListenModel getListenModel() {
		return listenModel;
	}

	public int getListenPort() {
		return listenPort;
	}

	public int getStrategy() {
		return strategy;
	}

	public boolean isKeepalive() {
		return keepalive;
	}

	public boolean isReuseAddr() {
		return reuseAddr;
	}

	public boolean isTcp_NoDelay() {
		return tcp_NoDelay;
	}

	public void setKeepalive(boolean keepalive) {
		this.keepalive = keepalive;
	}

	public void setListenModel(ListenModel listenModel) {
		this.listenModel = listenModel;
	}

	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}

	public void setReuseAddr(boolean reuseAddr) {
		this.reuseAddr = reuseAddr;
	}

	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}

	public void setTcp_NoDelay(boolean tcp_NoDelay) {
		this.tcp_NoDelay = tcp_NoDelay;
	}

	public void setBufSize(int bufSize) {
		this.bufSize = bufSize;
	}

	public int getBufSize() {
		return bufSize;
	}

	public void setIdlePeriod(int idlePeriod) {
		this.idlePeriod = idlePeriod;
	}

	public int getIdlePeriod() {
		return idlePeriod;
	}

}
