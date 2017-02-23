package voltric.data;

import voltric.variables.DiscreteVariable;
import voltric.variables.IVariable;
import voltric.variables.Variable;
import voltric.variables.VariableCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fernando on 2/15/2017.
 */
public class DataInstance<A extends IVariable> implements Comparable<DataInstance>{

    private double[] values;
    private final VariableCollection<A> variables;

    public DataInstance(final VariableCollection<A> variables, double[] values){
        this.variables = variables;
        this.values = values;
    }

    public int compareTo(DataInstance o) {
        if(this.values.length != o.values.length)
            throw new IllegalArgumentException("The number of columns of the data instances does not coincide");

        if(!variables.equals(o.variables))
            throw new IllegalArgumentException("The variables object of both data instances does not coincide");

        for (int i = 0; i < values.length; i++) {
            if (values[i] != o.values[i])
                if(values[i] < o.values[i])
                    return -1;
                else if(o.values[i] < values[i])
                    return 1;
        }

        return 0;
    }

    public final double[] getNumericValues() {
        return values;
    }

    public final List<String> getTextualValues() {
        List<String> result = new ArrayList<String>(variables.size());

        for (int i = 0; i < values.length; i++) {
            // variable's type match-case
            if(variables.get(i) instanceof DiscreteVariable) {
                DiscreteVariable discreteVariable = (DiscreteVariable) variables.get(i);
                result.add(discreteVariable.getState((int) values[i]));
            }
            else
                result.add(values[i] + "");
        }

        return result;
    }

    public double getNumericValue(int index) {
        return values[index];
    }

    public String getTextualValue(int index){
        if(variables.get(index) instanceof DiscreteVariable) {
            DiscreteVariable discreteVariable = (DiscreteVariable) variables.get(index);
            return discreteVariable.getState((int) values[index]);
        }
        else
            return values[index] + "";
    }

    public double getNumericValue(A variable){
        return getNumericValue(variables.indexOf(variable));
    }

    public int size() {
        return values.length;
    }
}
