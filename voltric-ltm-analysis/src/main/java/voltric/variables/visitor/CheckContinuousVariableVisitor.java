package voltric.variables.visitor;

import voltric.variables.DiscreteVariable;
import voltric.variables.JointContinuousVariable;
import voltric.variables.SingularContinuousVariable;

/**
 * Created by equipo on 21/02/2017.
 */
public class CheckContinuousVariableVisitor implements VariableVisitor<Boolean> {

    @Override
    public Boolean visit(SingularContinuousVariable variable) {
        return false;
    }

    @Override
    public Boolean visit(JointContinuousVariable variable) {
        return false;
    }

    @Override
    public Boolean visit(DiscreteVariable variable) {
        return true;
    }
}
