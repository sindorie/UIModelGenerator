package zhen.version1;

import main.Paths;

/**
 * Configuration file stores a list predefined variables
 * @author zhenxu
 *
 */
public class Configuration {
	
	public static String AndroidSDKPath = Paths.androidSDKPath;
	public static String AppDataDir = Paths.appDataDir;
	
	public static String AndroidToolPath = AndroidSDKPath+"sdk/tools/";
	public static String AndroidPlatformToolPath = AndroidSDKPath+"sdk/platform-tools/";
	public static String ADBPath = AndroidPlatformToolPath+"adb";
	public static String AndroidJarPath = "libs/android.jar";
	public static String ApktoolPath = "libs/apktool.jar";
	
	public static String InstrumentationTag = "System.out";//System.out
	
	public static String MonkeyLocation = AndroidToolPath + "monkeyrunner";
}
