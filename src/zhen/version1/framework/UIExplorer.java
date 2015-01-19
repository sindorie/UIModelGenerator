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
	
	public static int maxLoop = 100;
	boolean operating = true;
	boolean debug = true; 
	private int currentStep;
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
			Event event = traverser.nextEvent(rInfo);
			if(DEBUG)Utility.log(TAG, "traverse next Event,"+event);
			if(event == null) break;
			executer.applyEvent(event);
			rInfo.update(event);
			currentStep += 1;
			if(currentStep >= maxLoop){
				System.out.println("Max iteration reached");
				break;
			}
		}
	}
	
	/**
	 * check if any restriction is met. e.g. max step 
	 * @return
	 */
	public boolean checkRestriction(){
		if(operating == false) return false;
		else return true;
	}
	
	public void requestStop(){
		this.operating = false;
	}
}
