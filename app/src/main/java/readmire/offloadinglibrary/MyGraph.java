package readmire.offloadinglibrary;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
/**
 *
 *    @yasemin
 **/

public class MyGraph
extends AbstractBaseGraph<Vertex, Edge>
implements UndirectedGraph<Vertex, Edge> {

    public MyGraph() {// EdgeFactory<String, DefaultEdge> arg0, boolean arg1,boolean arg2
	    super(new ClassBasedEdgeFactory<Vertex, Edge>(
	            Edge.class),
	        true,
	        true);
	   
	}
	
}