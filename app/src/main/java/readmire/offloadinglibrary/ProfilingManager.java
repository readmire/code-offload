package readmire.offloadinglibrary;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import org.jgrapht.Graph;
/**
 *
 *    @yasemin
 **/
public class ProfilingManager {

	public static Graph<Vertex,Edge> graph;	
	
	public ProfilingManager() {
		graph = new MyGraph2();
	}

	public static void eraseGraph(){
		graph = new MyGraph();
	}
    public static Graph<Vertex,Edge> getGraph(){
    	return graph;
    }
	public void startProfiling(StackTraceElement[] elements, Method method, Object[] args,
			long elapsed, long sizes,long rsizes, String packageName) {
		
		ArrayList<String> tempMethodList = new ArrayList<String>();		

		for (int i = 0; i < elements.length; i++) {
			// Thread.currentThread().getStackTrace()[i].getMethodName());
			//System.out.println("--"+elements[i].getClassName()+"."+elements[i].getMethodName());			
			if (!filterMethods(elements[i].getMethodName())) {
				 int length = elements[i].getClassName().length();
				//(elements[i].getClassName().substring((length - 5))).equals("Proxy")
				//elements[i].getClassName().equals("android.app.ActivityThread")		
				 
				if ((elements[i].getClassName().substring((length - 5))).equals("Proxy")
						||elements[i].getClassName().equals("android.app.ActivityThread")
						||elements[i].getMethodName().equals("run")) {	
					if (!filterMethods2(elements[i].getMethodName()))  {
						//System.out.println(" xx " + elements[i].getClassName()+ "." + elements[i].getMethodName());						
						  if(!elements[i].getClassName().equals("com.android.internal.os.ZygoteInit$MethodAndArgsCaller")){							
							if(elements[i].getClassName().equals("android.app.ActivityThread")){
								tempMethodList.add(elements[i].getClassName().split("\\.")[2] + "."+ elements[i].getMethodName());						    
							}else if(elements[i].getMethodName().equals("run")){
								try{
								String temp = elements[i].getClassName().split("\\.")[2];
								if(!(temp.equals("Thread")) && !temp.equals("View$PerformClick"))
								    tempMethodList.add(temp.substring(0,temp.indexOf("$")) + "."+ elements[i].getMethodName());	
								}catch(Exception e){
									e.printStackTrace();
								}
							}else{
								String s = elements[i].getMethodName();
								if(!s.contains("super$")){
									//String[] sar = s.split("\\$");									
									//tempMethodList.add(elements[i].getClassName() + "."+ sar[1]);
									//System.out.println(" !! " + elements[i].getClassName()+ "." + sar[1]);
									
								    tempMethodList.add(elements[i].getClassName() + "."+ elements[i].getMethodName());
								   // System.out.println(" !! " + elements[i].getClassName()+ "." + elements[i].getMethodName());	
								}
							}							
						} 
						
						
					}// if filterMethods2	
				}
			}
		}
		//System.out.println("----------------------------------------------------");
		if (!tempMethodList.isEmpty()) {			
			if (tempMethodList.size() <= 1) {
				Vertex v = new Vertex(tempMethodList.get(0).split("\\.")[0]);				
				String edgetemp = tempMethodList.get(0);							
				long ex = 0;
				long melapsedtime = 0;
				Vertex vtemp = getVertexFromGraph(v);
				if(vtemp != null){
					Set<Edge> elist = graph.edgesOf(vtemp);					
					if(!elist.isEmpty()){					
						for(Edge e:elist){
							if(e!=null){
								 if(e.getEcontrol().equals(edgetemp)){
									 ex+=e.getExtime();
								 }
							     
							}
						}
					}					
					melapsedtime = elapsed - ex;
					if(melapsedtime < 0)
						melapsedtime=0;
					//vtemp.setExtime(melapsedtime + vtemp.getExtime());
					 vtemp.setExtime((melapsedtime + vtemp.getExtime())/2);
					
				}else {
					for(String name:OffloadingFactory.getCFC().getLocalClass()){
						if(name.equals(v.getUid()))
							v.setLocal(true);
					}
					v.setExtime(melapsedtime);
					graph.addVertex(v);
				}
			} else {				
				//two nodes
				String r1 = (tempMethodList.get(0).split("\\.")[0]).toString().trim();
				String r2 = (tempMethodList.get(1).split("\\.")[0]).toString().trim();
				
				if(!r1.equals(r2)){				
					String spath="";
					for( int i=0; i<tempMethodList.size();i++){
						if(i!=tempMethodList.size()-1)
						   spath +=tempMethodList.get(i)+":";
						else
						   spath +=tempMethodList.get(i);
					}			
					//System.out.println(("get(0) : "+tempMethodList.get(0) ));					
					ArrayList<Vertex> vlist = new ArrayList<Vertex>();
					vlist.add(new Vertex(r1));
					vlist.add(new Vertex(r2));
					
					//Vertex v1 = new Vertex((tempMethodList.get(1).split("\\.")[0]).toString().trim());
					//Vertex v2 = new Vertex((tempMethodList.get(0).split("\\.")[0]).toString().trim());				
					String edgename=tempMethodList.get(1)+"->"+tempMethodList.get(0);
					//.split("\\.")[0]
					String econtrol;
					int idx = findPrevMethod(tempMethodList);
					if(idx!=-1){
						econtrol = tempMethodList.get(idx);
					}else {
						econtrol = tempMethodList.get(1);
					}					
					long ex = 0;
					long melapsedtime=0;
					//System.out.println("graph.containsVertex(vlist.get(1))"+vlist.get(0).getUid()+vlist.get(0).hashCode() +"-"+ vlist.get(1).getUid()+vlist.get(1).hashCode() + ": "+ graph.containsVertex(vlist.get(1)));
					Vertex vtemp1 = getVertexFromGraph(vlist.get(0));
					Vertex vtemp2 = getVertexFromGraph(vlist.get(1));
					Edge eloop = graph.getEdge(vtemp2, vtemp1);
					// if the method calls other method of the object
					String edgetemp = tempMethodList.get(0);
					if(vtemp1 != null){
						Set<Edge> elist = graph.edgesOf(vtemp1);
						if(elist!=null && !elist.isEmpty()){					
							for(Edge e:elist){
								if(e!=null){
									 String vpth =e.getPath(); 
									 if(vpth!=null && vpth!=""){
								         if(vpth.contains(spath)){
								        	 if(e.getEcontrol().equals(edgetemp)){
								        		 double loopCount=1;
								        		    if(eloop!=null){
								        		    	loopCount = e.getFreq()/(eloop.getFreq()+1);
								        		    	 int loop = (int) (loopCount + 0.5);
								        		    	 if(loop==0) 
										        		    loop = 1;								        		    
										                 ex+=e.getExtime()*loop;
										                
								        		    }else{
								        		    	 //ex+=e.getExtime()*e.getFreq();
														 ex+=e.getExtime();
								        		    }
								        	 }
										           	
								         }
									 }
								     
								}
							}
						}
					}					
					melapsedtime = elapsed - ex;
					if(melapsedtime < 0)
						melapsedtime=0;
					if(vtemp1 != null){					
					   //vtemp1.setExtime(melapsedtime + vtemp1.getExtime());
						vtemp1.setExtime((melapsedtime + vtemp1.getExtime())/2);
						//graph.getVertex(v2).setExtime((melapsedtime + graph.getVertex(v2).getExtime()));				     
					}else{					
						vlist.get(0).setExtime(melapsedtime);
						graph.addVertex(vlist.get(0));
						vtemp1 = getVertexFromGraph(vlist.get(0));
						for(String name :OffloadingFactory.getCFC().getLocalClass()){
							if(name.equals(vtemp1.getUid()))
								vtemp1.setLocal(true);
								
						}
				    }
					//System.out.println("MMM-" + tempMethodList.get(0)+ "- Elapsed time: " + melapsedtime + "- ex :" +ex);						
					
					if (vtemp1 != null && vtemp2 != null) {					
						if (graphContainsEdge(edgename)){	//graph.containsEdge(vtemp1, vtemp2)					
							Edge edge = getEdgeFromGraph(edgename); //(Edge)graph.getEdge(vtemp1, vtemp2);
							edge.setExtime((edge.getExtime() + elapsed)/2);
//							if(edge.getExtime() < elapsed){
//						    	edge.setExtime(elapsed);
//						    }
							edge.setFreq(1+edge.getFreq());
						    
						}else{	
							
							Edge edge = new Edge(edgename, elapsed, sizes, rsizes, spath, econtrol);
							graph.addEdge(vtemp2, vtemp1, edge);
							
						}
						
					} else {
						
						if(vtemp2==null){
					        graph.addVertex(vlist.get(1));
					        vtemp2 = getVertexFromGraph(vlist.get(1));
					        for(String name :OffloadingFactory.getCFC().getLocalClass()){
								if(name.equals(vtemp2.getUid()))
									vtemp2.setLocal(true);
									
							}
						}						
						Edge edge = new Edge(edgename, elapsed, sizes, rsizes, spath,econtrol);
						graph.addEdge(vtemp2, vtemp1, edge);
						//prevExtime = elapsed;
	
					}
					
						
			 }
		  }

		}

	}  // end of startProfiling

	public int findPrevMethod(ArrayList<String> ml){	  
		   int index;
		   boolean last = true;
		   for(int i = 2 ; i < ml.size()-1; i++){
			   String s1 = ml.get(i).split("\\.")[0];
			   String s2 = ml.get(i+1).split("\\.")[0];
			   if(s1.equals(s2)&& s1.equals(ml.get(1).split("\\.")[0])){
				   index = i+1;
				   for(int j = i+1; j<ml.size()-1; j++){
					   if(ml.get(j).split("\\.")[0].equals(ml.get(j+1).split("\\.")[0])){
						   index = j+1;
					   }else{
						   return index;
					   }
				   }
			   }
		   }
		   return -1;
	   }
	
	public boolean graphContainsEdge(String ename){
		   for(Edge et: graph.edgeSet()){
				if(et.getUid().equals(ename))
					 return true;			
			}
			return false;
	}  
	
	public Edge getEdgeFromGraph(String ename){
		   for(Edge et: graph.edgeSet()){
				if(et.getUid().equals(ename))
					 return et;			
			}
			return null;
	}    
	
	public Vertex getVertexFromGraph(Vertex v){
		for(Vertex vt: graph.vertexSet()){
			if(vt.getUid().equals(v.getUid()))
				 return vt;			
		}
		return null;
	}
	
	public static boolean filterMethods(String method) {
		String[] methodlist = new String[] { "getThreadStackTrace",
				"getStackTrace", "invoke","invokeNative", "callSuper",
				"onClick", "performClick", "handleCallback", "dispatchMessage",
				"loop", "hashCode", "<init>","super$" };
		for (int i = 0; i < methodlist.length; i++) {
			if (method.equals(methodlist[i])) {
				return true;
			}
			
		}
		return false;
	}
    
	public static boolean filterMethods2(String method){
		boolean flag = false;		
		if(method.length()> 4 && method.length()<=16){
		  flag = (method.substring((method.length()-4))).equals("void");
		  return flag;
		}else if(method.length()>16){
		   flag = (method.substring((method.length()-16))).equals("java_lang_String");
		   return flag;
		}
		return flag;		
	}
	
	
	
}
