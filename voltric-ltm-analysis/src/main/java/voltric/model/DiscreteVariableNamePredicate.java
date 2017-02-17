package voltric.model;


import voltric.util.Predicate;
import voltric.variables.DiscreteVariable;

/**
 * Checks whether a variable has the same name as a specified string.
 * 
 * @author leonard
 * 
 */
public class DiscreteVariableNamePredicate implements Predicate<DiscreteVariable> {
	public DiscreteVariableNamePredicate(String name) {
		this.name = name;
	}

	public boolean evaluate(DiscreteVariable variable) {
		return variable.getName().equals(name);
	}

	private final String name;
}
