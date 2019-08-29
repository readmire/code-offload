package readmire.offloadinglibrary;

import android.util.Log;

import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *    We have implemented the Min-Cut heuristic (Stoer-Wagner)
 *    @yasemin
 **/

public class HeuristicSolution3MC implements HeuristicSolution {

    private VertexGroup A, B;
    private VertexGroup unswappedA, unswappedB;
    private VertexGroup X1, X2, X1temp, X2temp;
    public VertexGroup getGroupA() { return A; }
    public VertexGroup getGroupB() { return B; }
    final private Graph<Vertex, Edge> graph;
    public Graph getGraph() { return graph; }
    float totalGain;

    HashMap<Vertex, Set<Edge>> hmap;
    ArrayList<Vertex[]> vertexList;
    ArrayList<Vertex[]> vList;
    Set<Vertex> herlist;
    int gcost = 0;
    BufferedWriter bw;
    Vertex vtemp = null;
    private int localcount =0;

    public static HeuristicSolution3MC processWithGraph(Graph<Vertex,Edge> g) {
        return new HeuristicSolution3MC(g);
    }

    public class VertexGroup extends HashSet<Vertex> {
        public VertexGroup(HashSet<Vertex> clone) { super(clone); }
        public VertexGroup() { }
    }

    public HeuristicSolution3MC(Graph<Vertex,Edge> g) {
        graph =copyOfGraph(g);
        A = new VertexGroup();
        B = new VertexGroup();
        // Split vertices into A and B
        int i = 0;
        for (Vertex v : g.vertexSet()) {
            if(v.isLocal()) {
                A.add(v);
                localcount++;
            }else
                B.add(v);

        }
        for(Vertex vt : B){
            totalGain += vt.getCost();
        }
        unswappedA = new VertexGroup(A);
        unswappedB = new VertexGroup(B);
        mergeLocalVertices2();
    }

    public String printGraph(){
         StringBuilder sb = new StringBuilder();
         sb.append("Graph: \n Vertices : ");
         for(Vertex v : graph.vertexSet()){
             sb.append(v.getUid()+"( " + v.getCost()+", "+v.isLocal()+" ), ");
         }
         sb.append("\n Edges : \n");
         for(Edge e : graph.edgeSet()){
            sb.append(e.getUid() + "( " + e.getCost()+" ), ");
         }
      return sb.toString();
    }

    public void mergeLocalVertices2(){
         MyGraph2 g = (MyGraph2)graph;
         Vertex vlocal = new Vertex("vlocal", -1, 0);
         vlocal.setLocal(true);
         g.addVertex(vlocal);
         boolean flag = false;
         int vcost = 0;
         for(Vertex v : A){
             vcost += v.getCost();
         }
         vlocal.setCost(vcost);

         for(Vertex v : B){
           flag = false;
           Set <Edge> edges = g.incomingEdgesOf(v);
           int ecost = 0;
           Set<Edge> redge = new HashSet<Edge>();
           for(Edge e : edges){
               Vertex vk = g.getEdgeSource(e);
               if(A.contains(vk)){
                   ecost += e.getCost();
                   redge.add(e);
                   flag = true;
               }
           }
           g.removeAllEdges(redge);
           if(flag) {
               Edge e = new Edge(vlocal.getUid()+"-"+v.getUid(),ecost);
               e.setArgsize(ecost);
               graph.addEdge(vlocal, v, e);
           }

         }

        for(Vertex v3 : A){
            flag = false;
            Set <Edge> edges = g.incomingEdgesOf(v3);
            int ecost = 0;
            Set<Edge> redge = new HashSet<Edge>();
            for(Edge e : edges){
                Vertex vk = g.getEdgeSource(e);
                if(B.contains(vk)){
                    flag = true;
                    Edge etemp = new Edge(vk.getUid()+"-"+vlocal.getUid(),e.getCost());
                    etemp.setArgsize(e.getCost());
                    graph.addEdge(vk, vlocal, etemp);
                    redge.add(e);
                }
            }
            if(flag)
                g.removeAllEdges(redge);

        }
        for(Vertex v2 : A){
            if(!v2.getUid().equals("vlocal"))
                g.removeVertex(v2);
        }

    }
    public void mergeLocalVertices_OLD(VertexGroup A){
           HashSet<Vertex> vtemplist = new HashSet<>();
           Object[] vlistlocal =  A.toArray();
           Vertex vlocal = new Vertex("vlocal", -1, 0);
           vlocal.setLocal(true);
           graph.addVertex(vlocal);
           for(int i = 0 ; i < vlistlocal.length ; i++){
               for(int j = i+1 ; j< vlistlocal.length ; j++){
                   if(graph.containsEdge((Vertex)vlistlocal[i], (Vertex)vlistlocal[j])){
                       graph.removeEdge((Vertex)vlistlocal[i], (Vertex)vlistlocal[j]);
                       if(vlocal!=null)
                             vlocal.setCost((((Vertex)vlistlocal[i]).getCost() + ((Vertex)vlistlocal[j]).getCost()));

                       Set<Edge> edgeset = graph.edgesOf((Vertex)vlistlocal[i]);
                       Set<Edge> edgeset2 = graph.edgesOf((Vertex)vlistlocal[j]);
                       Object[] m1edge = edgeset.toArray();
                       Object[] m2edge = edgeset2.toArray();
                       boolean flag = false;
                       for(int k = 0 ; k < m1edge.length ; k++){
                           flag=false;
                           for(int m = 0 ; m <m2edge.length;m++ ){
                               if(m1edge[k] !=null && m2edge[m]!=null && (graph.getEdgeTarget((Edge)m1edge[k])).equals(graph.getEdgeTarget((Edge)m2edge[m]))){
                                   Edge e = new Edge("vlocal-"+((Edge)m1edge[k]).getUid().split("-")[1],(((Edge)m1edge[k]).getCost() +((Edge)m2edge[m]).getCost()) );
                                   if(graph.containsEdge(vlocal,graph.getEdgeTarget((Edge) m1edge[k])))
                                       graph.removeEdge(vlocal,graph.getEdgeTarget((Edge) m1edge[k]));
                                   graph.addEdge(vlocal, graph.getEdgeTarget((Edge) m1edge[k]), e);
                                   graph.removeEdge((Edge)m1edge[k]);
                                   graph.removeEdge((Edge)m2edge[m]);
                                   m1edge[k] = null;
                                   m2edge[m] = null;
                                   flag = true;
                               }
                           }

                            if (!flag) {
                                Edge temp = (Edge) m1edge[k];
                                Edge en = new Edge(vlocal.getUid()+"-"+temp.getUid().split("-")[1], temp.getCost());
                                graph.addEdge(vlocal, graph.getEdgeTarget((Edge) m1edge[k]), en);
                            }

                       }
                       for(Object o : m2edge){
                           if(o != null){
                               Edge temp = (Edge)o ;
                               Edge myedge = new Edge(vlocal.getUid()+"-"+graph.getEdgeTarget(temp).getUid(),temp.getCost());
                               graph.addEdge(vlocal,graph.getEdgeTarget(temp), myedge);
                           }
                       }
                       vtemplist.add((Vertex)vlistlocal[i]);
                       vtemplist.add((Vertex)vlistlocal[j]);
                   }else{

                       vlocal.setCost(vlocal.getCost()+((Vertex)vlistlocal[i]).getCost());

                       Set<Edge> edgeset = graph.edgesOf((Vertex)vlistlocal[i]);
                       Set<Edge> edgeset2 = graph.edgesOf((Vertex)vlistlocal[j]);
                       Object[] m1edge = edgeset.toArray();
                       Object[] m2edge = edgeset2.toArray();
                       boolean flag = false;
                       for(int k = 0 ; k < m1edge.length ; k++){
                           flag=false;
                           for(int m = 0 ; m <m2edge.length;m++ ){
                               if(m1edge[k] !=null && m2edge[m]!=null && (graph.getEdgeTarget((Edge)m1edge[k])).equals(graph.getEdgeTarget((Edge)m2edge[m]))){
                                   Edge e = new Edge("vlocal-"+((Edge)m1edge[k]).getUid().split("-")[1],(((Edge)m1edge[k]).getCost() +((Edge)m2edge[m]).getCost()) );
                                   graph.addEdge(vlocal,graph.getEdgeTarget((Edge)m1edge[k]),e);
                                   graph.removeEdge((Edge)m1edge[k]);
                                   graph.removeEdge((Edge)m2edge[m]);
                                   m1edge[k] = null;
                                   m2edge[m] = null;
                                   flag = true;
                               }
                           }

                           if (!flag) {
                               Edge temp = (Edge) m1edge[k];
                               Edge en = new Edge(vlocal.getUid()+"-"+temp.getUid().split("-")[1], temp.getCost());
                               graph.addEdge(vlocal, graph.getEdgeTarget((Edge) m1edge[k]), en);
                           }

                       }
                       for(Object o : m2edge){
                           if(o != null){
                               Edge temp = (Edge)o ;
                               Edge myedge = new Edge(vlocal.getUid()+"-"+graph.getEdgeTarget(temp).getUid(),temp.getCost());
                               graph.addEdge(vlocal,graph.getEdgeTarget(temp), myedge);
                           }
                       }

                       vtemplist.add((Vertex)vlistlocal[i]);

                   }
               }
           }

           for(Vertex v : vtemplist){
               graph.removeVertex(v);
           }
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

    public Cut minCutAlgorithm(){
        //MyGraph2 gmc = (MyGraph2) ((AbstractBaseGraph)graph).clone();
        MyGraph2 gmc = (MyGraph2) copyOfGraph(graph);
        Cut minCut =null,lastCut=null;
        minCut = new Cut(Float.MAX_VALUE,null);
        int iteration=0;
        while(gmc.vertexSet().size()>1){

            X1 = new VertexGroup();
            X2 = new VertexGroup();
            Set<Vertex> vset = gmc.vertexSet();
            for(Vertex mv : vset){
                if(mv.isLocal())
                    X1.add(mv);
                else
                    X2.add(mv);
            }
            lastCut = minCutPhase_SW(gmc, X1, X2);
            if( lastCut.getCost() < minCut.getCost() )
                    minCut = lastCut;

            iteration++;
        }
        //Log.v("ITERATION-", " " + iteration);
       return minCut;
    }

    public static class Cut{
        private float cost;
        private Vertex vt;

        public Cut(float cost, Vertex vt){
            this.cost = cost;
            this.vt = vt;
        }

        public float getCost() {
            return cost;
        }

        public void setCost(float cost) {
            this.cost = cost;
        }

        public Vertex getVt() {
            return vt;
        }

        public void setVt(Vertex vt) {
            this.vt = vt;
        }
    }


    public Cut minCutPhase_SW(MyGraph2 gmc, VertexGroup X1temp,VertexGroup X2temp){

        Vertex vmax, vs, vt = X1.iterator().next();
        vmax = vs = vt;
        int iteration = 0;
        while(X1temp.size() != gmc.vertexSet().size()){
            float max = Float.NEGATIVE_INFINITY;
            float diff = 0;

            for(Vertex v : X1temp){
                Set<Edge> redges = gmc.edgesOf(v);
                for(Edge e : redges){
                    Vertex out = gmc.getEdgeTarget(e);
                    if(out.getUid().equals(v.getUid()))
                        out = gmc.getEdgeSource(e);
                    if(X2temp.contains(out)){
                        diff = e.getCost()-out.getCost();
                        if(max < diff){
                            max = diff;
                            vmax = out;
                        }
                    }
                }
            }
            vs = vt;
            vt = vmax;

            X1temp.add(vmax);
            X2temp.remove(vmax);

           // Log.v("MINCUTPHASE", " " + (iteration++));
        }

        float cutCost = findOneVertexMovedCost2(gmc,vt);
        merge_s_t_vertices(gmc,vs,vt);

        return new Cut(cutCost,vt);

    }

    public void merge_s_t_vertices(MyGraph2 gmc, Vertex vs, Vertex vt){
        vs.setCost(vs.getCost()+vt.getCost());
        if(gmc.containsEdge(vs,vt))
           gmc.removeEdge(vs,vt);
        if(gmc.containsEdge(vt,vs))
            gmc.removeEdge(vt,vs);
        Set<Edge> inEdge = gmc.incomingEdgesOf(vt);
        Set<Edge> outEdge = gmc.outgoingEdgesOf(vt);
        for(Edge e : inEdge){
             Vertex vin = gmc.getEdgeSource(e);
             Edge etemp = new Edge(vin.getUid() + "-" + vs.getUid(), e.getCost());
             if(gmc.containsEdge(vin,vs)){
                 Edge ex = gmc.getEdge(vin,vs);
                 ex.setCost(ex.getCost()+e.getCost());
             }else{
                 gmc.addEdge(vin, vs, etemp);
             }

        }
        for(Edge e : outEdge){
                Vertex vout = gmc.getEdgeTarget(e);
                Edge etemp = new Edge(vs.getUid() + "-" + vout.getUid(), e.getCost());

                gmc.addEdge(vs, vout, etemp);

        }

        vs.addNode(vt.getNodelist());
        gmc.removeVertex(vt);

    }

    public float findOneVertexMovedCost2(MyGraph2 g,Vertex v){
        float ecost = 0;
        for(Edge e : g.edgesOf(v))
            ecost += e.getCost();

        return (totalGain - v.getCost()+ ecost);

    }


    public String getMinCutResult_old(){
       Cut c = minCutAlgorithm();
        String s = "Cost : "+c.getCost();
        s += ", vertex: " + c.getVt().getUid();
        s += ", offlist: " + c.getVt().getNodelist();

        return s;
    }

    List<String> mylist;

    public List<String> getBList(){
        return mylist;
    }

    @Override
    public void setGraph(Graph<Vertex, Edge> graph) {

    }

    public String getHeuristicResult(){
        String result="";
        Cut c = minCutAlgorithm();
        String[] vlist =  c.getVt().getNodelist().split(";");
        boolean flag = false;
        A = new VertexGroup();
        B = new VertexGroup();
        for(Vertex v : graph.vertexSet()){
            flag = false;
            for(String s:vlist ){
                if(s.equals(v.getUid())) {
                    flag=true;
                }
            }
            if(flag)
                B.add(v);
            else
                A.add(v);
        }

        float cost = findOffloadingCost();
        String s  = c.getVt().getNodelist();
        String[] slist   = s.split(";");
        mylist = new ArrayList<String>();
        for(String sm: slist){
            mylist.add(sm);
        }
        Collections.sort(mylist);
        if(cost > 0){
            result = "MinCut gain: "+cost +", "+ mylist +" Offloaded Nodes : " + slist.length;
        }else{
            result = "MinCut gain: 0, Offloaded list: [] , Offloaded Nodes : 0";
        }

        return result;
    }


    public int getOffloadingCost(){
        return findOffloadingCost();
    }

    public HashSet<Vertex> getOffloadableClasses(){
        return getGroupB();
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
