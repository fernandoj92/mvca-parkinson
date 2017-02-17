package ferjorosa.analysis;

import voltric.data.dataset.DiscreteDataCase;
import voltric.data.dataset.DiscreteDataSet;
import voltric.model.LTM;
import voltric.reasoner.CliqueTreePropagation;
import voltric.util.Function;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;

/**
 * Created by equipo on 14/02/2017.
 */
public class AssignToClusters {

    /**
     * Returns a matrix with the CPT values of the latent variables associated to each data case.
     *
     * @param dataSet
     * @param ltm
     */
    public static ArrayList<ArrayList<Function>> assignDataCaseToClusters(DiscreteDataSet dataSet, LTM ltm){

        ArrayList<ArrayList<Function>> partitionCpts = new ArrayList<>();

        // Creates a CliqueTreePropagation instance to do the inference
        CliqueTreePropagation inferenceEngine = new CliqueTreePropagation(ltm);
        for(DiscreteDataCase dataCase : dataSet.getData()){
            ArrayList<Function> cpts = new ArrayList<>();
            // Set the dataCase as evidence
            inferenceEngine.setEvidence(dataSet.getVariables(), dataCase.getStates());
            // Propagate the evidence
            inferenceEngine.propagate();
            // Request the values of the latentVariables
            for(DiscreteVariable latentVar : ltm.getLatVarsfromTop())
                cpts.add(inferenceEngine.computeBelief(latentVar));
            // Add the cpts associated with the dataCase to the array that will be returned
            partitionCpts.add(cpts);
        }
        return partitionCpts;
    }

}
