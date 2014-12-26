package zhen.version1.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.ddmlib.IDevice;

import zhen.version1.Configuration;
import zhen.version1.component.Event;
import zhen.version1.component.JDBControl;
import zhen.version1.component.Report;

public class Validation { 
	private List<List<Event>> sequenceList;
	private Report[] record;
	private List<String> targetMethods;
	private Integer count = 0;
	
	public Validation() { 
	}

	public void startValidation(List<List<Event>> sequenceList, List<String> targetMethods) {
		this.sequenceList = sequenceList;
		this.targetMethods = targetMethods;
		
		record = new Report[sequenceList.size()];
		
//		if (frame.traverseExecuter != null){
//			frame.traverseExecuter.terminate();
//			try { Thread.sleep(500); } catch (InterruptedException e1) { }
//		}
		
		List<Thread> list = new ArrayList<Thread>();
//		for (IDevice device : frame.rInfo.getDeviceList()) {
//			list.add(new Thread(new RunExecuter(device.getSerialNumber(), sequenceList)));
//		}

		for (Thread thread : list) { thread.start(); }
		for (Thread thread : list) { try { thread.join(); } catch (InterruptedException e) { e.printStackTrace(); }}
	
	}
	
	public void buildReport(){
		StringBuilder sb = new StringBuilder();
		
		
		List<String>  uHits = JDBControl.uniqueBreakPointHits;
		sb.append("Hits:"+uHits.size()+" / Total:"+targetMethods.size()+"\r\n");
		for(String hit: uHits){
			sb.append(hit+"\r\n");
		}
		 
		int i = 1;
		for(Report single : record){
			sb.append("Sequence #"+i+"\r\n");
			sb.append(Arrays.toString(single.sequence)+"\r\n");
			sb.append(Arrays.toString(single.hits)+"\r\n");
			sb.append("\r\n");
			i+=1;
		}
		
		File output = new File(Configuration.AppDataDir+"Report.txt");
		try {
			PrintWriter pw = new PrintWriter(output);
			pw.print(sb.toString());
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private class RunExecuter implements Runnable{
		private String serial;
		private List<List<Event>> sequenceList;
		private Executer ex;
		public RunExecuter(String serial, List<List<Event>> sequenceList){
			this.serial = serial;
			this.sequenceList = sequenceList;
//			ex = new Executer(frame,serial);
		}
		@Override
		public void run() {
//			ex.init(frame.getAttributes());
			JDBControl jdb = ex.getJdb();
			while(true){
				jdb.clearBreakPointBuffer();
				List<Event> sequence = null;
				int index = 0;
				synchronized(count){
					index = count;
					if(index >= sequenceList.size()) break;
					sequence = sequenceList.get(index);
					count+=1;
				}
				Event[] arr = sequence.toArray(new Event[0]);
				ex.applyEventSequence(arr);
				String[] hits = jdb.getBreakPointHits().toArray(new String[0]);
				record[index] = new Report(arr,hits);
			}
		}
	}
	
}
