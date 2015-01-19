package zhen.version1.Support;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;

import zhen.version1.Configuration;
 

//	am start: start an Activity.  Options are:
//    -D: enable debugging
//    -W: wait for launch to complete
//    --start-profiler <FILE>: start profiler and send results to <FILE>
//    -P <FILE>: like above, but profiling stops when app goes idle
//    -R: repeat the activity launch <COUNT> times.  Prior to each repeat,
//        the top activity will be finished.
//    -S: force stop the target app before starting the activity
//    --opengl-trace: enable tracing of OpenGL functions
//    --user <USER_ID> | current: Specify which user to run as; if not
//        specified then run as the current user.


public class CommandLine {
	public static boolean DEBUG = false;
	public static String TAG = "CommandLine";

	public static String unlockScreenShellCommand = "input keyevent 82";
	public static String clickPowerButtonShellCommand = "input keyevent KEYCODE_POWER";
	
	public static boolean autoClean = true;
	public static int MaxCount = 100;
//	private static Logger  logger = Utility.setupLogger(CommandLine.class);
	private static Stack<String> outputStack = new Stack<String>(){
		@Override
		public String push(String msg){
			if(autoClean){
				if(this.size() >= MaxCount){
					this.remove(0);
				}
			}
			return super.push(msg);
		}
	};
	private static Stack<String> errStack = new Stack<String>(){
		@Override
		public String push(String msg){
			if(autoClean){
				if(this.size() >= MaxCount){
					this.remove(0);
				}
			}
			return super.push(msg);
		}
	};
	
	public static int executeCommand(String command){
		return executeCommand(command,0);
	}
	
	public static int executeADBCommand(String command, String serial){
		return executeCommand(Configuration.ADBPath+" -s "+serial+" "+command);
	}
	
	public static int executeShellCommand(String command, String serial){
		return executeCommand(Configuration.ADBPath+ " -s "+serial+" shell "+command);
	}
	public static int executeShellCommand(String command){
		return executeCommand(Configuration.ADBPath+ " shell "+command);
	}

	public static int executeCommand(String command , int timeout_ms){
		if(DEBUG) Utility.log(TAG, "executeCommand, "+command);
		Process task = null;
		InputStream stderr = null, stdout = null;
		try {
			task = Runtime.getRuntime().exec(command);
			if(timeout_ms>0){ task.wait(timeout_ms);
			}else{ task.waitFor(); }
			
			stderr = task.getErrorStream();
			int count = stderr.available();
			if(count > 0){
				byte[] buffer = new byte[count];
				stderr.read(buffer);
				String reading = new String(buffer);
				if(DEBUG) Utility.log(TAG, "stderr, "+reading);
				errStack.add(reading);
			}
			stdout = task.getInputStream();
			count = stdout.available();
			if(count > 0){
				byte[] buffer = new byte[count];
				stdout.read(buffer);
				String reading = new String(buffer);
				if(DEBUG) Utility.log(TAG, "stdout, "+reading);
				outputStack.add(reading);
			}
			return task.exitValue();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}
	

	public static String getLatestStdoutMessage(){
		if(outputStack.size() <= 0) return null;
		return outputStack.get(outputStack.size()-1);
	}
	public static String getLatestStderrMessage(){
		if(errStack.size() <= 0) return null;
		return errStack.get(errStack.size()-1);
	}
	public static void clearStdoutBuffer(){
		outputStack.clear();
	}
	public static void clearStderrBuffer(){
		errStack.clear();
	}
}
