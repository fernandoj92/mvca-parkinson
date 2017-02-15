package ferjorosa.analysis;

import ferjorosa.data.DataCase;
import org.latlab.model.LTM;
import org.latlab.reasoner.CliqueTreePropagation;
import ferjorosa.data.DataSet;
import org.latlab.util.Function;
import org.latlab.util.Variable;

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
    public static ArrayList<ArrayList<Function>> assignDataCaseToClusters(DataSet dataSet, LTM ltm){

        ArrayList<ArrayList<Function>> partitionCpts = new ArrayList<>();

        // Creates a CliqueTreePropagation instance to do the inference
        CliqueTreePropagation inferenceEngine = new CliqueTreePropagation(ltm);
        for(DataCase dataCase : dataSet.getData()){
            ArrayList<Function> cpts = new ArrayList<>();
            // Set the dataCase as evidence
            inferenceEngine.setEvidence(dataSet.getVariables(), dataCase.getStates());
            // Propagate the evidence
            inferenceEngine.propagate();
            // Request the values of the latentVariables
            for(Variable latentVar : ltm.getLatVarsfromTop())
                cpts.add(inferenceEngine.computeBelief(latentVar));
            // Add the cpts associated with the dataCase to the array that will be returned
            partitionCpts.add(cpts);
        }
        return partitionCpts;
    }

}
