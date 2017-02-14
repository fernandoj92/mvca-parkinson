package ferjorosa.analysis;

import ferjorosa.data.DataCase;
import org.latlab.model.LTM;
import org.latlab.reasoner.CliqueTreePropagation;
import ferjorosa.data.DataSet;
import org.latlab.util.Function;

/**
 * Created by equipo on 14/02/2017.
 */
public class AssignToClusters {

    /**
     *
     * @param dataSet
     * @param ltm
     */
    public static void assignDataCaseToClusters(DataSet dataSet, LTM ltm){


        // Creates a CliqueTreePropagation instance to do the inference
        CliqueTreePropagation inferenceEngine = new CliqueTreePropagation(ltm);
        for(DataCase dataCase : dataSet.getData()){
            // Set the dataCase as evidence
            inferenceEngine.setEvidence(dataSet.getVariables(), dataCase.getStates());
            // Propagate the evidence
            inferenceEngine.propagate();
            // Request the values of the latentVariables
            Function f = inferenceEngine.computeBelief(ltm.getLatVarsfromTop().get(0));
            System.out.println("DataCase: " + dataCase.toString());
            //System.out.println(f.getCells().toString());
            System.out.println(f.toString(1));
            System.out.println("-----------------------------------------\n");
        }
        //inferenceEngine.setEvidence();
    }

}
