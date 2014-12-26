package zhen.version1.component;


public class Report {
	public final Event[] sequence;
	public final String[] hits;
	
	public Report(Event[] sequence, String[] hits ){
		this.sequence = sequence;
		this.hits = hits;
	}
	
}
