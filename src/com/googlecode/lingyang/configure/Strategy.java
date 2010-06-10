package com.googlecode.lingyang.configure;

import java.nio.channels.Selector;

public enum Strategy {
	next,less;
	protected static int currentIndex=0;
	public int getIndex(Selector[] selectors){
		switch(this){
		case next:
		{			
			return (currentIndex++%selectors.length);
		}
		case less:
		{
			int k=0;
			for(int i=0,j=Integer.MAX_VALUE;i<selectors.length;++i){
				if(j>selectors[i].keys().size()){
					j=selectors[i].keys().size();k=i;
				}
			}
			return k;
		}
		}
		return 0;
	}
}
