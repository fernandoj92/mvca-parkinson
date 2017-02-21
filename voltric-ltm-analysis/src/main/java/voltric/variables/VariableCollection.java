package voltric.variables;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds a collection of getVariables.
 * 
 * @author leonard
 * 
 */
// TODO: Permitir diferente tipo de Collections? (implements Collection<Variable>)
public class VariableCollection<A extends IVariable> extends AbstractList<A> {

	private static final long serialVersionUID = -291636629108998675L;
	
	private final List<A> variables;

	public VariableCollection() {
		variables = new ArrayList<A>();
	}

	public VariableCollection(int initialCapacity) {
		variables = new ArrayList<A>(initialCapacity);
	}
	
	public VariableCollection(VariableCollection variables) {
		this.variables = new ArrayList<A>(variables);
	}

	public VariableCollection(List<A> _variables) {
        this.variables =_variables;
    }

	@Override
	public A get(int index) {
		return variables.get(index);
	}

	@Override
	public int size() {
		return variables.size();
	}

	@Override
	public A set(int index, A variable) {
		return variables.set(index, variable);
	}

	@Override
	public void add(int index, A variable) {
		variables.add(index, variable);
	}

	@Override
	public A remove(int index) {
		return variables.remove(index);
	}
}
