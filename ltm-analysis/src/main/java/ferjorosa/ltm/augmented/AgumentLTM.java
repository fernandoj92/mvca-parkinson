package ferjorosa.ltm.augmented;

import org.latlab.model.LTM;
import org.latlab.util.DataSet;
import org.latlab.util.Variable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Set;

/**
 * Created by Fernando on 15/01/2017.
 */
public class AgumentLTM {

    /**
     * Applies the Chow Liu algorithm to all the MVs of the LTM to generate a new LTM with edges between its MVs
     *
     * @param ltm
     * @param dataSet
     * @return
     */
    public static LTM treeAugmentedLTM(LTM ltm, DataSet dataSet){

        Set<Variable> manifestVariables = ltm.getManifestVars();

        //



        throw new NotImplementedException();
    }

    /**
     * Applies the Super Parent algorithm to the full structure, without taking into consideration the different partitions
     *
     * @param ltm
     * @param dataSet
     * @return
     */
    public static LTM superParentLTM(LTM ltm, DataSet dataSet){
        throw new NotImplementedException();
    }
}
