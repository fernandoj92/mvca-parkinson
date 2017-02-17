package voltric.graph;

import voltric.graph.predicates.RootNodePredicate;
import voltric.graph.search.DepthFirstSearch;
import voltric.graph.search.TimeVisitor;
import voltric.util.Algorithm;
import voltric.util.Caster;

import java.util.List;

/**
 * Checks whether a graph is a tree.
 * 
 * @author leonard
 * 
 */
public class TreeChecker {

    /**
     * Returns whether a tree has a specific node as root.
     *
     * @param graph graph to check.
     * @param node the node in question.
     * @return wheter the node is the root of the tree.
     */
    public static boolean isRoot(AbstractGraph graph, AbstractNode node) {
        // a tree can have only one root
        List<DirectedNode> roots = Algorithm.filter(graph.getNodes(),
                new Caster<DirectedNode>(), new RootNodePredicate());

        if (roots.size() == 1 && roots.get(0).equals(node))
            return true;

        return false;
    }

	/**
	 * Returns whether a graph is a tree.
	 * 
	 * @param graph graph to check
	 * @return whether the graph is a tree
	 */
	public static boolean isTree(AbstractGraph graph) {
		// the number of edges in a tree must be number of nodes minus 1
		if (graph.getNumberOfEdges() != graph.getNumberOfNodes() - 1)
			return false;

		// a tree can have only one root
		List<DirectedNode> roots = Algorithm.filter(graph.getNodes(),
				new Caster<DirectedNode>(), new RootNodePredicate());
		if (roots.size() != 1)
			return false;

		// all nodes in a tree must be connected to the root
		DepthFirstSearch search = new DepthFirstSearch(graph);
		TimeVisitor visitor = new TimeVisitor();
		search.perform(roots.get(0), visitor);

		return visitor.discoveringTimes.size() == graph.getNumberOfNodes();
	}
	
	/**
	 * Return whether a graph is a bayesnet.
	 * 
	 * The idea is: if it is not a bayesnet, there must be circles
	 * 
	 * @param graph the graph to check.
	 * @return whether the graph is a bayesian network.
	 */
	public static boolean isBayesNet(AbstractGraph graph) {
		
		List<DirectedNode> roots = Algorithm.filter(graph.getNodes(),
				new Caster<DirectedNode>(), new RootNodePredicate());
		
		if(roots.size()==0)
			return false;
		
		DepthFirstSearch search = new DepthFirstSearch(graph);
		TimeVisitor visitor = new TimeVisitor();
		search.perform(roots.get(0), visitor);
		
		if(visitor.discoveringTimes.size() != graph.getNumberOfNodes())
		{
			return false;
		}
		
		for(AbstractNode node : graph.getNodes())
		{
			search = new DepthFirstSearch(graph);
			visitor = new TimeVisitor();
			visitor.setRoot(node);
			search.perform(node, visitor);
			
			
			if(visitor.reVisit())
			{
				return false;
			}
		}
		
		return true;
	}


}
