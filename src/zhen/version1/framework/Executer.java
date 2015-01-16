package zhen.version1.framework;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException; 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.Paths;
import zhen.version1.Configuration;
import zhen.version1.Support.CommandLine;
import zhen.version1.Support.Utility;
import zhen.version1.component.*;

/**
 * Execution of an event on the device
 * It creates a MonkeyRunner process and send command via
 * the stdin. 
 * @author zhenxu
 *
 */
public class Executer {
	public static final String TAG = "Executer";
	public static final String UP = "MonkeyDevice.UP";
	public static final String DOWN = "MonkeyDevice.DOWN";
	public static final String DOWN_AND_UP = "MonkeyDevice.DOWN_AND_UP";
	public static boolean DEBUG = true;
	
	private Process monkeyProcess = null;
	private BufferedOutputStream ostream = null;
	private BufferedInputStream estream = null;
	private BufferedInputStream istream = null;
	private String serial = null, packageName; 
//	private RunTimeInformation rInfo;
	private JDBControl jdb;
	private List<String> methodSigature;
	
	public Executer(String packageName, String serial){
		this.packageName = packageName; this.serial = serial;
		if(init() == false){
			throw new AssertionError("initialization fails");
		}
	}
	
	public String getSerial(){
		return this.serial;
	}
	
	public void setMethodSigature(List<String> methodSigature) {
		this.methodSigature = methodSigature;
	}

	/**
	 * A convenient way for apply onBack event
	 */
	public void onBack(){
		this.applyEvent(Event.getOnBackEvent());
	}
	
	public void setSerial(String serial){
		this.serial = serial;
	}
	
	/**
	 * Apply an event on the device 
	 * @param event	
	 */
	public void applyEvent(Event event){
		System.out.println("Applying Event");
		if(DEBUG){ Utility.log(TAG, event.toString()); }
		Utility.clearLogcat(serial);
		int type = event.getEventType();
		switch(type){
		case Event.iLAUNCH:{
			String packageName = (String) event.getValue(Common.event_att_packname);
			String actName = (String) event.getValue(Common.event_att_actname);
			String shellCommand = "am start -f 32768 -W -n " + packageName + "/" + actName;
			CommandLine.executeShellCommand(shellCommand, serial);
		}break;
		case Event.iRESTART:{
			String packageName = (String) event.getValue(Common.event_att_packname);
			String actName = (String) event.getValue(Common.event_att_actname);
			
			String stopCommand = "am force-stop "+packageName;
			CommandLine.executeShellCommand(stopCommand, serial);
//			com.example.backupHelper/com.example.backupHelper.BackupActivity
			String launchCommand = "am start -f 32768 -W -n " + packageName + "/" + actName;
			CommandLine.executeShellCommand(launchCommand, serial);
		}break;
		case Event.iREINSTALL:{
			String packageName = (String) event.getValue(Common.event_att_packname);
			String actName = (String) event.getValue(Common.event_att_actname);
//			String uninstallCommand = "pm uninstall -k "+packageName;
//			CommandLine.executeShellCommand(uninstallCommand, serial);
			
			String installCommand = "install -r "+ event.getValue(Common.apkPath);
			CommandLine.executeADBCommand(installCommand, serial);
			
			if(actName != null){
				String launchCommand = "am start  -f  32768 -W -n " + packageName + "/" + actName;
				CommandLine.executeShellCommand(launchCommand, serial);
			}
		}break;
		case Event.iPRESS:{
			String keycode = (String)event.getValue(Common.event_att_keycode);
			this.press(keycode);
		}break;
		case Event.iONCLICK:{
			String x = event.getValue(Common.event_att_click_x).toString();
			String y = event.getValue(Common.event_att_click_y).toString();
			this.click(x, y);
		}break;
		case Event.iUPDATE:{
			//TODO -- should do nothing
		}break;
		case Event.iEMPTY:{
			//TODO -- should do nothing
		}break;
		case Event.iUNDEFINED:
		default: throw new IllegalArgumentException();
		}
		
		try { Thread.sleep(Event.getNeededSleepDuration(type));
		} catch (InterruptedException e) { }
	}
	/**
	 * Apply a list of events,x`
	 * It tries to close the keyboard each time after an event is applied 
	 * @param events
	 */
	public void applyEventSequence(Event[] events){
		for(Event singleEvnet: events){
//			if(jdb!=null) {if(!jdb.isConnected())jdb.connect();}
			this.applyEvent(singleEvnet);
			WindowInformation.checkVisibleWindowAndCloseKeyBoard(this);
		}
	}
	
	public JDBControl getJdb() {
		return jdb;
	}

	/**
	 * get the last event applied
	 * -- DO NOT USE
	 * @return
	 */
	public Event getLastEventApplied(){
		//TODO
		return null;
	}
	/**
	 * initialization function, primarily initialize a MonkeyRunner process
	 * @param attributes -- input attributes
	 * @return
	 */
	public boolean init(){
		if (DEBUG) Utility.log(TAG, "initialization starts");
		try {
			monkeyProcess = Runtime.getRuntime().exec(Configuration.MonkeyLocation);
			ostream = new BufferedOutputStream(monkeyProcess.getOutputStream());
			estream = new BufferedInputStream(monkeyProcess.getErrorStream());
			istream = new BufferedInputStream(monkeyProcess.getInputStream());
			sleepForMonekyReady();
			importLibrary();
			connectDevice();
			
			String s1 = Utility.readString(estream);
			if(s1!= null)Utility.log(TAG, Utility.readString(estream));
			String s2 = Utility.readString(istream);
			if(s2!= null)Utility.log(TAG, Utility.readString(istream));
			jdb = new JDBControl(serial,this.packageName);
			
			if (DEBUG) Utility.log(TAG, "initialization finishes");
		} catch (IOException e) {
			e.printStackTrace();
			try {
				if (ostream != null)
					ostream.close();
				if (estream != null)
					estream.close();
				if (istream != null)
					istream.close();
			} catch (IOException e1) {
			}
			if (monkeyProcess != null)
				monkeyProcess.destroy();
			if (DEBUG) Utility.log(TAG, "initialization fails");
			return false;
		} 
		return true;
	}
	/**
	 * Close/terminate necessary component
	 */
	public void terminate(){
		if (monkeyProcess != null) {
			try {
				ostream.close();
				istream.close();
				estream.close();
			} catch (IOException e) {
			}
			monkeyProcess.destroy();
		}
		if (DEBUG) Utility.log(TAG, "termination finished");
	}

	/** Monkey method **/
	
	
	/**
	 * Click on a device
	 * @param x
	 * @param y
	 */
	public void click(String x, String y) {
		touch(x, y, DOWN_AND_UP);
	}
	/**
	 * Touch on a device 
	 * @param x
	 * @param y
	 * @param type	-- allowed string is defined in this class
	 */
	public void touch(String x, String y, String type) {
		String toWrite = "device.touch(" + x + "," + y + "," + type + ")\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * type msg to the device
	 * @param msg
	 */
	public void type(String msg) {
		String toWrite = "device.type('" + msg + "')\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * wake up device
	 */
	public void wakeDeviceup() {
		sendCommand("device.wake()\n");
	}
	/**
	 * press a key e.g. Home
	 * @param keyCode -- see KeyEvent.KEYCODE_
	 */
	public void press(String keyCode) {
		// http://developer.android.com/reference/android/view/KeyEvent.html
		// all string name begins with "KEYCODE_"
		sendCommand("device.press('" + keyCode + "')\n");
	}
	/**
	 * press a key e.g. Home
	 * @param keyCode -- see KeyEvent.KEYCODE_
	 */
	public void press(int keyCode) {
		sendCommand("device.press('" + keyCode + "')\n");
	}
	/**
	 * ask monkey to sleep for a duration
	 * @param sec
	 */
	public void sleep(int sec) {
		String toWrite = "MonkeyRunner.sleep(" + sec + ")\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * install an application 
	 * @param apkPath 
	 */
	public void install(String apkPath) {
		String toWrite = "device.installPackage('" + apkPath + "')\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void importLibrary() {
		String toWrite = "from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice\n";
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void connectDevice() {
		String toWrite = "device = MonkeyRunner.waitForConnection(60, '"+serial+"')\n";
		if(DEBUG)Utility.log(TAG, "Connecting");
		try {
			ostream.write(toWrite.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(DEBUG)Utility.log(TAG, "Connection Successful");
	}
	private boolean isNotReadyForNextInput(String msg) {
		if (msg == null) {
			return true;
		} else {
			return !msg.trim().endsWith(">>>");
		}
	}
	private void sleepForMonekyReady() {
		String output = null;
		do {
			output = getMonkeyOutput();
			if (DEBUG && output != null) {
				output.replace("\n", "\n\t");
				if (DEBUG)
					Utility.log(TAG, "stdout " + output);
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			String error = getMonkeyError();
			if (error != null) {
				if (DEBUG && output != null) {
					output.replace("\n", "\n\t");
					if (DEBUG)
						Utility.log(TAG, "stderr " + output);
				}
				break;
			}
		} while (isNotReadyForNextInput(output));
	}
	private String getMonkeyOutput() {
		try {
			int count = istream.available();
			if (count > 0) {
				byte[] reading = new byte[count];
				istream.read(reading);
				return new String(reading);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private String getMonkeyError() {
		try {
			int count = estream.available();
			if (count > 0) {
				byte[] reading = new byte[count];
				estream.read(reading);
				return new String(reading);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private void sendCommand(String command) {
		if (DEBUG) Utility.log(TAG, command);
		if (!command.endsWith("\n")) {
			command = command + "\n";
		}
		try {
			ostream.write(command.getBytes());
			ostream.flush();
			sleepForMonekyReady();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
