package voltric.variables.util;

import voltric.variables.IVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by equipo on 21/02/2017.
 */
public class VariableUtil {

    public static <V extends IVariable> boolean checkType(IVariable variable, Class<V> castType) {
        return castType.isInstance(variable);
    }

    // True if OK
    public static <V extends IVariable> boolean checkTypes(Collection<IVariable> variables, Class<V> castType) {
        for(IVariable variable: variables)
            if(!checkType(variable, castType))
                return false;

        return true;
    }

    // I know about type erasure on runtime...
    public static <V extends IVariable> List<V> castVariables(List<IVariable> variables, Class<V> castType) throws IllegalVariableCastException {

        if(!checkTypes(variables, castType))
            throw new IllegalVariableCastException("Illegal IVariable casting.");
        else
            // Cast the variables (just for compiler, type is erased at runtime)
            return variables.stream().map(x -> (V) x).collect(Collectors.toList());

    }
}
