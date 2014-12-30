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
	private DeviceInformaion deviceInfo;
	
	public UIModelGenerator(StaticApp app){
		this.app = app;
		deviceInfo = new DeviceInformaion(); 

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
			this.generate();
			Utility.dumpData(bundle,path+name);
			System.out.println("finished");
		}else{
			this.bundle = (Bundle) Utility.restoreData(path+name);
		}
	}
	
	public Executer getExecutor(){
		if(this.executer == null){
			String serial = null;
			do{
				try { Thread.sleep(100);
				} catch (InterruptedException e) { }
				if(deviceInfo.getPrimaryDevice()!=null){
					serial = deviceInfo.getPrimaryDevice().getSerialNumber();
					break;
				}
			}while(true);
			Executer ex = new Executer(app.getPackageName(), serial);
			this.executer = ex;
			return ex;
		}else{
			return this.executer;
		}
	}
	
	public UIModelGraph getUIModel(){
		return (UIModelGraph) this.bundle.os[0];
	}
	public Map<String, List<Event>> getEventMethodMap(){
		return (Map<String, List<Event>>) this.bundle.os[1];
	}
	public List<Event> getEventDeposit(){
		return (List<Event>) this.bundle.os[2];
	}
	

}
