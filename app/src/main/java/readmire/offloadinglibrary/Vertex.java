package readmire.offloadinglibrary;

import java.util.ArrayList;

/**
 *
 *    @yasemin
 **/
public class Vertex implements Comparable<Vertex>{

	String uid;
	int id;
	long extime;
	boolean local = false;
	int vcost;
	String nodelist = "";
	public Vertex(String className) {		
		this.uid = className;		
	}

	public Vertex(String className, int id) {
		this.uid = className;
		this.id=id;
		nodelist =className;
	}

	public Vertex(String className, int id, int cost) {
		this.uid = className;
		this.id=id;
		this.vcost = cost;
		nodelist = className;
	}

	public void addNode(String s){
		nodelist += ";"+s;
	}

	public String getNodelist() {
		return nodelist;
	}

	public String getUid() {
		return uid;
	}

	public int getId() {
		return id;
	}

	public int getCost() {
		return vcost;
	}

	public void setCost(int vcost) {
		this.vcost = vcost;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	public void setId(int id){
		this.id = id;
	}
	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public long getExtime() {
		return extime;
	}

	public void setExtime(long extime) {
		this.extime = extime;
		this.vcost = (int) extime;
	}
	@Override
	public boolean equals(Object v){		
		if(v==null)
		    return false;
		if(this.uid.equals(((Vertex)v).uid))
			return true;
		
		return false;
	}
	
	@Override
    public String toString() {
        return this.uid;
    }
	
	@Override
	public int hashCode()
	{
	    final int prime = 31;
	    int result = 1;	  
	    result = prime * result + ((uid == null) ? 0 : uid.hashCode());	 
	    return result;
	}


	@Override
	public int compareTo(Vertex vertex) {
		return this.id - vertex.id;
	}
}
