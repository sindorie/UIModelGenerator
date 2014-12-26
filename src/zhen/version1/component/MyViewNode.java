package zhen.version1.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.Window;

public class MyViewNode implements Serializable{
	
	public MyViewNode parent;
	
	public MyViewNode(ViewNode node){
		this.id = node.id;
		this.name = node.name;
		this.hashCode = node.hashCode;
		this.properties = new ArrayList<Property>();
		for(com.android.hierarchyviewerlib.models.ViewNode.Property p : node.properties){
			this.properties.add(new Property(p));
		}
		
		Set<String> keys = node.namedProperties.keySet();
		for(String key : keys){
			this.namedProperties.put(key, new Property(node.namedProperties.get(key)));
		}
		
		this.left = node.left;
		this.top = node.top;
		this.width = node.width;
		this.height = node.height;
		this.scrollX = node.scrollX;
		this.scrollY = node.scrollY;
		this.paddingLeft = node.paddingLeft;
		this.paddingRight = node.paddingRight;
		this.paddingBottom = node.paddingBottom;
		this.paddingTop = node.paddingTop;
		this.marginBottom = node.marginBottom;
		this.marginLeft = node.marginLeft;
		this.marginRight = node.marginRight;
		this.marginTop = node.marginTop;
		this.baseline = node.baseline;
		this.layoutTime = node.layoutTime;
//		this.image = node.image;
		this.viewCount = node.viewCount;
		this.drawTime = node.drawTime;
		this.categories = node.categories;
		this.filtered = node.filtered;
		this.protocolVersion = node.protocolVersion;
		this.willNotDraw = node.willNotDraw;
		this.hasFocus = node.hasFocus;
		this.hasMargins = node.hasMargins;
		this.index = node.index;
	}
	
	public String id;

    public String name;

    public String hashCode;

    public List<Property> properties = new ArrayList<Property>();

    public Map<String, Property> namedProperties = new HashMap<String, Property>();

    public List<MyViewNode> children = new ArrayList<MyViewNode>();

    public int left;

    public int top;

    public int width;

    public int height;

    public int scrollX;

    public int scrollY;

    public int paddingLeft;

    public int paddingRight;

    public int paddingTop;

    public int paddingBottom;

    public int marginLeft;

    public int marginRight;

    public int marginTop;

    public int marginBottom;

    public int baseline;

    public boolean willNotDraw;

    public boolean hasMargins;

    public boolean hasFocus;

    public int index;

    public double measureTime;

    public double layoutTime;

    public double drawTime;

    public Set<String> categories = new TreeSet<String>();

    public Window window;

//    public Image image;

    public int viewCount;

    public boolean filtered;

    public int protocolVersion;
    
    @Override
    public String toString() {
        return name + "@" + hashCode; //$NON-NLS-1$
    }
    
    public static class Property implements Serializable{
    	public Property(){}
    	
    	
    	public Property(com.android.hierarchyviewerlib.models.ViewNode.Property p){
    		this.name = p.name;
    		this.value = p.value;
    	}
    	
        public String name;

        public String value;

        @Override
        public String toString() {
            return name + '=' + value;
        }
    }
}
