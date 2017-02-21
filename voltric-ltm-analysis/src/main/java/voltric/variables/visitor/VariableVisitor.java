package voltric.variables.visitor;

import voltric.variables.DiscreteVariable;
import voltric.variables.JointContinuousVariable;
import voltric.variables.SingularContinuousVariable;

/**
 * A visitor class of the visitor pattern that visits the subclasses of the
 * {@code Variable} class.
 *
 * @author leonard
 *
 */
public interface VariableVisitor<T> {

    T visit(SingularContinuousVariable variable);

    T visit(JointContinuousVariable variable);

    T visit(DiscreteVariable variable);
}
