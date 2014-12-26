package zhen.version1.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;

import com.android.hierarchyviewerlib.models.ViewNode;

import android.view.KeyEvent;
import zhen.version1.framework.Common;
import zhen.version1.framework.Executer;

/**
 * The event class which also plays as edge in the graph 
 * @author zhenxu
 *
 */
public class Event extends DefaultEdge implements Serializable{

	private static final long serialVersionUID = -6904639332119650622L;
	public final static String EMPTY = "empty";
	public final static String UPDATE = "update";
	public final static String UNDEFINED = "undefined";
	public final static String LAUNCH = "launch";
	public final static String RESTART = "restart";
	public final static String REINSTALL = "reinstall";
	public final static String PRESS = "press";
	public final static String ONCLICK = "android:onClick";
	
	public final static int iEMPTY = -3;
	public final static int	iUPDATE = -2;
	public final static int iUNDEFINED = -1;
	public final static int iLAUNCH = 0;
	public final static int iRESTART = 1;
	public final static int iREINSTALL = 2;
	public final static int iPRESS = 3;
	public final static int iONCLICK = 4;
	
	public final Map<String, Object> attributes;
	public int operationCount = 0;
//	public boolean isIgnored = false;
	private static int avaliableIndex = 0;
	private UIState source, target;
	private List<String> methodHits = new ArrayList<String>();
	private boolean isBroken =false;
	private int index;
	private int eventType;
	
	
	private Event(){ 
		attributes = new HashMap<String, Object>(); 
		this.index = avaliableIndex;
		avaliableIndex += 1;
	}
	/**
	 * Copy constructor
	 * @param other
	 */
	public Event(Event other){
		this.eventType = other.eventType;
		//seems that shallow copy is sufficient at this point
		this.attributes = new HashMap<String, Object>(other.attributes);
		this.index = avaliableIndex;
		avaliableIndex += 1;
	}
	
	public Event(Event other,boolean copytree){
		this.eventType = other.eventType;
		//seems that shallow copy is sufficient at this point
		this.attributes = new HashMap<String, Object>(other.attributes);
		this.index = avaliableIndex;
		avaliableIndex += 1;
		this.source =  new UIState(other.source,copytree);
		this.target =  new UIState(other.target,copytree);
	}
	
	@Override
	public int hashCode(){
		return this.getEventType();
	}
	
	public void setVertices(UIState source, UIState target){
		this.source = source; this.target = target;
	}
	@Override
	public UIState getSource() {
		return source;
	}
	public void setSource(UIState source) {
		this.source = source;
	}
	@Override
	public UIState getTarget() {
		return target;
	}
	public void setTarget(UIState target) {
		this.target = target;
	}
	public List<String> getMethodHits() {
		return methodHits;
	}
	public int getEventType() {
		return eventType;
	}
	public void setEventType(int eventType) {
		this.eventType = eventType;
	}
	public Object getValue(String key){
		return this.attributes.get(key);
	}
	public void putValue(String key, Object value){
		this.attributes.put(key, value);
	}
	
	public void addMethodHiets(List<String> hits){
		for(String hit : hits){
			int index = Collections.binarySearch(methodHits, hit);
			if(index < 0){ methodHits.add(hit); }
			Collections.sort(methodHits);
		}
	}
	
	@Override	
	public boolean equals(Object other){
		if(other instanceof Event){
			Event otherEvent = (Event)other;
			if(otherEvent.eventType != this.eventType) return false;
			
			if(this.index == otherEvent.index) return true;
			
			switch(this.eventType){
			case iONCLICK:{
				String xy1 = this.getValue(Common.event_att_click_x)+"" + this.getValue(Common.event_att_click_y);
				String xy2 = otherEvent.getValue(Common.event_att_click_x)+"" + otherEvent.getValue(Common.event_att_click_y);
				if(!xy1.equals(xy2)) return false;
			}
			}
			
			boolean emptySource1 = this.source == null;
			boolean emptySource2 = otherEvent.source == null;
			
			boolean emptyTarget1 = this.target == null;
			boolean emptyTarget2 = otherEvent.target == null;

			if(emptySource1 == emptySource2 && emptyTarget1 == emptyTarget2) return true;
			if(emptySource1 !=  emptySource2) return false;
			if(emptyTarget1 !=  emptyTarget2) return false;
			
			if(emptySource1) return false;
			if(emptyTarget1) return false;
			if(!otherEvent.source.equals(this.source)) return false;
			if(!otherEvent.target.equals(this.target)) return false;
			return true;
		}
		return false;
	}
	@Override
	public String toString(){
		String typename = intToString(eventType);
		String result = "";
		switch(this.eventType){
		case iLAUNCH: 		
		case iRESTART: 	 
		case iREINSTALL:	{
			result = typename+" "+this.getValue(Common.event_att_actname);	
			break;
		}
		case iPRESS: 	 	{
			result = typename + " keycode "+this.getValue(Common.event_att_keycode);
			break;
		}
		case iONCLICK: 	 	{
			result = typename + " "+this.getValue(Common.event_att_click_x)+","+this.getValue(Common.event_att_click_y);
			break;
		}
		case iEMPTY:	
		case iUPDATE:	
		case iUNDEFINED:	{
			result = typename; 
			break;
		}
		}
		if(this.source != null) result += " in "+this.source;
		if(this.target != null) result += " to "+this.target;
		return result;
	}
	
	public static String intToString(int type){
		switch(type){
		case iEMPTY:	return EMPTY;
		case iUPDATE:	return UPDATE;
		case iLAUNCH: 	return LAUNCH;
		case iRESTART: 	return RESTART;
		case iREINSTALL: return REINSTALL;
		case iPRESS: 	return PRESS;
		case iONCLICK: 	return ONCLICK;
		}
		return UNDEFINED;
	}
	public static int stringToint(String eventString){
		if(eventString.equals(EMPTY)){
			return iEMPTY;
		}else if(eventString.equals(UPDATE)){
			return iUPDATE;
		}else if(eventString.equals(LAUNCH)){
			return iLAUNCH;
		}else if(eventString.equals(RESTART)){
			return iRESTART;
		}else if(eventString.equals(REINSTALL)){
			return iREINSTALL;
		}else if(eventString.equals(PRESS)){
			return iPRESS;
		}else if(eventString.equals(ONCLICK)){
			return iONCLICK;
		}else return iUNDEFINED;
	}
	public static long getNeededSleepDuration(int type){
		switch(type){
		case iLAUNCH: 	return Common.LAUNCH_SLEEP;
		case iRESTART: 	return Common.RESTART_SLEEP;
		case iREINSTALL: return Common.REINSTALL_SLEEP;
		case iPRESS: 	return Common.PRESS_SLEEP;
		case iONCLICK: 	return Common.ONCLICK_SLEEP;
		case iEMPTY:
		case iUPDATE:	
		case iUNDEFINED:
		default: return Common.NON_SLEEP;
		}
	}
	
	public static Event getRestartEvent(String appName, String actName){
		Event result = new Event();
		result.eventType = Event.iRESTART;
		result.putValue(Common.event_att_packname, appName);
		result.putValue(Common.event_att_actname, actName);
		return result;
	}
	
	public static Event getReinstallEvent(String apkPath){
		Event result = new Event();
		result.eventType = Event.iREINSTALL;
		result.putValue(Common.apkPath, apkPath);
		return result;
	}
	
	public static Event getReinstallEventWithLaunch(String apkPath, String appName, String actName){
		Event result = new Event();
		result.eventType = Event.iREINSTALL;
		result.putValue(Common.apkPath, apkPath);
		result.putValue(Common.event_att_packname, appName);
		result.putValue(Common.event_att_actname, actName);
		return result;
	}
	
	public static Event getOnBackEvent(){
		Event result = new Event();
		result.eventType = Event.iPRESS;
		result.putValue(Common.event_att_keycode, KeyEvent.KEYCODE_BACK+"");
		return result;
	}
	public static Event getLaunchEvent(String appName, String actName){
		Event result = new Event();
		result.eventType = Event.iLAUNCH;
		result.putValue(Common.event_att_packname, appName);
		result.putValue(Common.event_att_actname, actName);
		return result;
	}
	
	public static Event getEmptyEvent(){
		Event result = new Event();
		result.eventType = Event.iEMPTY;
		return result;
	}
	
	public static Event getOnClickEvent(String x, String y){
		Event result = new Event();
		result.eventType = Event.iONCLICK;
		result.putValue(Common.event_att_click_x, x);
		result.putValue(Common.event_att_click_y, y);
		return result;
	}
	public static Event getOnClickEvent(int x, int y){
		Event result = new Event();
		result.eventType = Event.iONCLICK;
		result.putValue(Common.event_att_click_x, x+"");
		result.putValue(Common.event_att_click_y, y+"");
		return result;
	}

	public static Event getPressEvent(String keyCode){
		Event result = new Event();
		result.eventType = Event.iPRESS;
		result.putValue(Common.event_att_keycode,keyCode); 
		return result;
	}
}
