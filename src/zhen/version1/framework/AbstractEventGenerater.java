package zhen.version1.framework;

import java.util.Map;

import zhen.version1.component.Event;
 

public abstract class AbstractEventGenerater {
	protected UIExplorer frame;
	public AbstractEventGenerater(UIExplorer frame){
		this.frame = frame;
	}
	public abstract void init(Map<String, Object> attributes);
	
	public abstract Event nextEvent();
	
	public abstract void terminate();
}
