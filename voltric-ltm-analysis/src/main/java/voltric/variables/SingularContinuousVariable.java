package voltric.variables;

import voltric.variables.visitor.VariableVisitor;

import java.util.Collections;
import java.util.Set;

/**
 * A single dimensional continuous variable.
 *
 * @author leonard
 *
 */
public class SingularContinuousVariable extends ContinuousVariable {

    /**
     * Constructs a singular continuous variable with a default getName.
     */
    public SingularContinuousVariable() {

    }

    /**
     * Constructs a singular continuous variable with the given {@code getName}.
     *
     * @param name
     *            getName of this variable
     */
    public SingularContinuousVariable(String name) {
        super(name);
    }

    /**
     * Returns a sorted set that contains only this variable.
     *
     * @return sorted set containing only this variable
     */
    @Override
    public Set<SingularContinuousVariable> variables() {
        return Collections.singleton(this);
    }

    /**
     * The accept function of the visitor pattern.
     *
     * @param visitor
     *            visits this instance
     */
    public <T> T accept(VariableVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        else if (o instanceof SingularContinuousVariable) {
            return getName().equals(((SingularContinuousVariable) o).getName());
        } else {
            return false;
        }
    }
}