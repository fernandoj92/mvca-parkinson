package voltric.variables.util;

import voltric.variables.IVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by equipo on 21/02/2017.
 */
public class VariableUtil {

    public static <V extends IVariable> boolean checkType(IVariable variable, Class<V> castType) throws IllegalVariableCastException{
        return castType.isInstance(variable);
    }

    public static <V extends IVariable> boolean checkTypes(Collection<IVariable> variables, Class<V> castType) throws IllegalVariableCastException{
        for(IVariable variable: variables)
            if(!checkType(variable, castType))
                return false;

        return true;
    }

    // I know about type erasure on runtime...
    public static <V extends IVariable> List<V> castVariables(List<IVariable> variables, Class<V> castType) throws IllegalVariableCastException {
        List<V> castedVariables = new ArrayList<V>();

        if(checkTypes(variables, castType))
            throw new IllegalVariableCastException("Illegal IVariable casting.");
        else
            // Cast the variables (just for compiler, type is erased at runtime)
            variables.stream().map(x -> (V) x);

        return castedVariables;
    }
}
