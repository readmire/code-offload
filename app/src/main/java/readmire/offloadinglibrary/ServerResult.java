package readmire.offloadinglibrary;

import java.io.Serializable;
/**
 *
 *    @yasemin
 **/
public class ServerResult implements Serializable{
	
	String className;
	String methodName;
	//Object proxy;
	//Method method;
	Object[] args;
	Long objectID;
	boolean result;
	byte [] imgbyte;
	boolean hasimage;
	ConstructorParam cp;

    public ServerResult(){
    	
    }
	
	public ServerResult(Long objectID, String className, String methodName , Object[] args, ConstructorParam cp) {
		super();
		this.objectID = objectID;
		this.className = className;
		//this.proxy = proxy;
		//this.method = method;
		this.args = args;
		this.methodName = methodName;
		this.cp = cp;
	}

	
	
	public boolean isHasimage() {
		return hasimage;
	}

    

	public ConstructorParam getCp() {
		return cp;
	}

	public void setCp(ConstructorParam cp) {
		this.cp = cp;
	}

	public void setHasimage(boolean hasimage) {
		this.hasimage = hasimage;
	}



	public byte[] getImgbyte() {
		return imgbyte;
	}



	public void setImgbyte(byte[] imgbyte) {
		this.imgbyte = imgbyte;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public Long getObjectID() {
		return objectID;
	}

	public void setObjectID(Long objectID) {
		this.objectID = objectID;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	

}
