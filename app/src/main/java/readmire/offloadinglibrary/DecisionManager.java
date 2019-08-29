package readmire.offloadinglibrary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.Graph;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
/**
 *
 *    @yasemin
 **/
public class DecisionManager{
	private static String serverIpAddress = "";
	int speedup;	
	int bandwidth;
	Graph<Vertex,Edge> graph;
	static long sendRequest, receiveRequest,receiveImageResponse, 
	imageSendTime, networkOverhead , serverProcessTime;	
	private static final String TAG = "DependencyGraph";
	
	public DecisionManager() {
	}
	public int getSpeedup() {
		return speedup;
	}
	public void setSpeedup(int speedup) {
		this.speedup = speedup;
	}
	public int getBandwidth() {
		return bandwidth;
	}
	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}
	
	public void calculateBandwitdh(){
	}
    public void startSolver(){
    	
    }
    public void loadOffloadableArray(Activity act){    	
    	Set<Vertex> vset = getOffloadbleClassesForArray();
   	    if(vset!=null && vset.size()>0){
   	     if(OffloadingFactory.getCFC().getOffClass()!=null){
   	    	ArrayList<String> al = new ArrayList<String>();
   	    	for(Vertex v : vset){   	    		
   	    		al.add(v.getUid());
   	    		//System.out.println(v.getUid());
   	    	}
   	    	OffloadingFactory.getCFC().setOffClass(al);
   	     }
   	        //System.out.println("loadOffloadableArray -1");
   	    }else{
   	    	String[] all = getOffloadableClassesFromFile(act);
   	    	if(all != null && all.length>0){
   	    		ArrayList<String> al = new ArrayList<String>();
   	    		for(String s : all){
   	    			al.add(s);
   	    		}
   	    		OffloadingFactory.getCFC().setOffClass(al);
   	    	}  
   	    	//System.out.println("loadOffloadableArray -2");
   	    }
    }
    
    public HashSet<Vertex> getOffloadbleClassesForArray(){
    	graph = ProfilingManager.getGraph();
       if(graph !=null){
    	if(graph.vertexSet().size()>0){
    		HeuristicSolution2 h2 = new HeuristicSolution2(graph);
    		if (h2.getOffloadingCost()>0 && h2.getOffloadableClasses().size()>0){
    			return h2.getOffloadableClasses();
    		}
    	}
      }
    	return null;
    }    
    public String[] getOffloadableClassesFromFile(Activity act){
    	FileInputStream fin;    	
    	String[] all = null;
		try {
			fin = act.openFileInput("OffloadClasses.txt");
			int c;
	        String temp="";
	        while( (c = fin.read()) != -1){
	           temp = temp + Character.toString((char)c);
	        }
	        all = temp.split(";");
	        
	        //System.out.println("getOffloadableClassesFromFile metot is called");
	        return all; 
	        
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    	return null;
    }
    
    public void writeClassesToTextFile(Activity act, String DATA_PATH ){
    	//System.out.println("writeClassesToTextFile method is called");
        graph = ProfilingManager.getGraph();
       if(graph!=null){	
        if(graph.vertexSet().size()>0){
	    	HeuristicSolution2 h2 = new HeuristicSolution2(graph);
	    	StringBuilder sb=null;
	    	int size = 0;
	    	if (h2.getOffloadingCost()>0 && h2.getOffloadableClasses().size()>0){
	    		sb = new StringBuilder();
	    		size = h2.getOffloadableClasses().size();     				
	    		for (Vertex v : h2.getOffloadableClasses()) { 
	    			sb.append(v.getUid()+";");    		    
	    		    if (--size == 0) {
	    		    	sb.append(v.getUid());     		      
	    		    }
	    		}	    		
	    	} 
	    	if(sb!=null){
		         try {
		            FileOutputStream fos = act.openFileOutput("OffloadClasses.txt", Context.MODE_PRIVATE | Context.MODE_WORLD_READABLE);		
		            fos.write(sb.toString().getBytes());		
		            fos.close();		
		            String storageState = Environment.getExternalStorageState();
		            Log.d(TAG, "Storage State: " + storageState);	            
		            if (storageState.equals(Environment.MEDIA_MOUNTED)) {	
		                File ofile = new File(DATA_PATH,"OffloadClasses.txt");
		                Log.d(TAG, "File path: " + DATA_PATH);
		                FileOutputStream fos2 = new FileOutputStream(ofile,false);
		                //fos2.write("\r\n ---------------------------------------- \r\n".getBytes());
		                fos2.write((" " +sb.toString() + "\r\n").getBytes());		                 
		                //fos2.write("\r\n \r\n ---------------------------------------- \r\n".getBytes());
		                fos2.close();		
		            }
		
		        } catch (Exception e) {
		
		            e.printStackTrace();
		
		        }
	         
	      }
    	
        }
      }
    }
    
    public String getOptimalResults(){    	
    	OptimalSolution opsol = new OptimalSolution();
		return opsol.findOptimalResult(graph);
    }
    
    public String getHeuristicResultSA(){
    	HeuristicSolution2SA h1 = new HeuristicSolution2SA(graph);
		return h1.getHeuristicResult();
    }    
    public String getHeuristicResultMC(){
    	HeuristicSolution3MC h2 = new HeuristicSolution3MC (graph);
		return h2.getHeuristicResult();
    }
    
    public String getSolverResult(){
    	graph = ProfilingManager.getGraph();
    	StringBuffer sbb = null;
    	if(graph!=null){
			Set<Edge> eset = graph.edgeSet();
			Set<Vertex> vset = graph.vertexSet();
			sbb = new StringBuffer();
			sbb.append("Vertex size: "+ vset.size()+", ");
			sbb.append("Edge size: "+ eset.size()+", ");
			sbb.append(" {");		
			for(Vertex v1:vset){
				sbb.append(v1.getUid()+":"+v1.getExtime()+" , ");
			}
			sbb.append("}");
			sbb.append(" {");		
			for(Edge e1:eset){
				sbb.append(e1.getUid()+":"+e1.getCost()+","+e1.getFreq()+ " , ");
			}		
			sbb.append("}");	

			sbb.append("\n\n"+getOptimalResults());
			sbb.append("\n\n"+getHeuristicResultSA());
			sbb.append("\n\n"+getHeuristicResultMC());

    	}
		return sbb.toString();
    	
    }    
    
    public long[][] startMeasurement(){
    	long[][] times = new long[100][10];
    	byte[] respond = null;
    	
        byte[] ts=null;
        for(int i = 0 ; i<100;i++){
	          for(int j = 0 ; j<10;j++){
		         byte[] ar = new byte[(i+1)*10240];
		         for(int k = 0 ; k<ar.length;k++){
		        	   ar[k] = (byte) (Math.random()*10);
		         }
	             long start = System.currentTimeMillis();
	             respond = sendByteArray(ar, serverIpAddress); 
	             long elapsed = System.currentTimeMillis()-start;
	             times[i][j] = elapsed;         
        }
      } 
//        if(respond!=null){
//        	for(int i = 0 ; i<respond.length;i++){
//        		System.out.println(respond[i]);
//        	}
        	
//        	ts = new byte[respond.length-5120];
//        	for(int i = 5120,j=0 ;  i< respond.length;i++,j++) {
//    		    System.out.format("%d ", respond[i]);
//    		    ts[j]=respond[i];
//    		 }
//        	  System.out.println();
//        }else{
//        	 System.out.println("respond null geldi ");
//        }
                
//       long tms = bytesToLong(ts);
//       System.out.println(tms);
        return times;
    }   
    
    public byte[] sendByteArray(byte[] picture, String address) {
    	OutputStream out;
    	try {
            Socket clientSocket = new Socket(address, 8877);
            clientSocket.setSoTimeout(10000);
            DataOutputStream dOut = new DataOutputStream(clientSocket.getOutputStream());
            InputStream in = clientSocket.getInputStream();
            
            dOut.writeInt(picture.length); // write length of the message
            dOut.write(picture); 
           

//            out = clientSocket.getOutputStream();
//            BufferedOutputStream bout = new BufferedOutputStream(out);           
//            //BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//            bout.write(picture);
//            bout.flush();
            //clientSocket.shutdownOutput();
            //out.close();            
           
            return getRespond(in);
        }
        catch(IOException ioe) {
            Log.v("test", ioe.getMessage());
        }
        return null;
    }
    
    public byte[] getRespond(InputStream in) {
    	  try {
    		System.out.println("getRespond(InputStream in)");
    		
    		DataInputStream dIn = new DataInputStream(in);
    		byte[] message=null;
    		int length = dIn.readInt();                    // read length of incoming message
    		if(length>0) {
    		    message = new byte[length];
    		    dIn.readFully(message, 0, message.length); // read the message
    		}
    		return message;
//    	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
//    	    byte[] data = new byte[1000*1024];
//    	    int length = 0;
//    	    while ((length = in.read(data))!=-1) {
//    	        System.out.println("okunan data "+length);
//    	    	bout.write(data,0,length);
//    	    }
//    	   
//    	    return bout.toByteArray();
    	      
    	    } catch(IOException ioe) {
    	    	System.out.println(ioe.getMessage()); 
    	   }
    	   return null;
    	 }
    
    
    public byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
	    buffer.putLong(x);
	    return buffer.array();
	}

	public long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}
}
