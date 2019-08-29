package readmire.offloadinglibrary;

import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
/**
 *
 *    @yasemin
 **/
public class MyGraph2 extends AbstractBaseGraph<Vertex, Edge>
        implements DirectedGraph<Vertex, Edge> {
    public MyGraph2() {// EdgeFactory<String, DefaultEdge> arg0, boolean arg1,boolean arg2
        super(new ClassBasedEdgeFactory<Vertex, Edge>(
                        Edge.class),
                false,
                false);

    }

}