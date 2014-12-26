package zhen.version1.Support;

import java.io.Serializable;

public class Bundle implements Serializable{
	public Object[] os;
	public Bundle(Object... os){
		this.os = os;
	}
}
