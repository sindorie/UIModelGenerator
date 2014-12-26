package zhen.version1.framework;
 
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.Map.Entry;

import zhen.version1.Support.Utility;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;
import zhen.version1.component.WindowInformation;

/**
 * Responsibility: Explore the UI until some condition is satisfied.
 * 	Conditions:
 * 	1.	No new Event could be applied on any layout.
 * 	2.	Max Event count is reached.
 * 	3.	Target layout is reached. 
 * 
 * By default, during traversal, the explorer will try to get back to the previous
 * layout if a known layout is encountered. A flag along with a maximum steps could 
 * be set if the explorer is instructed to keep forwarding all the time.(E.g. treat 
 * onBack the same as others event)
 * 
 * Step control (after each event applied) can be enabled and allows information 
 * inspection. 
 * 
 * 
 * @author zhenxu
 *
 */
public class UIExplorer {
	public static boolean DEBUG = true;
	public static String TAG = "UIExplorer";
	
	boolean operating = true;
	boolean debug = true; 
//	private boolean enableStepControl = false;
	private int maxStep, currentStep;
//	private StepControlCallBack stepControl;
	/**
	 * This is primarily for debug purpose
	 * which allows the user to check information for each loop
	 */
//	public final static StepControlCallBack defaultCallBack = new StepControlCallBack(){
//		private Scanner sc = new Scanner(System.in);
//		@Override
//		public void action(Framework frame) {
//			while(true){
//				String read = sc.nextLine().trim();
//				Utility.info(TAG,"Rcv:"+read);
//				if(read.equals("1")){
//					Stack<UIState> stack = frame.traverser.getUIStack();
//					for(UIState state : stack){
//						Utility.info( TAG, state);
//					}
//				}else if(read.equals("2")){
//					Map<String, List<Event>> map = frame.rInfo.getMethodEventMap();
//					for(Entry<String, List<Event>> entry : map.entrySet()){
//						Utility.info(RunTimeInformation.TAG,entry);
//					}
//				}else if(read.equals("3")){
//					WindowInformation win = frame.rInfo.getUIModel().getCurrentState().winInfo;
//					Utility.info(TAG, win!=null?win.toString():"null");
//				}else if(read.equals("h")){
//					Utility.info(UIExplorer.TAG,"1: show stack, 2: get method map");
//				}else if(read.equals("stop")){
//					frame.explorer.requestStop(); break;
//				}else break;
//			}	
//		}
//	};
	
	private Executer executer;
	private TraversalEventGenerater traverser;
	private RunTimeInformation rInfo;
	public UIExplorer(Executer executer, TraversalEventGenerater traverser, RunTimeInformation rInfo){
		this.executer = executer;
		this.traverser = traverser;
		this.rInfo = rInfo;
	}
	
	/**
	 * ask the program to start traversing
	 */
	public void traverse(){
		this.traverse(-1,false);
	}
	public void traverse(int maxCount){
		this.traverse(maxCount,false);
	}
	/**
	 * DO NOT USE
	 * @param maxStep
	 * @param keepForwarding
	 */
	public void traverse(int maxStep, boolean keepForwarding){
		
		currentStep = 0;
		while(checkRestriction()){
//			if(enableStepControl){ stepControlCallBack();}
			Event event = traverser.nextEvent(rInfo);
			if(DEBUG)Utility.log(TAG, "traverse next Event,"+event);
			if(event == null) break;
			executer.applyEvent(event);
			rInfo.update(event);
			currentStep += 1;
		}
	}
	
//	public boolean reachUIState(UIState state){
////		TraversalEventGenerater traverser = frame.traverser;
////		RunTimeInformation rInfo = frame.rInfo;
////		Executer executer = frame.traverseExecuter;
//		//TODO
////		rInfo.getEventSequence(layout)
//		
//		return false;
//	}
	
	/**
	 * check if any restriction is met. e.g. max step 
	 * @return
	 */
	public boolean checkRestriction(){
		if(maxStep>0 && currentStep>=maxStep)return false; 
		if(operating == false) return false;
		else return true;
	}
	
	public void requestStop(){
		this.operating = false;
	}
	
//	public void enableStepControl(boolean flag){
//		enableStepControl = flag;
//	}
//	public void setStepControlCallBack(StepControlCallBack callback){
//		stepControl = callback;
//	}
//	public static interface StepControlCallBack{
//		public void action(Framework frame);
//	}
	
//	private void stepControlCallBack(){
//		if(this.stepControl == null) return;
//		if(DEBUG) Utility.log(TAG,"UIExplorer Step Control.");
//		stepControl.action(this.frame);
//		if(DEBUG) Utility.log(TAG,"UIExplorer Operation Continues.");
//	}
}
