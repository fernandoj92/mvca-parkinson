package voltric.graph.predicates;

import voltric.graph.DirectedNode;
import voltric.util.Predicate;

/**
 * Checks whether a node is a root node, which has no parents.
 * @author leonard
 *
 */
public class RootNodePredicate implements Predicate<DirectedNode> {

	public boolean evaluate(DirectedNode node) {
		return node.getParents().size() == 0;
	}

}
