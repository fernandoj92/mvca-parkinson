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
public class VariableCollection<V extends IVariable> extends AbstractList<V> {

	private static final long serialVersionUID = -291636629108998675L;
	
	private final List<V> variables;

	public VariableCollection() {
		variables = new ArrayList<V>();
	}

	public VariableCollection(int initialCapacity) {
		variables = new ArrayList<V>(initialCapacity);
	}
	
	public VariableCollection(VariableCollection variables) {
		this.variables = new ArrayList<V>(variables);
	}

	public VariableCollection(List<V> _variables) {
        this.variables =_variables;
    }

	@Override
	public V get(int index) {
		return variables.get(index);
	}

	@Override
	public int size() {
		return variables.size();
	}

	@Override
	public V set(int index, V variable) {
		return variables.set(index, variable);
	}

	@Override
	public void add(int index, V variable) {
		variables.add(index, variable);
	}

	@Override
	public V remove(int index) {
		return variables.remove(index);
	}

}
