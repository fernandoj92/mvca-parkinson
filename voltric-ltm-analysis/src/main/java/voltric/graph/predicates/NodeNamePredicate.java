package voltric.graph.predicates;

import voltric.graph.AbstractNode;
import voltric.util.Predicate;

/**
 * Checks whether a node has name matching a given regular expression.
 * @author leonard
 *
 */
public class NodeNamePredicate implements Predicate<AbstractNode> {
	public NodeNamePredicate(String regex) {
		this.regex = regex;
	}

	public boolean evaluate(AbstractNode node) {
		return node.getName().matches(regex);
	}

	private final String regex;
}
