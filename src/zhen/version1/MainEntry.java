package zhen.version1;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.graph.DefaultListenableGraph;

import staticFamily.StaticApp;
import zhen.version1.component.DeviceInformaion;
import zhen.version1.component.Event;
import zhen.version1.component.UIModelGraph;
import zhen.version1.component.UIState;
import zhen.version1.framework.Common;
import zhen.version1.framework.Executer;
import analysis.StaticInfo;

public class MainEntry {

	public static void main(String[] args){
		
		DeviceInformaion info = new DeviceInformaion();
		System.out.println("here");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println(info.getDeviceList().size());
		
		System.out.println(info.getPrimaryDevice().getSerialNumber());
		if(true) return;
		
		String path = "/home/zhenxu/workspace/APK/CalcA.apk";
		StaticApp app = StaticInfo.initAnalysis(path, false);
		
//		tools.Adb adb = new tools.Adb();
//
//		System.out.println("uninstalling");
//		adb.uninstallApp(app.getPackageName());
//
//		System.out.println("installing");
//		adb.installApp(app.getSignedAppPath());
		 
		
		//Note: 1. The APK needs to be installed and closed before start
		//		2. The library file should be under the current working directory. 
		
		UIModelGenerator builder = new UIModelGenerator(app);
		builder.buildOrRead(true);
		
		UIModelGraph model = builder.getUIModel();
//		model.enableGUI();
		
		System.out.println("getEventDeposit");
		for(Event e: builder.getEventDeposit()){
			System.out.println(e);
		}

		System.out.println("getEventMethodMap");
		for(Entry<String, List<Event>>  entry : builder.getMethodEventMap().entrySet() ){
			System.out.println(entry);
		}
		
//		DefaultListenableGraph<UIState, Event> original = model.getGraph();
//		DefaultListenableGraph<UIState, Event> copy = (DefaultListenableGraph<UIState, Event>) model.getGraph().clone();
//		UIState launcher = model.getKnownVertices().get(0);
		
//		System.out.println("copy.removeVertex(launcher)");
//		copy.removeVertex(launcher);
//		
//		boolean has = original.containsVertex(launcher);
//		System.out.println("original has launcher: "+has);
		
		
		
		
//		Executer executer = builder.getExecutor();
		
//		ExampleProcedure(model, executer);
	}

	static void ExampleProcedure(UIModelGraph model,Executer excuter){
		UIState firstMainUI = model.getFirstMainUIState();
		
		//for an event, go to the layout
		Event currentEvent = null; 
		List<Event>  sequence = model.getEventSequence(firstMainUI, currentEvent.getSource());
		excuter.applyEventSequence(sequence.toArray(new Event[0]));
		
		switch(currentEvent.getEventType()){
		case Event.iONCLICK:{//currently this is the only implemented one other than launching.
			String x = currentEvent.getValue(Common.event_att_click_x).toString();
			String y = currentEvent.getValue(Common.event_att_click_y).toString();
		}
			
		}
		
		
	}
	
}
