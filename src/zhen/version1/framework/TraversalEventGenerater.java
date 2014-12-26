package zhen.version1.framework;

import java.util.List;
import java.util.Stack;
import android.view.KeyEvent;
import zhen.version1.Support.Utility;
import zhen.version1.component.Event;
import zhen.version1.component.UIExpectation;
import zhen.version1.component.UIState;

/**
 * An event generator designed to traverse the UIs
 * @author zhenxu
 *
 */
public class TraversalEventGenerater{
	public static boolean DEBUG = true; 
	public static String TAG = "TraversalEventGenerater";
	/**
	 * A set of predefined Expectation
	 */
	private static UIExpectation InScopeExpectation = new UIExpectation(UIExpectation.INSCOPE,null);
	private static UIExpectation ForceFailure = new UIExpectation(UIExpectation.FORCE_FAILURE,null);
	private static UIExpectation ForceSuccess = new UIExpectation(UIExpectation.FORCE_SUCCESS,null);
	private static UIExpectation LauncherExpected = new UIExpectation(UIExpectation.SPECIFICUI,UIState.Launcher);
	
 
	/**
	 * The activity list and the index of the next one
	 */
	private List<String> actNames;
	private int actIndex = -1;
	private String packageName;
	/**
	 * A flag indicates if an activity should be launched
	 * only be set to true at the start of the activity list is empty again
	 */
	private boolean readyForNextActivity = true;
	/**
	 * Reposition event sequence and counter of trials
	 */
	private List<Event> repositionSequence;
	private int repositionCounter = 0;
	/**
	 * The expectation of the next UI 
	 */
	private UIExpectation expectation = LauncherExpected;
	/**
	 * The stack the path which the program has passed;
	 */
	private Stack<UIState> uiStack = new Stack<UIState>();
	
	
	public TraversalEventGenerater(List<String> actList, String packageName) {
		this.actNames = actList; this.packageName = packageName;
		
	}
	
	public void terminate() {}
	
	/**
	 * get the UI stack which store the path the program has previously encountered
	 * @return
	 */
	public  Stack<UIState> getUIStack(){
		return this.uiStack;
	}
	
	/**
	 * generate the next event given the known runtime information
	 * @param info	-- the known information of runtime
	 * @return	the next event
	 */
	public Event nextEvent(RunTimeInformation info){
		Event reuslt;
		if(DEBUG)Utility.log(TAG,"expectation,"+expectation);
		if(DEBUG)Utility.log(TAG,"Current State,"+info.getCurrentState());
		boolean expectationMet = expectation.check(info);
		if(DEBUG)Utility.log(TAG,"expectationMet,"+expectationMet);
		if(expectationMet) reuslt = normalOperation(info);
		else reuslt = reposition(info);
		return reuslt;
	}
	
	private int uiIndex = 0;
	private Event normalOperation(RunTimeInformation info){
		if(DEBUG) Utility.log(TAG, "normalOperation");
		repositionCounter = 0;
		UIState uiList = info.getCurrentState();
		if(DEBUG) Utility.log(TAG, "current state,"+uiList);
		if(readyForNextActivity){
			
			readyForNextActivity = false;
			List<UIState> states = info.getUIModel().getKnownVertices(); 
			
			/**
			 * First check if any further events can be applied on known UIState
			 */
			if(DEBUG) Utility.log(TAG,"Checking known UI");
			while(uiIndex < states.size()){
				UIState ui = states.get(uiIndex);
				uiIndex+=1;
				if(ui.hasNextEvent()){
					if(DEBUG) Utility.log(TAG,"Reposition to "+ui);
					this.uiStack.add(ui);
					this.setUIExpectation(new UIExpectation(UIExpectation.SPECIFICUI,ui));
					return Event.getEmptyEvent();
				}
			}
			if(DEBUG) Utility.log(TAG,"readyForNextActivity,"+readyForNextActivity+"");
			String actName = findNextActivity();
			if(actName == null){ return null;
			}else{ 
				this.setUIExpectation(InScopeExpectation);
				return Event.getLaunchEvent(packageName, actName);
			}
		}else if(uiStack.isEmpty()){
			if(DEBUG) Utility.log(TAG,"uiStack.isEmpty");
			Event next = uiList.getNextPossibleEvent();
			if(next != null){
				uiStack.push(uiList);
				this.setUIExpectation(InScopeExpectation);
				return next;
			}else{
				readyForNextActivity = true;
				return Event.getPressEvent(KeyEvent.KEYCODE_HOME+"");
			}
		}else{
			if(this.uiStack.peek().equals(uiList)){	//on the top
				if(DEBUG) Utility.log(TAG,"uiStack.peek().equals(state)");
				Event next = uiList.getNextPossibleEvent();
				if(next != null){
					this.setUIExpectation(InScopeExpectation);
					return next;
				} else{
					this.uiStack.pop();
					return reposition(info);
				}
			}else if(this.uiStack.contains(uiList)){	//in the stack body
				if(DEBUG) Utility.log(TAG,"uiStack.contains(state)");
				return reposition(info);
			}else if(uiList.isLauncher || !uiList.isInScopeUI){	
				if(DEBUG) Utility.log(TAG,"state.isLauncher || !state.isInScopeUI");
				throw new AssertionError(uiList.isLauncher +"  "+uiList.isInScopeUI);
			}else{	//not within the stack body
				if(DEBUG) Utility.log(TAG,"not within the stack body");
				Event next = uiList.getNextPossibleEvent();
				if(next != null){	// expect a inscope ui
					this.uiStack.push(uiList);
					this.setUIExpectation(InScopeExpectation);
					return next;
				} else return reposition(info);
			}
		}
	}
	
	/**
	 * reposition to the ui which is on the top of the stack 
	 * if no UI in the stack, go to the launcher
	 * @param info
	 * @return
	 */
	private Event reposition(RunTimeInformation info){
		if(DEBUG) Utility.log(TAG,"reposition");
		UIState targetState = null;
		if(uiStack.isEmpty()){ 
			if(DEBUG) Utility.log(TAG,"uiStack.isEmpty()");
			//the expected is launcher, than generate home event
			readyForNextActivity = true;
			this.setUIExpectation(LauncherExpected);
			return Event.getPressEvent(KeyEvent.KEYCODE_HOME+"");
		}else{ targetState = this.uiStack.peek(); }
		
		if(repositionSequence == null || repositionSequence.isEmpty()){
			if(DEBUG) Utility.log(TAG,"repositionSequence == null || repositionSequence.isEmpty()");
			repositionCounter += 1;
			switch(repositionCounter){
			case 1:{	//do on back
				if(DEBUG) Utility.log(TAG,"repositionCounter is 1.");
				UIState current = info.getCurrentState();
				if(current.isOnBackUsed()){
					if(DEBUG) Utility.log(TAG,"isOnBackUsed.");
					List<Event> sequenceFomrCurrent = info.getEventSequence(current, targetState);
					List<Event> sequenceFomrLauncher = info.getEventSequence(UIState.Launcher, targetState);
					List<Event> sequenceUsed = null;
					if(sequenceFomrCurrent == null && sequenceFomrLauncher == null){
						//seems to be impossible to reach the target UI
						return onRepositionFailure();
					}else if(sequenceFomrCurrent != null && sequenceFomrLauncher != null){ 
						if(sequenceFomrCurrent.size() < sequenceFomrLauncher.size()){
							sequenceUsed = sequenceFomrCurrent;
						}else{
							repositionCounter += 1;
							sequenceUsed = sequenceFomrLauncher;
						}
					}else if(sequenceFomrCurrent == null){
						sequenceUsed = sequenceFomrCurrent;
					}else {
						repositionCounter += 1;
						sequenceUsed = sequenceFomrLauncher;
					}
					
					if(DEBUG) Utility.log(TAG, "repositionCounter,"+repositionCounter);
					repositionSequence = sequenceUsed;
					return checkSequence(sequenceUsed,  targetState);
				}else{
					current.setOnBackUsed();
					this.setUIExpectation(new UIExpectation(UIExpectation.SPECIFICUI,targetState));
					return Event.getOnBackEvent();
				}
			}
			case 2:{	//get the sequence
				if(DEBUG) Utility.log(TAG,"repositionCounter is 2");
				repositionSequence = info.getEventSequence(UIState.Launcher, targetState);
				if(DEBUG) Utility.log(TAG,"repositionSequence,"+repositionSequence);
				return checkSequence(repositionSequence,targetState);
			}
			default: return onRepositionFailure();
			}
		}else if(repositionSequence.size() == 1){ 
			if(DEBUG) Utility.log(TAG,"repositionSequence.size() == 1");
			this.setUIExpectation(new UIExpectation(UIExpectation.SPECIFICUI,targetState));
			return repositionSequence.remove(0);
		}else{
			if(DEBUG) Utility.log(TAG,"repositionSequence.size() > 1");
			this.setUIExpectation(ForceFailure);
			return repositionSequence.remove(0);
		}
	}
	
	private Event checkSequence(List<Event> newSequence, UIState targetState){
		if(newSequence.isEmpty()){
			if(DEBUG) Utility.log(TAG,"repositionSequence.isEmpty(), reposition failure");
			//no path can be found
			return onRepositionFailure();
		}else if(newSequence.size() == 1){
			if(DEBUG) Utility.log(TAG,"repositionSequence.size() == 1");
			this.setUIExpectation(new UIExpectation(UIExpectation.SPECIFICUI,targetState));
			Event result = newSequence.remove(0);
			newSequence = null;
			return result;
		}else{
			if(DEBUG) Utility.log(TAG,"repositionSequence processing");
			this.setUIExpectation(ForceFailure);
			return newSequence.remove(0);
		}
	}
	
	
	private Event onRepositionFailure(){
		if(DEBUG) Utility.log(TAG, "onRepositionFailure");
		repositionCounter = 0;
		if(this.uiStack.isEmpty()){
			if(DEBUG) Utility.log(TAG, "uiStack.isEmpty()");
			//the target was launcher
			this.setUIExpectation(LauncherExpected);
			readyForNextActivity = true;
			return Event.getPressEvent(KeyEvent.KEYCODE_HOME+"");
		}else if(this.uiStack.size() == 1){
			if(DEBUG) Utility.log(TAG, "uiStack.size() == 1");
			uiStack.pop();
			this.setUIExpectation(LauncherExpected);
			readyForNextActivity = true;
			return Event.getPressEvent(KeyEvent.KEYCODE_HOME+"");
		}else{
			if(DEBUG) Utility.log(TAG, "uiStack.size() > 1");
			uiStack.pop();
			this.setUIExpectation(new UIExpectation(UIExpectation.SPECIFICUI, uiStack.peek()));
			return Event.getEmptyEvent();
		}
	}
	
	private void setUIExpectation(UIExpectation next){
		if(DEBUG) Utility.log(TAG,"setUIExpectation,"+next);
		this.expectation = next;
	}
 
	private String findNextActivity(){
		actIndex += 1;
		if(actIndex >= actNames.size()) return null;
		else return actNames.get(actIndex); 
	}
	
	
}


//if(repositionSequence.isEmpty()){
//	if(DEBUG) Utility.log(TAG,"repositionSequence.isEmpty(), reposition failure");
//	//no path can be found
//	onRepositionFailure();
//}else if(repositionSequence.size() == 1){
//	if(DEBUG) Utility.log(TAG,"repositionSequence.size() == 1");
//	this.setUIExpectation(new UIExpectation(UIExpectation.SPECIFICUI,targetState));
//	Event result = repositionSequence.remove(0);
//	repositionSequence = null;
//	return result;
//}else{
//	if(DEBUG) Utility.log(TAG,"repositionSequence processing");
//	this.setUIExpectation(ForceFailure);
//	return repositionSequence.remove(0);
//}
