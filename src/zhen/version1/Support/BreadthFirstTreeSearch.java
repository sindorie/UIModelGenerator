package zhen.version1.Support;

import java.util.ArrayList;
import java.util.List; 

public class BreadthFirstTreeSearch{
	public static interface ChildCollecter <T>{
		public T[] collecter(T parent);
	}
	public static interface CriticalPoint<T>{
		public void onEnQueue(T node);
	}
	
	public static <T> void search(T root, ChildCollecter<T> collecter, CriticalPoint<T> operations){
		List<T> queue = new ArrayList<T>();
		operations.onEnQueue(root);
		queue.add(root);
		while(!queue.isEmpty()){
			T node = queue.remove(0);
			T[] children = collecter.collecter(node);
			if(children == null || children.length == 0) continue;
			for(T child: children){
				operations.onEnQueue(child);
				queue.add(child);
			}
		}
	}
}
