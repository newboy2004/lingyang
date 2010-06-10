package com.googlecode.lingyang.future;

import java.util.concurrent.Semaphore;

public class AbstractFuture<T> implements Future<T>{
	T object;
	Semaphore sem=new Semaphore(1);
	@Override
	public T get() {
		synchronized(this){		
			return object;
		}
	}
	protected void setT(T t) {
		synchronized(this){		
			this.object=t;
		}
	}
	@Override
	public void waiting() throws InterruptedException {
		sem.acquire();
	}
	protected void lock() {
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	protected void ready() {
		sem.release();
	}
}
