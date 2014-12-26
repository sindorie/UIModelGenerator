package zhen.version1.component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import zhen.version1.Support.CommandLine;
import zhen.version1.Support.Utility;

public class JDBControl {
	private String serial;
	private boolean connected = false;
	private Object connectionLock = new Object();	//for the flag
	
	private String packageName;
	private static int baseTCP = 20000;
	private static int topTCP = 21000;
	private static int currentTCP = 7000;
	private String sourcePath = "src";
	private int tcpPort;
	private Process JDBProcess = null;
	private OutputStream stdout; 
	private List<String> breakPointHits = new ArrayList<String>();
	public final static List<String> uniqueBreakPointHits = new ArrayList<String>();
	
	public JDBControl(String serial, String packageName){
		this.serial=serial;
		this.packageName = packageName;
	}
	
	public void connect(){
		String shellCommand = "ps |grep " + packageName;
		CommandLine.executeShellCommand(shellCommand, serial);
		String reading = CommandLine.getLatestStdoutMessage();
		Scanner sc = new Scanner(reading);
		String pid = null;
		while(sc.hasNext()){
			String line = sc.nextLine();
			if (!line.endsWith(packageName)) continue;
			String[] parts = line.split(" ");
			for (int i = 1; i < parts.length; i++) {
				if (parts[i].equals(""))	continue;
				pid = parts[i].trim();
				break;
			}
		}
		sc.close();
		if(pid == null) {
			connected=false;
			return;
		}
		
		tcpPort = getAvailableTCPport();
		String forwardCommand = " forward tcp:" + tcpPort + " jdwp:" + pid;
		CommandLine.executeADBCommand(forwardCommand, serial);
		
		String command = "jdb -sourcepath " + sourcePath + " -attach localhost:" + tcpPort;
		try {
			JDBProcess = Runtime.getRuntime().exec(command);
			setupMonitorThread();
			stdout=JDBProcess.getOutputStream();
			connected=true;
		} catch (IOException e) {
			e.printStackTrace();
			connected=false;
		}
	}
	
	public List<String> getBreakPointHits() {
		return breakPointHits;
	}
	
	public void clearBreakPointBuffer(){
		this.breakPointHits.clear();
	}
	
	public void setBreakPointLine(String className, int lineNumber){
		if(!this.isConnected()) return;
		
		try {
			stdout.write(("stop at " + className + ":" + lineNumber + "\n").getBytes());
			stdout.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void setBreakPointMethod(String className, String methodName){
		try {
			stdout.write(("stop in " + className + "." + methodName + "\n").getBytes());
			stdout.flush(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void clearBreakPointLine(String className, int lineNumber) throws Exception{
		stdout.write(("clear " + className + ":" + lineNumber + "\n").getBytes());
		stdout.flush();
	}
	
	public boolean isConnected(){
		synchronized(connectionLock){
			return this.connected;
		}
	}
	
	public void setConnectionStatus(boolean condition){
		synchronized(connectionLock){
			this.connected = condition;
		}
	}
	
	public static int getAvailableTCPport(){
		currentTCP += 1;
		while(!Utility.checkPortIsAvailable(currentTCP)){
			currentTCP += 1;
			if(currentTCP >= topTCP){
				currentTCP = baseTCP;
			}
		}
		return currentTCP;
	}
	
	public void terminate(){
		if(this.JDBProcess != null){
			this.JDBProcess.destroy();
		}
		CommandLine.executeADBCommand("forward --remove tcp:"+tcpPort, serial);
	}
	
	private void setupMonitorThread(){
		Thread thread1 = new Thread(new JDBMonitor(JDBProcess.getErrorStream()));
		Thread thread2 = new Thread(new JDBMonitor(JDBProcess.getInputStream()));
		thread1.start();
		thread2.start();
	}
	
	private class JDBMonitor implements Runnable{

		private BufferedReader stream;
		public JDBMonitor(InputStream stream) {
			this.stream = new BufferedReader(new InputStreamReader(stream));
		}
		
		@Override
		public void run() {
			try {
				String line;
				while ((line = stream.readLine())!=null) {
					if (!line.startsWith("Breakpoint hit: \"")) continue;

					//information retrieving
					String classAndMethod = line.split(",")[1].trim();
					String className = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
					String methodSig = classAndMethod.substring(classAndMethod.lastIndexOf(".")+1, classAndMethod.length());
					int lineNumber = Integer.parseInt(line.split(",")[2].trim().split(" ")[0].split("=")[1]);

					String toadd = className + "," + methodSig + "," + lineNumber;
					breakPointHits.add(toadd);
					synchronized(uniqueBreakPointHits){
						if(!uniqueBreakPointHits.contains(toadd)){
							uniqueBreakPointHits.add(toadd);
						}
					}
				}
			} catch (IOException e) {e.printStackTrace();}
			setConnectionStatus(false);
			CommandLine.executeADBCommand("forward --remove tcp:"+tcpPort, serial);
		}
		
	}
}
