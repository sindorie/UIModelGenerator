package zhen.version1.framework;

/**
 * A storage place for Variables being used through the program
 * @author zhenxu
 *
 */
public class Common {
	/**
	 * Strings used for event attributes
	 */
	public static final String event_att_actname = "event_actName";
	public static final String event_att_packname = "event_packageName";
	public static final String event_att_keycode = "event_keycode";
	public static final String event_att_click_x = "event_clickx";
	public static final String event_att_click_y = "event_clicky";
	
	public static final String originalApkPath = "apkPahtOriginal";
	
	public static final String apkPath = "apkPath";
	public static final String packageName = "packageName";
	public static final String apkFile = "apkFile";
	
	/**
	 * Strings used for ViewNode attributes
	 */
	public static final String node_activity_position = "node_position_toActivity";
	public static final String node_actual_position = "node_actual_position";
//	public static final String ui_extra_event = "ui_extra_list";
//	public static final String ui_extra_index = "ui_extra_index";
	
	/**
	 *	predefined sleep duration for diffrent operations
	 */
	public static final long NON_SLEEP = 0;
	public static final long LAUNCH_SLEEP = 2000;
	public static final long RESTART_SLEEP = 2000;
	public static final long REINSTALL_SLEEP = 2000;
	public static final long PRESS_SLEEP = 1500;
	public static final long ONCLICK_SLEEP = 1500;
	
}
