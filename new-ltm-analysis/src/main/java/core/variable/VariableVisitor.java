package core.variable;

/**
 * A visitor class of the visitor pattern that visits the subclasses of the
 * {@code Variable} class.
 *
 * @author leonard
 *
 */
public class VariableVisitor<T> {

    public T visit(SingularContinuousVariable variable) {
        return null;
    }

    public T visit(JointContinuousVariable variable) {
        return null;
    }

    public T visit(DiscreteVariable variable) {
        return null;
    }
}
