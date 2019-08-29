package readmire.offloadingframework;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import org.jgrapht.Graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import readmire.offloadinglibrary.Edge;
import readmire.offloadinglibrary.HeuristicSolution2SA;
import readmire.offloadinglibrary.HeuristicSolution3MC;
import readmire.offloadinglibrary.MyGraph2;
import readmire.offloadinglibrary.OptimalSolution;
import readmire.offloadinglibrary.Vertex;
/**
 *
 *    @yasemin
 *    Random graphs including different number of tasks/nodes are created to test partitioning algorithms.
 **/
public class TestRandomGraph extends AppCompatActivity {
    TextView vertex,edge;
    CheckBox copt;
    boolean optFlag;
    Button test;
    EditText gsize;
    int GRAPH_SIZE;
    Graph g;
    HashSet<Vertex>  kontrol;
    StringBuilder sb1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_randomcut);
       gsize = findViewById(R.id.etSize);
       vertex  = findViewById(R.id.txtVertex);
       edge  = findViewById(R.id.txtEdge);
       test = findViewById(R.id.btnTest);
       copt = findViewById(R.id.checkBox);
       gsize.clearFocus();
        kontrol = new HashSet<>();
       test.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               test_Click(view);
           }
       });

       copt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked)
                    optFlag=true;
                else
                    optFlag=false;
           }
        });
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
            Edge enew = new Edge(e.getUid(),e.getCost());
            enew.setArgsize(e.getCost());
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


    public void test_Click(View view){

        try {

            Graph g  =  createRandomGraph();
            Long start = System.currentTimeMillis();

            HeuristicSolution2SA h2 = new HeuristicSolution2SA(g);
            String result = h2.getHeuristicResult();

            Long elapsed = System.currentTimeMillis() - start;
            Log.v("Simulated An", elapsed + " _______________________");

            //edge.setText(result);

            /*
            OptimalSolution opsol = new OptimalSolution();
            String opt = opsol.findOptimalResult(g);
            */
          Log.v("FOLLOW--", "Start min cut heuristic");
            Long start2 = System.currentTimeMillis();
            HeuristicSolution3MC h4 = new HeuristicSolution3MC(g);
            String result4 = h4.getHeuristicResult();

            Long elapsed2 = System.currentTimeMillis() - start2;
            Log.v("MinCut Alg", elapsed2 + " _______________________");

            edge.setText(result + "\n" + result4 + "\n");
            //edge.setText(result4 + "\n");
            if(!optFlag)
                edge.setText(result + "\n" + result4 + "\n");
            else{
                OptimalSolution opsol = new OptimalSolution();
                String resultopt = opsol.findOptimalResult(g);
                edge.setText(result + "\n" + result4 + "\n" + resultopt);

            }
            StringBuilder ms = new StringBuilder();
            List<String> b1 = h2.getBList();
            List<String> b2 = h4.getBList();

            ms.append("Diff : ");
            if(b1!=null && b2!=null) {
                for (String s : b1) {
                    if (!b2.contains(s)) {
                        ms.append(s);
                    }
                }
            }
            ms.append("---");
            if(b1!=null && b2!=null) {
                for (String s2 : b2) {
                    if (!b1.contains(s2)) {
                        ms.append(s2);
                    }
                }
            }
            vertex.setText("Simulated Annealing : " + elapsed + "\nMinCut Heuristic :" + elapsed2  + "\n" + ms.toString());


        }catch(Exception ex){
            Log.v("Exception on test:" , ex.getMessage());
        }

    }
    public Graph createRandomGraph(){
        boolean flag = false;
        Random r = new Random(System.nanoTime());
        g = new MyGraph2();
        String size = gsize.getText().toString();
        if (size != null && size.length() > 0) {
            GRAPH_SIZE = Integer.parseInt(size);
        } else {
            GRAPH_SIZE = 20;
        }
        for (int i = 0; i < GRAPH_SIZE; i++) {
            Vertex v = new Vertex("v" + i, i);
            int cost = 5 + r.nextInt(3000);
            v.setCost(cost);
            // we can randomize the local node assignment
            if (i == 0 || i == 10 || i==20 || i == 30 || i == 40 || i == 50 || i == 60 || i== 70 || i == 80 || i == 90 || i==100 )
                v.setLocal(true);
            g.addVertex(v);
        }
        Random r2 = new Random(System.nanoTime());
        for (int k = 0; k < g.vertexSet().size(); k++) {
            flag = false;
            for (int m = k + 1; m < g.vertexSet().size(); m++) {
                if (r.nextDouble() < 0.1) {
                    Vertex vk = null;
                    Vertex vm = null;
                    for (Object vt : g.vertexSet()) {
                        Vertex v1 = (Vertex) vt;
                        if (v1.getId() == k)
                            vk = v1;
                        if (v1.getId() == m)
                            vm = v1;
                    }
                    int cost = 5 + r2.nextInt(3000);
                    Edge e = new Edge(vk.getUid() + "-" + vm.getUid());
                    e.setCost(cost);
                    e.setArgsize(cost);
                    g.addEdge(vk, vm, e);
                    flag = true;

                }
            }
            if (!flag) {
                int randomnode = (k + 1) + r2.nextInt(g.vertexSet().size() - k);
                Vertex vs = getVertex(k);
                Vertex vt = null;
                if (k < g.vertexSet().size() - 1) {
                    vt = getVertex((k + 1));
                    int cost = 5 + r2.nextInt(3000);
                    Edge e = new Edge(vs.getUid() + "-" + vt.getUid());
                    e.setCost(cost);
                    e.setArgsize(cost);
                    //Log.v("Followme", "k : " + k + ", m : " + (k + 1) + " edge created");
                    g.addEdge(vs, vt, e);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Edge size : " + g.edgeSet().size() + " \n");

        for (Object e : g.edgeSet()) {
            Edge e1 = (Edge) e;
            sb.append(e1.getUid() + ", ");
        }
        edge.setText(sb.toString());
        sb1 = new StringBuilder();

        // make graph connected
        Vertex v = getVertex(1);

        for (Object o : g.vertexSet()) {
            Vertex vk = (Vertex) o;
            kontrol.add(vk);
        }
        breadFirstSearch(g);

        return g;
    }


    public void breadFirstSearch(Graph g){
        int count = 0;
               //StringBuilder sb = new StringBuilder();

               boolean visited[] = new boolean[g.vertexSet().size()];

               // Create a queue for BFS
               LinkedList<Vertex> queue = new LinkedList<Vertex>();

               // Mark the current node as visited and enqueue it
               Vertex v = getVertex(0);

               visited[v.getId()] = true;
               queue.add(v);
               if (kontrol.contains(v))
                   kontrol.remove(v);

               while (queue.size() != 0) {
                   // Dequeue a vertex from queue and print it
                   v = queue.poll();
                   //System.out.print(v.getUid()+" ");
                   sb1.append(" " + v.getUid() + " ("+ v.getCost()+ "), ");
                   count++;
                   kontrol.remove(v);
                   if (g.edgesOf(v).size() == 0) {
                       Vertex t = kontrol.iterator().next();
                       g.addEdge(v, t, new Edge(v.getUid() + "-" + t.getUid()));
                       kontrol.remove(t);
                   }
                   Iterator<Edge> i = g.edgesOf(v).iterator();
                   while (i.hasNext()) {
                       Edge ee = i.next();
                       //if(ee.isExplored() == true)
                       //    continue;
                       Vertex nt = (Vertex) g.getEdgeTarget(ee);
                       Vertex ns = (Vertex) g.getEdgeSource(ee);

                       if (!visited[nt.getId()]) {
                           // ee.setExplored(true);
                           visited[nt.getId()] = true;
                           queue.add(nt);

                       }
                       if (!visited[ns.getId()]) {
                           // ee.setExplored(true);
                           visited[ns.getId()] = true;
                           queue.add(ns);

                       }
                   }
               }
        sb1.append(" (count :" +count + ")");

    }


    public Vertex getVertex(int id){

        for(Object o : g.vertexSet()){
            Vertex v = (Vertex)o;
            if(v.getId() == id)
                return v;
        }

        return null;
    }



}
