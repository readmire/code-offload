package readmire.offloadingframework;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jgrapht.Graph;

import java.util.Set;

import readmire.offloadinglibrary.DecisionManager;
import readmire.offloadinglibrary.Edge;
import readmire.offloadinglibrary.OffloadingFactory;
import readmire.offloadinglibrary.ProfilingManager;
import readmire.offloadinglibrary.Vertex;

/**
 *
 *    @yasemin
 **/
public class OffloadingFramework extends AppCompatActivity {
     OffloadingFactory offManager;
     TextView tresult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offloadingframework);
        tresult = findViewById(R.id.txtResult);
        Button b1 = findViewById(R.id.btn_testapp);
        Button b2 = findViewById(R.id.btn_testgraphcut);
        Button b3 = findViewById(R.id.btn_testrandomgraph);
        offManager = OffloadingFactory.getInstance();
        OffloadingFactory.initvar(OffloadingFramework.this);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 clickTestFramework(view);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OffloadingFramework.this,TestGraphCut.class));
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OffloadingFramework.this,TestRandomGraph.class));
            }
        });
    }
    public void clickTestFramework(View v){
            Graph<Vertex, Edge> graph = ProfilingManager.getGraph();
            if(graph!=null){
                Set<Edge> eset = graph.edgeSet();
                for(Edge e : eset){
                    e.setFreq(0);
                }
            }
            A a = offManager.create(A.class, OffloadingFramework.this,null);
              a.doA();

        DecisionManager dm = new DecisionManager();
        tresult.setText(dm.getSolverResult());

    }


}
