package readmire.offloadinglibrary;

/**
 *
 *    @yasemin
 **/
public class Edge {

	int ecost;	
    String uid;
    boolean explored = false;
    private int freq;
    private long extime;   
    String path;
    public long argsize;
    public long rsizes;
    String econtrol;

	public long getArgsize() {
		return argsize;
	}

	public void setArgsize(long argsize) {
		this.argsize = argsize;
	}

	public Edge(String name){
    	this.uid = name;
    	ecost = 0; 
    	freq=1;
    }  
    public Edge(String name, int ecost){    	
    	this.uid = name;
    	this.ecost = ecost; 
    	freq=1;
    } 
    
    public Edge(String name, long extime, long argsize, String path, String econtrol){    	
    	this.uid = name;
    	this.ecost = 1;
    	this.extime = extime;
    	this.argsize = argsize;
    	this.path = path;    	
    	this.econtrol = econtrol;
    	freq=1;
    }

	public Edge(String name, long extime, long argsize,long rsizes, String path, String econtrol){
		this.uid = name;
		this.ecost = 1;
		this.extime = extime;
		this.argsize = argsize;
		this.rsizes = rsizes;
		this.path = path;
		this.econtrol = econtrol;
		freq=1;
	}

	public String getEcontrol() {
		return econtrol;
	}

	public void setEcontrol(String econtrol) {
		this.econtrol = econtrol;
	}

	public String getUid() {
		return uid;
	}
    
	public int getFreq() {
		return freq;
	}

	public void setFreq(int freq) {
		this.freq = freq;
	}

	public long getExtime() {
		return extime;
	}

	public void setExtime(long extime) {		
		this.extime = extime;
		ecost = (int) extime;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public boolean isExplored() {
		return explored;
	}
	public void setExplored(boolean explored) {
		this.explored = explored;
	}
	public int getCost() {
		ecost = (int)argsize;
		return ecost;
		
	}
    
	public void setCost(int ecost) {
		this.ecost = ecost;
	}
	
	@Override
	public int hashCode() {		
		final int prime = 31;
	    int result = 1;	  
	    result = prime * result + ((uid == null) ? 0 : uid.hashCode());	 
	    return result;
	}
	
	@Override
	public String toString() {
		return uid;
	}	
	
	@Override
	public boolean equals(Object target) {
		if(target == null){
			return false;
		}
        if(this.uid == ((Edge)target).uid){
       	 return true;
        }else{          
       	 return false;         
        } 
	}
}
