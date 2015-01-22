package zhen.version1;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jgrapht.alg.DijkstraShortestPath;

import analysis.StaticInfo;
import staticFamily.StaticApp;
import staticFamily.StaticClass;
import zhen.version1.Support.Bundle;
import zhen.version1.Support.CommandLine;
import zhen.version1.Support.Utility;
import zhen.version1.component.DeviceInformaion;
import zhen.version1.component.Event;
import zhen.version1.component.UIModelGraph;
import zhen.version1.framework.Executer;
import zhen.version1.framework.RunTimeInformation;
import zhen.version1.framework.TraversalEventGenerater;
import zhen.version1.framework.UIExplorer;

/**
 * This class serves the entry point. 
 * Input: 	StaticApp
 * Output:	UIModel 
 * 
 * Explore the UI and try to build UI model. 
 * 
 * @author zhenxu
 *
 */
public class UIModelGenerator {

	private StaticApp app;
	private Executer executer;
	private Bundle bundle;
	private static DeviceInformaion deviceInfo;
	private List<Event> uniqueEventList;
	
	public UIModelGenerator(StaticApp app){
		this.app = app;
	}
	
	private void generate(){
		String serial = null;
		String packageName = app.getPackageName();
		List<StaticClass> actList = app.getActivities();
		List<String> names = new ArrayList<String>();
		for(StaticClass sc :actList ){
			names.add(sc.getJavaName());
		}
		do{
			try { Thread.sleep(100);
			} catch (InterruptedException e) { }
			if(deviceInfo.getPrimaryDevice()!=null){
				serial = deviceInfo.getPrimaryDevice().getSerialNumber();
				break;
			}
		}while(true);
		System.out.println("Device serial: "+serial);
		
		RunTimeInformation rInfo = new RunTimeInformation(packageName,deviceInfo);
		this.executer = new Executer(packageName,serial);
		rInfo.setExecuter(executer);
		TraversalEventGenerater eventGen = new TraversalEventGenerater(names,packageName);
		UIExplorer explore = new UIExplorer(executer, eventGen, rInfo);
		
		explore.traverse();
		
		bundle = new Bundle(rInfo.getUIModel(),rInfo.getMethodEventMap(),rInfo.getEventDeposit());
	}
	
	
	public void buildOrRead(boolean force){
		String name = app.getPackageName();
		String path = Configuration.AppDataDir+"ModelObject/";
		File dir = new File(path);
		dir.mkdirs();

		File target = new File(path+name);
		if(!target.exists() || force){
			System.out.println("building");
			if(deviceInfo == null) deviceInfo = new DeviceInformaion(); 
			this.generate();
			Utility.dumpData(bundle,path+name);
			System.out.println("finished");
		}else{
			System.out.println("loading");
			this.bundle = (Bundle) Utility.restoreData(path+name);
			System.out.println("loaded");
		}
	}
	
	public Executer getExecutor(){
		if(this.executer == null){
			String serial = null;
			System.out.println("getExecutor");
			if(deviceInfo == null) deviceInfo = new DeviceInformaion(); 
			System.out.println("getExecutor deviceInfo");
			do{
				try { Thread.sleep(100);
				} catch (InterruptedException e) { }
				if(deviceInfo.getPrimaryDevice()!=null){
					serial = deviceInfo.getPrimaryDevice().getSerialNumber();
					break;
				}
			}while(true);
			System.out.println("getExecutor serial");
			Executer ex = new Executer(app.getPackageName(), serial);
			this.executer = ex;
			return ex;
		}else{
			return this.executer;
		}
	}
	
	/**
	 * get the graph where UIstates are the vertex and events are the edeges
	 * @return UIModelGraph 
	 */
	public UIModelGraph getUIModel(){
		return (UIModelGraph) this.bundle.os[0];
	}
	/**
	 * get a map between methods and lists of events
	 * Each event in a list triggered the corresponding method during
	 * UI model building. 
	 * @return
	 */
	public Map<String, List<Event>> getMethodEventMap(){
		return (Map<String, List<Event>>) this.bundle.os[1];
	}
	
	/**
	 * get the event deposit which contains all the events in sequential order
	 * other than closing keyboard.
	 * 
	 * @return
	 */
	public List<Event> getEventDeposit(){
		return (List<Event>) this.bundle.os[2];
	}
 
	/**
	 * get a list of unique events
	 * 
	 * Two events are consider equal if they have the same type and the same source UI
	 * and target UI.
	 * @return
	 */
	public List<Event> getUniqueEventList(){
		if(uniqueEventList != null) return uniqueEventList;
		uniqueEventList = new ArrayList<Event>();
		for(Event event: this.getEventDeposit()){
			if(!uniqueEventList.contains(event)){
				uniqueEventList.add(event);
			}
		}
		return uniqueEventList; 
	}
}
