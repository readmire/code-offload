package readmire.offloadinglibrary;

import org.jgrapht.Graph;

public interface HeuristicSolution {
   public void setGraph(Graph<Vertex,Edge> graph);
   public String getHeuristicResult();
}
