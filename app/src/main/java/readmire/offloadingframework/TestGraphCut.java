package readmire.offloadingframework;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jgrapht.Graph;

import readmire.offloadinglibrary.Edge;
import readmire.offloadinglibrary.HeuristicSolution2SA;
import readmire.offloadinglibrary.HeuristicSolution3MC;
import readmire.offloadinglibrary.MyGraph2;
import readmire.offloadinglibrary.OptimalSolution;
import readmire.offloadinglibrary.Vertex;
/**
 *
 *    @yasemin
 *    We have created a graph manually and apply the algorithms to find best parititions
 **/
public class TestGraphCut extends AppCompatActivity {
     Button test;
     TextView mytext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_graph_cut);
        test = findViewById(R.id.btnmytest);
        mytext = findViewById(R.id.txtmytest);

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                click_mytestbutton(view);
            }
        });

    }

    private void click_mytestbutton(View view) {

        //create graph:
         //Graph 1
        ///*
        Graph g = new MyGraph2();
        //Vertices are created and vertex cost is assigned
        Vertex v0 = new Vertex("v0",0,0);
        Vertex v1 = new Vertex("v1",1,6);
        Vertex v2 = new Vertex("v2",2,2);
        Vertex v3 = new Vertex("v3",3,8);
        Vertex v4 = new Vertex("v4",4,4);
        Vertex v5 = new Vertex("v5",5,10);
       // Vertex v10 = new Vertex("v10",10,5);
        // vertices are added to the graph
        g.addVertex(v0);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addVertex(v5);
        //g.addVertex(v10);

        v0.setLocal(true);
        //v10.setLocal(true);
        //v4.setLocal(true);

        //Edges are created and added to the graph
        g.addEdge(v0,v1, new Edge("v0-v1",4));
        g.addEdge(v0,v2,new Edge("v0-v2",8));
        g.addEdge(v1,v3,new Edge("v1-v3",3));
        g.addEdge(v1,v4,new Edge("v1-v4",2));
        g.addEdge(v2,v3,new Edge("v2-v3",1));
        g.addEdge(v3,v4,new Edge("v3-v4",4));
        g.addEdge(v3,v5,new Edge("v3-v5",5));
        //g.addEdge(v0,v10,new Edge("v0-v10",10));
        //g.addEdge(v10,v2,new Edge("v10-v2",7));
        //g.addEdge(v10,v3,new Edge("v10-v3",9));

          //*/

        /*
        Graph g = new MyGraph2();
        Vertex v0 = new Vertex("v0",0,5);
        Vertex v1 = new Vertex("v1",1,0);
        Vertex v2 = new Vertex("v2",2,6);
        Vertex v3 = new Vertex("v3",3,10);
        Vertex v4 = new Vertex("v4",4,20);
        g.addVertex(v0);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        v0.setLocal(true);
        v1.setLocal(true);
        v2.setLocal(true);

        g.addEdge(v0,v4, new Edge("v0-v4",5));
        g.addEdge(v0,v3,new Edge("v0-v3",8));
        g.addEdge(v1,v3,new Edge("v1-v3",7));
        g.addEdge(v2,v3,new Edge("v2-v3",10));
        g.addEdge(v2,v4,new Edge("v2-v4",2));
        g.addEdge(v3,v4,new Edge("v3-v4",4));

        */
        // Partitioning algorithms are implemented to find best parts for code offloading
        OptimalSolution opsol = new OptimalSolution();
        String resultopt = opsol.findOptimalResult(g);

        HeuristicSolution2SA h2 = new HeuristicSolution2SA(g);
        String  result = h2.getHeuristicResult();

        HeuristicSolution3MC h3 = new HeuristicSolution3MC(g);
        String result2 = h3.getHeuristicResult();

        mytext.setText(resultopt + " \n\n " + result + " \n\n " + result2);
    }
}
