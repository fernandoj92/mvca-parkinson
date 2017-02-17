package voltric.model;

import voltric.util.Converter;
import voltric.variables.DiscreteVariable;

/**
 * Extracts the variable from a belief node.
 * 
 * @author leonard
 * 
 */
public class DiscreteVariableExtractor implements Converter<BeliefNode, DiscreteVariable> {

	public DiscreteVariable convert(BeliefNode node) {
		return node.getVariable();
	}

}
