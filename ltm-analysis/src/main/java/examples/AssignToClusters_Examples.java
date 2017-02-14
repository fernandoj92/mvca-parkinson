package examples;

import ferjorosa.analysis.AssignToClusters;
import ferjorosa.data.DataSet;
import ferjorosa.data.DataSetLoader;
import ferjorosa.ltm.learning.parameters.LTM_Learner;
import org.latlab.clustering.BridgedIslands;
import org.latlab.model.LTM;

/**
 * Created by equipo on 14/02/2017.
 */
public class AssignToClusters_Examples {

    public static void main(String[] args){
        assignAsiaDataSet();
    }

    private static void assignAsiaDataSet(){
        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/Asia_train.arff"));

            BridgedIslands bi = new BridgedIslands();
            // Learns the structure of the LTM with the Bridged Islands algorithm and finishes the job
            // by learning its parameters with a full set of iterations of the EM algorithm.
            LTM ltm = LTM_Learner.learnParameters(bi.learnLTM(data), data);
            //System.out.println(ltm.toString(1));
            System.out.println("BIC: "+ ltm.getBICScore(data));
            System.out.println("AIC: "+ ltm.getLoglikelihood(data));
            System.out.println("LL: "+ ltm.getAICScore(data));

            AssignToClusters.assignDataCaseToClusters(data, ltm);


        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
