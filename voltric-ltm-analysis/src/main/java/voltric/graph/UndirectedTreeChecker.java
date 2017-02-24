package voltric.graph;

import java.util.stream.Collectors;

/**
 * Checks whether an UndirectedGraph is a tree or not
 */
public class UndirectedTreeChecker {

    public static boolean isTree(AbstractGraph graph) {
        // If the number of edges in a tree must be number of nodes minus 1
        if (graph.getNumberOfEdges() != graph.getNumberOfNodes() - 1)
            return false;
        // If there is in an unconnected node (a node without neighbors)
        else if(graph.getNodes().stream().filter(x->x._neighbors.size() == 0).collect(Collectors.toList()).size() != 0)
            return false;
        // If there are no cycles
        // TODO

        return true;
    }

}
