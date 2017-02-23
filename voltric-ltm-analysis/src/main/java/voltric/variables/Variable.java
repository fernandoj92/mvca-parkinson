package voltric.variables;

import voltric.util.counter.Counter;
import voltric.util.counter.CounterInstance;
import voltric.variables.visitor.VariableVisitor;

import java.util.Collection;

public abstract class Variable implements IVariable, Comparable<Variable>{

    private static Counter counter = new Counter("variable");

    /**
     * the index of this variable that indicates when it was created (its identifier)
     */
    private int _index;

    /**
     * the getName of this variable.
     */
    private String _name;

    /**
     * Constructs a variable with the given getName, trimmed with the white spaces.
     *
     * @param name
     *            getName of the variable
     */
    protected Variable(String name) {
        _name = name.trim();

        // getName cannot be blank
        if(_name.length() <= 0)
            throw new IllegalArgumentException("name cannot be blank");

        _index = counter.nextIndex();

        counter.encounterName(name);
    }

    /**
     * Constructs a variable by assigning it with a default getName given by an
     * internal counter.
     */
    protected Variable() {
        CounterInstance instance = counter.next();
        _name = instance.name;
        _index = instance.index;
    }

    /**
     * <p>
     * Compares this variable with the specified object for order.
     * </p>
     *
     * <p>
     * Note: <code>compareTo(Object)</code> is inconsistent with
     * <code>equals(Object)</code>.
     * </p>
     *
     * @param o the variable to be compared.
     * @return a negative or a positive integer if this variable was created
     *         earlier than or later than the specified variable; zero if they
     *         refers to the same variable.
     */
    public int compareTo(Variable o) {
        return _index - o._index;
    }

    /**
     * Returns the getName of this variable.
     *
     * @return getName of this variable
     */
    public String getName() {
        return _name;
    }

    /**
     * Updates the getName of this variable.
     *
     * <p>
     * Note: Only <code>BeliefNode.setName(String></code> is supposed to call
     * this method. Abusing this method may cause inconsistency between names of
     * a belief node and the variable attached to it.
     * </p>
     *
     * @param name
     *            new getName of this variable.
     */
    public void setName(String name) {
        name = name.trim();

        // getName cannot be blank
        if(name.length() <= 0)
            throw new IllegalArgumentException("name cannot be blank");

        _name = name;
    }

    public abstract StateSpaceType getStateSpaceType();

    public abstract <T> T accept(VariableVisitor<T> visitor);

    /**
     * Returns a string representation of a collection of getVariables, with a
     * delimiter between each of them.
     *
     * @param variables
     * @param delimiter
     * @return
     */
    public static String getName(Collection<? extends Variable> variables, String delimiter) {
        boolean first = true;

        StringBuilder builder = new StringBuilder();
        for (Variable variable : variables) {
            if (!first) {
                builder.append(delimiter);
            }

            builder.append(variable.getName());
            first = false;
        }

        return builder.toString();
    }
}