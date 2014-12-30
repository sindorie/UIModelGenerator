package zhen.version1.Support;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream; 
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List; 
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import zhen.version1.Configuration;

public class Utility {
	public static boolean DEBUG = true;
	public static String TAG = "Utility";
	public static List<String> readInstrumentationFeedBack(String serial){
		ArrayList<String> result = new ArrayList<String>();
		/**
		 * Situation that the process never terminated happened.
		 */
//		System.out.println("Input serail: "+serial);
		try{
			String command = Configuration.ADBPath + " -s "+serial+" logcat -v thread -d  -s "+Configuration.InstrumentationTag;
			final Process pc = Runtime.getRuntime().exec(command);
			InputStream in = pc.getInputStream();
			InputStream err = pc.getErrorStream();
			
			StringBuilder sb = new StringBuilder();
			Thread.sleep(300);
			long point1 = System.currentTimeMillis();
			while(true){
				int count = in.available();
				if(count <= 0) break;
				byte[] buf = new byte[count];
				in.read(buf);
				sb.append(new String(buf));
				long point2 = System.currentTimeMillis();
				if(point2 - point1 > 500) break;
			}
			String errMsg = null;
			if(err.available()>0){
				int count = in.available();
				byte[] buf = new byte[count];
				in.read(buf);
				errMsg = new String(buf);
			}
			if(errMsg!=null) System.out.println("DEBUG:errMsg,  "+errMsg);
			
			String tmp = sb.toString();
			if(tmp.trim().equals("")) return result;
			String[] parts = tmp.split("\n");
			if(DEBUG) Utility.log(TAG,tmp);
			for(String part: parts){
				if(part.contains("METHOD_STARTING")){
					String methodName = part.split("METHOD_STARTING,")[1].trim();
					result.add(methodName);
				}
			}
			Utilitylog("\n");
			pc.destroy();
		}catch(Exception e){}
		return result;
	}
	
	
//	public static void main(String[] args){
//		System.out.println("feedback: "+readInstrumentationFeedBack("0794aad2"));
//		System.out.println("feedbakc ends");
//	}
	
	public static void clearLogcat(String serial){
		try {
			Runtime.getRuntime().exec(Configuration.ADBPath + " -s "+serial+" logcat -c").waitFor();
		} catch (InterruptedException | IOException e) { 
			e.printStackTrace();
		}
	}
	
	public static String getUid(String appName){
		String command = Configuration.ADBPath+ " shell dumpsys "+appName+" | grep userId=";
		try {
			Process exec = Runtime.getRuntime().exec(command);
			exec.waitFor();
			InputStream in = exec.getInputStream();
			int count = in.available();
			byte[] buf = new byte[count];
			return new String(buf);
		} catch (IOException e) { 
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readString(InputStream in) {
		int count;
		try {
			count = in.available();
			if (count > 0) {
				byte[] buffer = new byte[count];
				in.read(buffer);
				return new String(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static int findFirstFalse(boolean[] arr){
		for(int i=0;i<arr.length;i++){
			if(arr[i] == false) return i;
		}
		return -1;
	}
	
	public static Logger setupLogger(Class clazz){
		Logger logger = Logger.getLogger(clazz.getName());
		FileHandler fhandler;
		ConsoleHandler chandler;
		try {
			for(Handler handle : logger.getHandlers()){
				handle.setLevel(Level.FINER);
	        }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return logger;
	}
	
	public static void removeFileUnderFolder(String folderName){
		File folder = new File(folderName);
		String[] names = folder.list();
		for(String name:names){
			File afile = new File(folderName+"/"+name);
			afile.delete();
		}
	}
	
	public static void log(String msg){
		System.out.println(msg);
	}
	
	public static void info(String msg){
		System.out.println(msg);
	}
	
	public static void info(String tag, Object input ){
		informationBuilder(tag,input);
	}
	
	public static void log(String tag, Object input){
		informationBuilder(tag,input);
	}
	
	private static void informationBuilder(String tag, Object input){
		int tagSize = 40, msgSize = 200;;
		if(tag.length() > tagSize){
			tag = (String) tag.subSequence(0, tagSize);
		}
		String part1 = String.format("%-"+tagSize+"s", tag);
		String[] part2 = String.format("%-"+msgSize+"s", (input==null?"null":input.toString())).split("\n");
		for(String part: part2){
			String tmp = (part==null)?"":part.trim();
			if(!tmp.equals("")){
				System.out.println(part1+"  "+tmp);
			}
		}
	}
	
	public static boolean checkPortIsAvailable(int port){
		ServerSocket socket = null;
		boolean canUse = false;
		try {
			socket = new ServerSocket(port);
			socket.close();
			canUse=true;
		} catch (IOException e) {
			canUse = false;
		}
		return canUse;
	}

	public static void dumpData(Object towrite, String path){
		FileOutputStream fout;
		ObjectOutputStream oos;
		try {
			fout = new FileOutputStream(path);
			oos = new ObjectOutputStream(fout);
			oos.writeObject(towrite);
			oos.close();
			fout.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			Utility.info(TAG, "dumpDataHelper FileNotFoundException,"+path);
		} catch (IOException e) {
			e.printStackTrace();
			Utility.info(TAG, "dumpDataHelper IOException,"+path);
		}
	}
	
	public static Object restoreData(String path){
		try {
			FileInputStream fin = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fin);
			Object result  = ois.readObject();
			ois.close();
			fin.close();
			return result;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	static PrintWriter pw = null;
	static void Utilitylog(Object o ){
		if(pw == null){
			try {
				pw = new PrintWriter(new File("Utility_log"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		pw.println(o.toString());
		pw.flush();
	}
}
