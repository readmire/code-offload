package readmire.offloadinglibrary;

import java.util.ArrayList;

/**
 *
 *    @yasemin
 **/
public class CheckForClass  {
	public  ArrayList<String> offClass;
	public  ArrayList<String> localClass;
	public  ArrayList<String> constructorControl;
	
	public  void initialize(){
		if(offClass==null)
		   offClass = new ArrayList<String>();
		if(localClass==null)
		  localClass = new ArrayList<String>();
		constructorControl = new ArrayList<String>(); 
		localClass.add("Activity");
		localClass.add("OffloadingFramework");
		localClass.add("A_Proxy");
		localClass.add("ActivityThread");
		localClass.add("EyeDentify");		
		localClass.add("ObjectRecognition_Proxy");
		localClass.add("RecognitionResult_Proxy");
//		offClass.add("NonOffloading_Proxy");
		
		constructorControl.add("android.app.AlertDialog");
		constructorControl.add("android.app.AlertDialog$Builder");
	}
	
	public ArrayList<String> getOffClass() {
		return offClass;
	}



	public void setOffClass(ArrayList<String> offClass) {
		this.offClass = offClass;
	}



	public ArrayList<String> getLocalClass() {
		return localClass;
	}



	public void setLocalClass(ArrayList<String> localClass) {
		this.localClass = localClass;
	}



	public ArrayList<String> getConstructorControl() {
		return constructorControl;
	}



	public void setConstructorControl(ArrayList<String> constructorControl) {
		this.constructorControl = constructorControl;
	}



	public boolean checkOffloadble(String name){		
		for(String s :offClass){
			if(s.equals(name)){				
				return true;
			}
		}		
		return false;		
	}
		
	public boolean checkForConstructor(String name){
		boolean flag=false;
		for(String s : constructorControl){
			if(s.equals(name)){
				flag=true;
				return true;
			}
		}		
		return flag;		
	}
	
}
