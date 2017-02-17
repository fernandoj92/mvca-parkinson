package voltric.data;

import voltric.variables.DiscreteVariable;
import voltric.variables.SingularContinuousVariable;
import voltric.variables.Variable;
import voltric.variables.VariableCollection;

/**
 * Created by equipo on 16/02/2017.
 */
// TODO: "isValuePermitted" not implemented, but should work anyway
public class DataInstanceFactory {

    private static final double MISSING = Double.NaN;

    public static DataInstance fromArffDataLine(String dataLine, final VariableCollection variables, int dataLineIndex){
        String[] parts = dataLine.split(",");

        if(parts.length != variables.size())
            throw new IllegalArgumentException("DataRow ["+ dataLineIndex +"]: The number of columns does not match the number of ARFF attributes.");

        double[] values = new double[variables.size()];
        for(int i=0; i<parts.length; i++){

            Variable variable = variables.get(i);

            if(parts[i] == null || parts[i].equals("?"))
                values[i] = MISSING;
            else if(variable instanceof DiscreteVariable)
                values[i] = ((DiscreteVariable) variable).indexOf(parts[i]);
            else if(variable instanceof SingularContinuousVariable)
                values[i] = Double.parseDouble(parts[i]);
            else
                throw new IllegalArgumentException("Illegal type of Variable: " + variable.getName());
        }
        return new DataInstance(variables, values);
    }
}
