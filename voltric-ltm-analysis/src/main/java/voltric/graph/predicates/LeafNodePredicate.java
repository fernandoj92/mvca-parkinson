package voltric.graph.predicates;

import voltric.graph.DirectedNode;
import voltric.util.Predicate;

/**
 * Checks whether a directed node is a leaf node, which has no children.
 * @author leonard
 *
 */
public class LeafNodePredicate implements Predicate<DirectedNode> {

	public boolean evaluate(DirectedNode node) {
		return node.getChildren().size() == 0;
	}

}
