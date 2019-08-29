package readmire.offloadinglibrary;

import android.util.Log;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.Graph;
/**
 *    We have implemented a greedy heuristic
 *    @yasemin
 **/

public class HeuristicSolution2 implements HeuristicSolution {

	final private VertexGroup A, B;
	final private VertexGroup unswappedA, unswappedB; 
	private VertexGroup X1, X2;
	public VertexGroup getGroupA() { return A; }
	public VertexGroup getGroupB() { return B; }
	final private Graph<Vertex, Edge> graph;
	public Graph getGraph() { return graph; }	
	
	HashMap<Vertex, Set<Edge>> hmap;
	ArrayList<Vertex[]> vertexList;
	ArrayList<Vertex[]> vList;	
	Set<Vertex> herlist;
	int gcost = 0;
	BufferedWriter bw;
	Vertex vtemp = null;
	
	public static HeuristicSolution2 processWithGraph(Graph<Vertex,Edge> g) {
	    return new HeuristicSolution2(g);
	}
	
	public class VertexGroup extends HashSet<Vertex> {  
	    public VertexGroup(HashSet<Vertex> clone) { super(clone); }
	    public VertexGroup() { }
	}
	
	public HeuristicSolution2(Graph<Vertex,Edge> g) {
		    graph =g;
		    A = new VertexGroup();
		    B = new VertexGroup();
		    
		    // Split vertices into A and B
		    int i = 0;
		    for (Vertex v : g.vertexSet()) {
		    	if(v.isLocal())
		    	   A.add(v);
		    	else 
		    	   B.add(v);
		     
		    }
		    unswappedA = new VertexGroup(A);
		    unswappedB = new VertexGroup(B);
		    
		    doAllMoves();
	  }

	@Override
	public void setGraph(Graph<Vertex, Edge> graph) {

	}

	public String getHeuristicResult(){
		if(findOffloadingCost()<0){		  
		    return "Greedy Heuristic gain: 0 , Offloaded list: [] , Offloaded Node size: 0";
		}else{
			List<Vertex> list = new ArrayList<Vertex>(getGroupB());
			Collections.sort(list);
			return "Greedy Heuristic gain: "+ findOffloadingCost()+ ", Offloaded list: "+list+", Offloaded Node size: "+getGroupB().size();
		}
	 }
	  
	  public int getOffloadingCost(){		  
		  return findOffloadingCost();
	  }
	  
	  public HashSet<Vertex> getOffloadableClasses(){
		  return getGroupB();
	  }
	
	  private void doAllMoves() {
		    boolean flag = true;
		    int bestGain = findOffloadingCost();
		    Log.v("Best Gain MYH : " , " " + bestGain );
		    do {
		    	 int gain = doSingleMove();
		    	 if(gain > bestGain){
		    		bestGain = gain;
		    		flag = true;
		    	 }else {
		    		 flag = false;
		    		 //reverse last move
		    		 if(A.contains(vtemp)){
		    		   A.remove(vtemp);
					   B.add(vtemp);
		    		 }
		    	 }
		    	
		    }while(flag); 
		    
	   }
	  private int doSingleMove() {		  
		    int cost = 0;
		    int minCost = findOffloadingCost();	
		    vtemp = null;
		   for (Vertex v_b : B) {			     
				cost = findOneVertexMovedCost(graph, v_b);			       
				if(cost > minCost){   
				   minCost = cost;
				   vtemp = v_b;
				}
		   }
		   if(vtemp!=null){
			   unswappedA.add(vtemp);
			   unswappedB.remove(vtemp);
			   A.add(vtemp);
			   B.remove(vtemp);
		   }

		   return findOffloadingCost();
	  } 	
   
    public int findOneVertexMovedCost(Graph<Vertex, Edge> randomGraph,Vertex v){    	

    	X1 = new VertexGroup(A);
    	X2 = new VertexGroup(B);
    	X1.add(v);
    	X2.remove(v);
    	return findTempOffloadingCost();
	}
	
  public int findOffloadingCost(){
 	   int ucost=0; 	   
 	   for(Vertex uv : B){
 	      ucost += findLastResultOfOffloading(uv);
 	   }
 	   return ucost;
    }
   public int findLastResultOfOffloading(Vertex v){
    	int ecost = 0;    	   		
		Set<Edge> edges = graph.edgesOf(v);			
		for(Edge e : edges){
			Vertex vtemp = graph.getEdgeTarget(e);
			if(vtemp.getUid().equals(v.getUid())){
				vtemp = graph.getEdgeSource(e);
			}			
			if(!B.contains(vtemp)){
				ecost += e.getCost();						
			}else {
				ecost += 0;
			}						
		} 
    	return (v.getCost()- ecost);    	
    
    }
   public int findTempOffloadingCost(){
 	   int ucost=0; 	   
 	   for(Vertex uv : X2){
 	      ucost += findLastTempOffloading(uv);
 	   }
 	   return ucost;
    }
    
   public int findLastTempOffloading(Vertex v){
   	int ecost = 0;    	   		
		Set<Edge> edges = graph.edgesOf(v);			
		for(Edge e : edges){
			Vertex vtemp = graph.getEdgeTarget(e);
			if(vtemp.getUid().equals(v.getUid())){
				vtemp = graph.getEdgeSource(e);
			}			
			if(!X2.contains(vtemp)){
				ecost += e.getCost();						
			}else {
				ecost += 0;
			}						
		} 
   	return (v.getCost()- ecost);    	
   
   }
	 public Set<Vertex> getNeighbors(Vertex v, Graph<Vertex,Edge> g){
		  Set<Vertex> vset = new HashSet<Vertex>();
		  for (Edge e: g.edgesOf(v)){
			  Vertex v1 = g.getEdgeTarget(e);
			  if(v1.equals(v))
				  v1=g.getEdgeSource(e);
			  
			  vset.add(v1);		 
		  }
		  
		  return vset;
	  }
	  
	  private double getVertexEdgeCost(Vertex v) {	    
	    double cost = 0;
	    boolean v1isInB = B.contains(v);
	    
	    for (Vertex v2 : getNeighbors(v,graph)) {	    	
	      boolean v2isInB = B.contains(v2);
	      Edge edge = graph.getEdge(v, v2);
	      
	      if (v1isInB != v2isInB) // external
	        cost += edge.getCost();
	      else
	        cost -= edge.getCost();
	    }
	    return cost;
	  }
	  
	  public Pair<Vertex> getEndPoints(Edge e, Graph<Vertex,Edge> g){
		  Vertex first = g.getEdgeSource(e);
		  Vertex second = g.getEdgeTarget(e);	  
		  return new Pair<Vertex>(first, second);
	  }
	  
	  /** Returns the sum of the costs of all edges between A and B **/
	  public double getCutCost() {
	    double cost = 0;

	    for (Edge edge : graph.edgeSet()) {
	      Pair<Vertex> endpoints = getEndPoints(edge,graph);
	      
	      boolean firstInA = A.contains(endpoints.first);
	      boolean secondInA= A.contains(endpoints.second);
	      
	      if (firstInA != secondInA) // external
	        cost += edge.getCost();
	    }
	    return cost;
	  }
	 
	 
	 public class MyVertexComparator2 implements Comparator<Vertex> {
			public int compare(Vertex v1, Vertex v2){
			       return v1.getCost()- v2.getCost();
			}
	}
}

