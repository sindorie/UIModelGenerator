package zhen.version1.framework;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.alg.DijkstraShortestPath;

import com.android.ddmlib.IDevice;
import com.android.hierarchyviewerlib.models.Window;

import zhen.version1.Configuration;
import zhen.version1.Support.Bundle;
import zhen.version1.Support.Utility;
import zhen.version1.component.Event;
import zhen.version1.component.MyViewNode;
import zhen.version1.component.DeviceInformaion;
import zhen.version1.component.UIModelGraph;
import zhen.version1.component.UIState;
import zhen.version1.component.WindowInformation;
 
/**
 * Responsibility: Keep track of runtime information: Logcat,layout
 * @author zhenxu
 *
 */
public class RunTimeInformation{
	public static boolean DEBUG = true;
	public static final String TAG = "RunTimeInformation";
	private static String winIgnoredList = "Toast";
	private String packageName;
	private Executer executer;

	private DeviceInformaion deviceLayout;

	private UIModelGraph UIModel;
	//a map between methods and lists of events. Each event in a list triggers
	//a corresponding method. 
	private Map<String,List<Event>> methodEventMap = new HashMap<String,List<Event>>();
	//all events which has been applied to the application (excludes closing keyboard)
	private List<Event> eventDeposit = new ArrayList<Event>();
	
	/**
	 * 
	 * @param packageName -- the name of the package
	 * @param deviceInfo 
	 */
	public RunTimeInformation(String packageName, DeviceInformaion deviceInfo){
		this.deviceLayout = deviceInfo;
		this.packageName = packageName;
		this.UIModel = new UIModelGraph();
	}
	
	public void setExecuter(Executer executer){
		this.executer = executer;
	}

	/**
	 * close/release/terminate related component
	 */
	public void terminate(){
		deviceLayout.terminate();
	}
	/**
	 * Enable the GUI for the graph 
	 */
	public void enableGUI(){
		this.UIModel.enableGUI();
	}
	
	/**
	 * get the UIModel being used
	 * @return UIModel
	 */
	public UIModelGraph getUIModel() {
		return UIModel;
	}
	
	/**
	 * set the UIModel 
	 * this method should be used only for restoration
	 * @param input
	 */
	void setUIModel(UIModelGraph input){
		this.UIModel = input;
	}
	
	public List<IDevice> getDeviceList(){
		return this.deviceLayout.getDeviceList();
	}
	
	/**
	 * get the list of events applied to the devices
	 * @return
	 */
	public List<Event> getEventDeposit() {
		return eventDeposit;
	}
	
	void setEventDeposit(List<Event> input){
		this.eventDeposit = input;
	}
	
	/**
	 * dump UIModel, methodEventMap and eventDeposit to file
	 * @param tag		-- used to distinguish between different situations/APK
	 * @param force		-- force to override existed object files
	 */
	public void dumpeData(String tag, boolean force){
		String path = Configuration.AppDataDir+"object/"+tag+"/";
		path = path.replace("//", "");
		File folder = new File(path);
		boolean existed = true;
		if(!folder.exists()){
			folder.mkdirs();
			existed = false;
		}
		
		if(force || !existed){
			Bundle bundle = new Bundle(this.UIModel,this.methodEventMap,this.eventDeposit);
			Utility.dumpData(bundle,path+"bundle1");
		}
	}
	
	/**
	 * 
	 * @param tag	-- same as the dumpData tag
	 * @return		-- if the objects are successfully restored
	 */
	@SuppressWarnings("unchecked")
	public boolean restoreData(String tag){
		try{
			String path = Configuration.AppDataDir+"object/"+tag+"/";
			Bundle bundle = (Bundle) Utility.restoreData(path+"bundle1");
			this.UIModel = (UIModelGraph) bundle.os[0];
			this.methodEventMap = (Map<String, List<Event>>) bundle.os[1];
			this.eventDeposit = (List<Event>) bundle.os[2];
//			this.UIModel = (UIModelGraph) Utility.restoreData(path+"object1");
//			this.methodEventMap = (Map<String, List<Event>>) Utility.restoreData(path+"object2");
//			this.eventDeposit = (List<Event>) Utility.restoreData(path+"object3");
			
			return (this.UIModel!=null) && (this.methodEventMap!=null)&& (this.eventDeposit!=null)/* */;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}catch(Error e){
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Synchronize with the device
	 * and Update necessary information 
	 * including feedback from logcat, layout.
	 * @param lastEvent -- the last event being applied on the device
	 */
	public void update(Event lastEvent){
		if(DEBUG) Utility.log(TAG, "update");
		
		//check if the event needs to be ignored 
		if(lastEvent.getEventType() == Event.iEMPTY){ return; }
		
		List<String> logcatFeedback = Utility.readInstrumentationFeedBack(this.getParimaryDevice().getSerialNumber());
		if(DEBUG) Utility.log(TAG, "readInstrumentationFeedBack finished");
//		if(isErrorPresent(logcatFeedback)){	//TODO
//			onApplicationError();
//		}
		
		WindowInformation[] visibleWindows = WindowInformation.checkVisibleWindowAndCloseKeyBoard(executer);
		
		WindowInformation targetInfo = null;
		
		for(WindowInformation info : visibleWindows){
			if(info.pkgName.equals(this.packageName) && !winIgnoredList.contains(info.name)){
				targetInfo = info;
				break;
			}
		}
		if(DEBUG) Utility.log(TAG, "WindowInformation, "+targetInfo);
		Window[] winList = deviceLayout.getWindowList();
		Window topWin = null;
		if(targetInfo == null){	//which means no app window visible
			topWin = deviceLayout.getFocusedWindow();
		}else{
			//find the first one that has the same name
			for(int index = 0; index < winList.length; index ++){
				Window win = winList[index]; 
				if(win.encode().equals(targetInfo.encode)){
					topWin = win;
					break;
				}
			}
		}
		if(DEBUG) Utility.log(TAG, "topWin, "+topWin);
		//Maybe want to check which is first drawn, focused or top
		UIState previous = this.UIModel.getCurrentState();
		String targetTitle = topWin.getTitle();
		String parts[] = getAppAndActName(targetTitle);
		String appName = parts[0];
		String actName = parts[1];
		
		//get necessary layout 
		int eventType = lastEvent.getEventType();
		switch(eventType){
		case Event.iLAUNCH:
		case Event.iRESTART:
		case Event.iREINSTALL:{
			UIState newState = null;
			if(needRetrieveLayout(topWin,targetInfo)){
				MyViewNode root = deviceLayout.loadWindowData(topWin);
				newState = UIModel.getOrBuildState(appName, actName, root,targetInfo);
			}else{ 
				newState = UIModel.getOrBuildState(appName, actName, null,targetInfo); 
				newState.isInScopeUI = false;
			}
			if(lastEvent.operationCount > 0){
				//which means in which target and source should not be null
				//and check if they meet the expectation. only the target matters because it
				//is a launch event
				if(!previous.equals(lastEvent.getTarget())){
					//does not meet the expectation
					lastEvent = new Event(lastEvent);
				}
			}
			this.UIModel.addLaunch(lastEvent, newState);
		}break;
		case Event.iONCLICK:
		case Event.iPRESS:{
			UIState newState = null;
			if(needRetrieveLayout(topWin,targetInfo)){
				MyViewNode root = deviceLayout.loadWindowData(topWin);
				newState = UIModel.getOrBuildState(appName, actName, root,targetInfo);
			}else{ 
				newState = UIModel.getOrBuildState(appName, actName, null,targetInfo); 
				newState.isInScopeUI = false;
			}
			if(lastEvent.operationCount > 0){
				//which means in which target and source should not be null
				//and check if they meet the expectation. 
				if(!previous.equals(lastEvent.getSource()) || !previous.equals(lastEvent.getTarget())){
					//does not meet the expectation
					lastEvent = new Event(lastEvent);
				}
			}
			this.UIModel.addTransition(lastEvent, newState);
		}break;
		}
		
		int counter = 0;
		List<String> filtered = new ArrayList<String>();
		for(String mes : logcatFeedback){
			if(mes.contains("METHOD_STARTING,")){
				String[] mesPart = mes.split("METHOD_STARTING,");
				String methodSig = mesPart[1].trim();
				filtered.add(methodSig);
				if(counter == 0){
					if(methodEventMap.containsKey(methodSig)){
						List<Event> eventList = methodEventMap.get(methodSig);
						if(!eventList.contains(lastEvent)) eventList.add(lastEvent);
					}else{
						List<Event> eventList = new ArrayList<Event>();
						eventList.add(lastEvent);
						methodEventMap.put(methodSig, eventList);
					}
				}
				counter += 1;
			}else if(mes.contains("METHOD_RETURNING,")){
				counter -= 1;
			}
		}
		lastEvent.addMethodHiets(filtered);
		lastEvent.operationCount += 1;
		eventDeposit.add(lastEvent);
	}
	
	/**
	 * get the current UI which the program believe the device is on.
	 * @return
	 */
	public UIState getCurrentState(){
		return this.UIModel.getCurrentState();
	}
	/**
	 * get an event sequence from source UI to target UI
	 * Both of the UIState must be known in the graph
	 * @param source	
	 * @param target
	 * @return	a list of event 
	 */
	public List<Event> getEventSequence(UIState source, UIState target){
		List<Event> path = DijkstraShortestPath.findPathBetween(this.UIModel.getGraph(), source, target);
		return path;
	}
	/**
	 * get the map which tells a method can be triggered by which events. 
	 * @return
	 */
	public Map<String, List<Event>> getMethodEventMap() {
		return methodEventMap;
	}
	
	void setMethodEventMap(Map<String,List<Event>> input){
		this.methodEventMap = input;
	}
	
	/**
	 * get the sequence of events that applied on the device by far
	 * Note: OnBack due to keyboard is not included
	 * @return
	 */
	public List<Event> getAppliedEventSequence(){
		return this.eventDeposit;
	}
	
	/**
	 * Find the sequence of events that reach the target UI for the first time
	 * @param target
	 * @return
	 */
	public List<Event> locateFirstOccurance(UIState target){
		List<Event> list = new ArrayList<Event>();
		for(int i=0;i<this.eventDeposit.size();i++){
			Event current = eventDeposit.get(i);
			if(current.getEventType() == Event.iEMPTY) continue;
			list.add(current);
			if(current.getSource()!=null && current.getSource().equals(target)){
				return list;
			}
		}
		return null;
	}
	
	/**
	 * only get the very first device connected to the laptop
	 * @return
	 */
	public IDevice getParimaryDevice(){
		return deviceLayout.getPrimaryDevice();
	}
	
	private boolean isErrorPresent(List<String> logcatFeedback){
		//TODO
		return false;
	}
	private void onApplicationError(){
		//TODO
	}
	/**
	 * 
	 * @param target
	 * @param winInfo	- a newly retrieved information for a window in the app
	 * @return
	 */
	private boolean needRetrieveLayout(Window target, WindowInformation winInfo){
		boolean result = winInfo!=null;
		if(DEBUG) Utility.log(TAG, "needRetrieveLayout,"+result);
		return result;
	}
	
	private static String[] getAppAndActName(String msg){ 
		String parts[] = msg.split("/");
		String appName = "";
		String actName = "";
		if(parts.length > 1){
			appName = parts[0];
			actName = parts[1];
		}else{
			actName = parts[0];
		}
		return new String[]{appName,actName};
	}
}
