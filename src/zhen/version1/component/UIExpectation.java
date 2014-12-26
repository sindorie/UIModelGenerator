package zhen.version1.component;

import zhen.version1.framework.RunTimeInformation;

/**
 * The expectation for the UI after an event applied
 * @author zhenxu
 *
 */
public class UIExpectation {
	public final static String TAG = "UIExpectation";
	public final static int FORCE_FAILURE = -2;
	public final static int FORCE_SUCCESS = -1;
	public final static int SPECIFICUI = 0;
	public final static int INSCOPE = 1;
	
	public final UIState expected;
	private int type;
	
	public UIExpectation(int type, UIState expected){
		this.type = type; this.expected = expected;
	}
	
	/**
	 * Check if the expectation matches with current runtime information
	 * @param info
	 * @return
	 */
	public boolean check(RunTimeInformation info){
		UIState currentState = info.getCurrentState();
		switch(type){
		case FORCE_FAILURE: return false;
		case FORCE_SUCCESS: return true;
		case SPECIFICUI:{
			if(expected == null && currentState == null) return true;
			if(expected != null){
				return expected.equals(currentState);
			}else return false;
		}
		case INSCOPE:{
			return currentState.isInScopeUI;
		}
		}
		return false;
	}
	
	@Override
	public String toString(){
		switch(type){
		case FORCE_FAILURE: return "FORCE_FAILURE";
		case FORCE_SUCCESS: return "FORCE_SUCCESS";
		case SPECIFICUI:{
			return "SPECIFICUI,"+expected;
		}
		case INSCOPE:{
			return "INSCOPE";
		}
		}
		return "UNDEFINED";
	}
}

