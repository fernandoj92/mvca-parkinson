package examples;


import voltric.data.dataset.DiscreteDataSet;
import voltric.io.data.DataFileLoader;
import voltric.learner.EmLearner;
import voltric.model.LTM;
import voltric.util.ScoreCalculator;
import voltric.variables.DiscreteVariable;
import voltric.variables.SingularContinuousVariable;

/**
 * Created by equipo on 24/01/2017.
 */
public class CompleteDataSet_Examples {

    public static void main(String[] args){
        sprinklerLTM();
    }

    private static void sprinklerLTM(){
        try {
            DiscreteDataSet data = new DiscreteDataSet(DataFileLoader.loadData("data/sprinklerDataHidden.arff", DiscreteVariable.class));
            LTM ltm = LTM.createLCM(data.getVariables(), 2);
            EmLearner emLearner = new EmLearner();
            ltm = (LTM) emLearner.em(ltm, data);
            ScoreCalculator scoreCalculator =  new ScoreCalculator();
            double scoreLL = scoreCalculator.computeMaximumLoglikelihood_CompletedData(ltm, data);
            System.out.println("EM LogLikelihood: " + ltm.getLoglikelihood(data));
            System.out.println("ScoreCalculator LogLikelihood: " + scoreLL);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
