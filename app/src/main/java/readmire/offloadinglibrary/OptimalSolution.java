package readmire.offloadinglibrary;

import android.annotation.SuppressLint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;


/**
 *    We have implemented optimal algorithm
 *    @yasemin
 **/

public class OptimalSolution {

	HashMap<Vertex, Set<Edge>> hmap;
	ArrayList<Vertex[]> vertexList;
	ArrayList<Vertex[]> vList;
	HashMap<Vertex[],Integer> flist;
	
    public String findOptimalResult(Graph<Vertex, Edge> rg){
		Graph<Vertex, Edge> randomGraph = copyOfGraph(rg);
		Set<Vertex> vset = randomGraph.vertexSet();
		flist = new HashMap<Vertex[],Integer>();			
		vertexList = new ArrayList<Vertex[]>();		
		
		Object[] vObjectList = vset.toArray();
		 
		for(int i=1; i <= vObjectList.length;i++){			   
		   combinations2(vObjectList,i,0, new Vertex[i]);				
		}  	
		
		 //System.out.println(vertexList.size()); 
		 
		 for(Vertex[] a : vertexList){
			 //StringBuffer sb = new StringBuffer();
			 int gain = 0;
			 int ara = 0;
			 if(a.length == 1){
				 for(Vertex b:a ){
					// sb.append(" Vertex : "+ b.getUid()+" Cost: "+b.getCost()+ ",");
					 if(!b.isLocal()){					   
					   gain += findOneVertexOffloadCost(randomGraph,b);	
					 }
				 }
	             //System.out.println(sb.toString()+ " Gain: "+ gain );
			 
			 }else if(a.length > 1){				 
				 for(Vertex b:a ){
					 //sb.append(" Vertex : "+ b.getUid()+" Cost: "+b.getCost()+ ",");
					 if(!b.isLocal()){						 
					     gain += findMoreVertexOffloadCost(randomGraph,b,a);
					 }
				 }	              	             
	             
	             //System.out.println(sb.toString()+ " Gain: "+ gain );
	             
			 }

			 flist.put(a, gain);
		 }
		
		 Iterator iter = flist.entrySet().iterator();
		    int t = 0;
		    Vertex[] vfinal = null;
			while (iter.hasNext()) {
				Map.Entry mEntry = (Map.Entry) iter.next();
				if ((Integer)mEntry.getValue() > t ){
					t = (Integer)mEntry.getValue();
					vfinal = (Vertex[]) mEntry.getKey();
				}	
			}
		   int counter = 0;
		   StringBuffer sb = new StringBuffer();
		   if(vfinal!=null){ 
			   for(Vertex v : vfinal){
				   if(!v.isLocal()){
				      sb.append(v.getUid()+ ",");
				      counter++;
				   }
			   }
		       //System.out.println(sb.toString()+ "  Gain : "+ t);
		   }
		   
		   return "Optimal Gain ;"+ t + "; Ofloaded list ;"+ sb.toString() +"; Offloaded Node size ;"+ counter;
   }

	public Graph copyOfGraph(Graph g){
		MyGraph2 gcopy = new MyGraph2();
		MyGraph2 gm = (MyGraph2)g;
		for(Vertex v : gm.vertexSet()){
			Vertex vnew = new Vertex(v.getUid(),v.getId(),v.getCost());
			if(v.isLocal())
				vnew.setLocal(true);
			gcopy.addVertex(vnew);
		}
		for(Edge e : gm.edgeSet()){
			Edge enew = new Edge(e.getUid(),e.getExtime(),e.getArgsize(),e.rsizes,e.getPath(),e.getEcontrol());
			enew.setCost(e.getCost());
			gcopy.addEdge(getVertexById(gcopy,gm.getEdgeSource(e).getUid()),getVertexById(gcopy,gm.getEdgeTarget(e).getUid()),enew);
		}

		return gcopy;
	}

	public Vertex getVertexById(MyGraph2 g, String id){
		for(Vertex v : g.vertexSet()){
			if(v.getUid().equals(id))
				return v;
		}
		return null;
	}
	
    int findOneVertexOffloadCost(Graph<Vertex, Edge> randomGraph, Vertex v){
    	int gain=0;
    	int ecost = 0;
    	if(v.isLocal()){    		
    		//System.out.println(v.getUid()+ " is local (unremotable)");
    		return 0;
    	}else{    		
    		Set<Edge> edges = randomGraph.edgesOf(v);			
			for(Edge e : edges){
				ecost += e.getCost();
			}
    		
    	}
    	gain = v.getCost()-ecost;	
    	//System.out.println("offloaded node : "+v.getUid()+" Kazanc: " +gain);    	
    	return gain;    	
	}
	
    int findMoreVertexOffloadCost(Graph<Vertex, Edge> randomGraph, Vertex v, Vertex[] vlist){
    	int gain=0;
    	int ecost = 0;
    	if(v.isLocal()){    		
    		//System.out.println(v.getUid()+ " is local (unremotable)");
    		return 0;
    	}else{    		
    		Set<Edge> edges = randomGraph.edgesOf(v);			
			for(Edge e : edges){
				boolean flag = false ;
				Vertex vtemp = randomGraph.getEdgeTarget(e);
				if(vtemp.getUid().equals(v.getUid())){
					vtemp = randomGraph.getEdgeSource(e);
				}
				if(vtemp.isLocal()){
					flag = false;
				}else {						
					for(int j = 0; j < vlist.length;j++){						
						//if(!vlist[j].getUid().equals(v.getUid())){						
							if(vtemp.getUid().equals(vlist[j].getUid())){						    	
						    	 flag = true;
							     break;							
						    }
						    else {
						         flag = false;
						    	 //System.out.println("eee "+e.getUid() +" cost " + e.getCost() +" v2[j] "+ v2[j].getUid());
						    }
						    
						//}						
					}
				}
				if(flag){
					ecost += 0;
				}else{
					ecost += e.getCost();
				}
			}
    		
    	}
    	gain = v.getCost() - ecost;	
    	//System.out.println("offloaded node : "+v.getUid()+" Kazanc: " + gain);    	
    	return gain;    	
	}
    
    int findEdgeCost(Graph<Vertex, Edge> randomGraph, Vertex v1, Vertex v2){
    	if(v1.getUid().equals("v1") || v2.getUid().equals("v1")){
    	   return 0;
    	
    	}else {
	    	Edge e = randomGraph.getEdge(v1, v2); 
	    	if(e!=null)
	    	   return e.getCost(); 
	    	else 
	    		return 0;
    	}
    }
	
    @SuppressLint("NewApi")
	void combinations3(Object[] arr, int len, int startPosition, Vertex[] res){
        if (len == 0){
        	vList.add(Arrays.copyOf(res, res.length));        	
            //System.out.println("oo "+ Arrays.toString(res));            
            return;
        }       
        for (int i = startPosition; i <= arr.length-len; i++){
            res[res.length - len] = ((Vertex) arr[i]);
            combinations3(arr, len-1, i+1, res);
        }
		
    }  
    
   @SuppressLint("NewApi")
void combinations2(Object[] arr, int len, int startPosition, Vertex[] result){
        if (len == 0){
        	vertexList.add(Arrays.copyOf(result, result.length));        	
            //System.out.println("o "+ Arrays.toString(result));            
            return;
        }       
        for (int i = startPosition; i <= arr.length-len; i++){
            result[result.length - len] = ((Vertex) arr[i]);
            combinations2(arr, len-1, i+1, result);
        }
		
    }
	
	public List<Edge> outboundNeighbors(Vertex vertex) {
        List<Edge> list = new ArrayList<Edge>();
        if(hmap.containsKey(vertex)){
        for(Edge e: hmap.get(vertex))
            list.add(e);
        }        
        return list;
       
    }
	
	public int getNumberOfEdges(){
        int sum = 0;
        for(Set<Edge> outBounds : hmap.values()){
            sum += outBounds.size();
        }
        return sum;
    }
	
	public List<Vertex> inboundNeighbors(Vertex inboundVertex) {
	        List<Vertex> inList = new ArrayList<Vertex>();
	        for (Vertex to : hmap.keySet()) {
	            for (Edge e : hmap.get(to))
	                if (e.equals(inboundVertex))
	                    inList.add(to);
	        }
	        return inList;
	 }
	
	
	
	
	
}
