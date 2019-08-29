package readmire.offloadinglibrary;

import android.util.Log;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.jgrapht.Graph;
/**
 *    We have implemented the Simulated Annealing heuristic
 *    @yasemin
 **/

public class HeuristicSolution2SA implements HeuristicSolution {

    final private VertexGroup A, B;
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
    List<Vertex> mlist=null;
    BufferedWriter bw;
    Vertex vtemp = null;
    Vertex vprev = null;
    Vertex vmprev=null;
    int icount=0;
    Cut c;

    public static HeuristicSolution2SA processWithGraph(Graph<Vertex,Edge> g) {
        return new HeuristicSolution2SA(g);
    }

    public class VertexGroup extends HashSet<Vertex> {
        public VertexGroup(HashSet<Vertex> clone) { super(clone); }
        public VertexGroup() { }
    }

    public HeuristicSolution2SA(Graph<Vertex,Edge> g) {
        graph =copyOfGraph(g);
        A = new VertexGroup();
        B = new VertexGroup();

        // Split vertices into A (includes local nodes) and B
        int i = 0;
        for (Vertex v : g.vertexSet()) {
            if(v.isLocal())
                A.add(v);
            else
                B.add(v);

        }
        mergeLocalVertices2();
        doAllMoves();
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


    public List<String> getBList(){
        List<String> myList=null;
        if(mlist!=null) {
            myList = new ArrayList<>();
            for (Vertex v : mlist) {
                myList.add(v.getUid());
            }
        }
        return myList;
    }

    @Override
    public void setGraph(Graph<Vertex, Edge> graph) {

    }

    public String getHeuristicResult(){
        if(findOffloadingCost()<0){
            return "SA gain: 0 , Offloaded list: [] , Offloaded Node size: 0" ;
        }else{
            mlist = new ArrayList<Vertex>(c.getBestB());
            Collections.sort(mlist);
            return "SA gain: "+ c.getBestGain()+ ", Offloaded list: "+mlist+", Offloaded Node size: "+c.getBestB().size();
        }
    }
    public int getOffloadingCost(){
        return findOffloadingCost();
    }

    public class Cut{
        float bestGain;
        float tempGain;
        public VertexGroup bestB;
        public VertexGroup tempB;

        public Cut(VertexGroup B){
             bestB = new VertexGroup(B);
             tempB = new VertexGroup(B);
        }

        public float getBestGain() {
            return bestGain;
        }

        public void setBestGain(float bestGain) {
            this.bestGain = bestGain;
        }

        public void setBestCut(VertexGroup B){
            bestB = new VertexGroup(B);
        }

        public VertexGroup getBestB(){
            return bestB;
        }

        public float getTempGain() {
            return tempGain;
        }

        public void setTempGain(float tempGain) {
            this.tempGain = tempGain;
        }

        public VertexGroup getTempB() {
            return tempB;
        }

        public void setTempB(VertexGroup tempB) {
            this.tempB = tempB;
        }
    }
    public HashSet<Vertex> getOffloadableClasses(){
        return getGroupB();
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
        // for Callbacks, but now we do not allow loop
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

        A.clear();
        A.add(vlocal);
    }

    private void doAllMoves() {
        float T=10000, Tend = 1;
        float crate = 0.70f;
        boolean flag = true;
        int iteration=0;
        double de=0;
        double pa = 0;
        double prob=0;
        float gainCurrent,gainBest;
        gainBest= gainCurrent = findOffloadingCost();
        Random random = new Random();
        float gainprev=0;
        c = new Cut(B);
        c.setBestGain(gainCurrent);
        Log.v("Best Gain MYH : " , " " + gainCurrent );
        do {
            float gain = doSingleMove(gainCurrent);
            if(gain > gainCurrent){
                gainCurrent = gain;
                if(gain > gainBest) {
                    gainBest = gain;
                    c.setBestCut(B);
                    c.setBestGain(gainBest);
                }
            }else {
                de = gainCurrent - gain;
                pa = Math.exp(-de/T);
                prob = random.nextDouble();
                if(prob < pa){
                    gainCurrent = gain;
                    c.setTempB(B);
                    c.setTempGain(gainCurrent);

                }else {
                    //reverse last move
                    if (A.contains(vtemp) && !vtemp.isLocal()) {
                        A.remove(vtemp);
                        B.add(vtemp);
                    }
                }

            }
            //Log.v("FOLLOWME", "pa : " + pa +   " prob : " + prob  + " Gain : " + gain + " gainTemp : " + gainCurrent);

            T = T*crate;
            iteration++;

        }while(T>Tend && B.size() > 0 );
    }

    public Vertex getVertexById(MyGraph2 g, String id){
        for(Vertex v : g.vertexSet()){
            if(v.getUid().equals(id))
                return v;
        }
        return null;
    }


    private float doSingleMove(float gainCurrent) {
        float cost=0f;
        float tempCost = Float.MIN_VALUE;
        vtemp=null;
        Vertex vrandom=null;
        MyGraph2 g = (MyGraph2)graph;
       for(Vertex v : A){
           Set<Edge>  edges = g.outgoingEdgesOf(v);
           for(Edge e : edges){
               Vertex vm = g.getEdgeTarget(e);
               if (vm != null && !(vm.getUid().equals("vlocal")) && !A.contains(vm)) {
                       cost = findOneVertexMovedCost(g, vm);
                       if (cost >= tempCost) {
                           tempCost = cost;
                           vtemp = vm;
                           //Log.v("VTEMP", vtemp.getUid()+" ");
                       }
               }
           }
       }
       if(vtemp == null || vmprev!= null && vtemp.getUid().equals(vmprev.getUid())) {
           Random rand = new Random();
           int index = rand.nextInt(B.size());
           Iterator<Vertex> iter = B.iterator();
           for (int i = 0; i < index; i++) {
               iter.next();
           }
           vrandom =  iter.next();
           tempCost = findOneVertexMovedCost(g, vrandom);

       }
       vmprev=vtemp;
       if(vrandom!=null)
           vtemp=vrandom;
       if(vtemp!=null){
            A.add(vtemp);
            B.remove(vtemp);
        }


       return tempCost;

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

