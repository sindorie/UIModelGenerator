package zhen.version1;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import staticFamily.StaticApp;
import zhen.version1.component.Event;
import zhen.version1.component.UIModelGraph;
import zhen.version1.component.UIState;
import zhen.version1.framework.Common;
import zhen.version1.framework.Executer;
import analysis.StaticInfo;

public class MainEntry {

	public static void main(String[] args){

		
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
		builder.buildOrRead(false);
		
		UIModelGraph model = builder.getUIModel();
		model.enableGUI();
		
		System.out.println("getEventDeposit");
		for(Event e: builder.getEventDeposit()){
			System.out.println(e);
		}

		System.out.println("getEventMethodMap");
		for(Entry<String, List<Event>>  entry : builder.getEventMethodMap().entrySet() ){
			System.out.println(entry);
		}
		

		Executer executer = builder.getExecutor();
		
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
