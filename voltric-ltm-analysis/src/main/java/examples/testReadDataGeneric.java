package examples;

import voltric.data.Data;
import voltric.io.data.DataFileLoader;
import voltric.io.data.arff.GenericArffFileReader;
import voltric.variables.ContinuousVariable;
import voltric.variables.DiscreteVariable;
import voltric.variables.SingularContinuousVariable;
import voltric.variables.Variable;

/**
 * Created by equipo on 21/02/2017.
 */
public class testReadDataGeneric {

    public static void main(String[] args){
        test();
    }

    private static void test(){
        GenericArffFileReader reader = new GenericArffFileReader();
        Data<SingularContinuousVariable> d =  reader.readData("data/Asia_train.arff");
        Data<SingularContinuousVariable> d1 = DataFileLoader.loadData("", SingularContinuousVariable.class);
    }
}
