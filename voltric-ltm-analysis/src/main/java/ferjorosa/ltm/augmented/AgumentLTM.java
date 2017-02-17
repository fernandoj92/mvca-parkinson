package ferjorosa.ltm.augmented;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import voltric.data.dataset.DiscreteDataSet;
import voltric.model.BayesNet;
import voltric.model.LTM;
import voltric.variables.DiscreteVariable;

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
    public static BayesNet treeAugmentedLTM(LTM ltm, DiscreteDataSet dataSet){

        Set<DiscreteVariable> manifestVariables = ltm.getManifestVars();

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
    public static BayesNet superParentLTM(LTM ltm, DiscreteDataSet dataSet){
        throw new NotImplementedException();
    }
}
