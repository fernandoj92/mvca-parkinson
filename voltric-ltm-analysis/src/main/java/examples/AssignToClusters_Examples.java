package examples;


import ferjorosa.analysis.AssignToClusters;
import ferjorosa.ltm.learning.parameters.LTM_Learner;
import scala.Array;
import voltric.clustering.BridgedIslands;
import voltric.data.dataset.DiscreteDataCase;
import voltric.data.dataset.DiscreteDataSet;
import voltric.io.data.DataFileLoader;
import voltric.model.LTM;
import voltric.util.Function;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;

/**
 * Created by equipo on 14/02/2017.
 */
public class AssignToClusters_Examples {

    public static void main(String[] args){
        assignAsiaDataSet();
    }

    private static void assignAsiaDataSet(){
        try {
            DiscreteDataSet dataSet = new DiscreteDataSet(DataFileLoader.loadData("data/Asia_train.arff", DiscreteVariable.class));

            BridgedIslands bi = new BridgedIslands();
            // Learns the structure of the LTM with the Bridged Islands algorithm and finishes the job
            // by learning its parameters with a full set of iterations of the EM algorithm.
            LTM ltm = LTM_Learner.learnParameters(bi.learnLTM(dataSet), dataSet);
            //System.out.println(ltm.toString(1));
            System.out.println("BIC: "+ ltm.getBICScore(dataSet));
            System.out.println("AIC: "+ ltm.getLoglikelihood(dataSet));
            System.out.println("LL: "+ ltm.getAICScore(dataSet));

            ArrayList<ArrayList<Function>> clusterAssignments = AssignToClusters.assignDataCaseToClusters(dataSet, ltm);

            // Basicamente lee el archivo y le asigna a cada instancia su data case correspondiente
            int[] dataCaseIndexes = dataSet.getDatacaseIndex("data/Asia_train.arff");
            ArrayList<DiscreteDataCase> dataCases = dataSet.getData();
            // Reconstruimos el dataset pero esta vez añadiendo los datos sobre las particiones
            // en formato ARFF
            for(int index: dataCaseIndexes){
                dataCases.get(index);

            }

        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
