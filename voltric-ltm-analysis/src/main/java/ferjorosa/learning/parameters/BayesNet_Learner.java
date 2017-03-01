package ferjorosa.learning.parameters;

import voltric.data.dataset.DiscreteDataSet;
import voltric.learner.ParallelEmLearner;
import voltric.model.BayesNet;

/**
 * Created by equipo on 01/03/2017.
 */
public class BayesNet_Learner {

    public static BayesNet learnParameters(BayesNet network, DiscreteDataSet data){

        int _EmMaxSteps = 50;
        int _EmNumRestarts = 5;
        double _EmThreshold = 0.01;

        ParallelEmLearner emLearner = new ParallelEmLearner();
        emLearner.setLocalMaximaEscapeMethod("ChickeringHeckerman");
        emLearner.setMaxNumberOfSteps(_EmMaxSteps);
        emLearner.setNumberOfRestarts(_EmNumRestarts);
        // fix starting point or not?
        emLearner.setReuseFlag(false);
        emLearner.setThreshold(_EmThreshold);

        return emLearner.em(network, data);
    }

    public static BayesNet learnParameters(BayesNet network, DiscreteDataSet data, int emMaxSteps, int emNumRestarts, double emThreshold){

        ParallelEmLearner emLearner = new ParallelEmLearner();
        emLearner.setLocalMaximaEscapeMethod("ChickeringHeckerman");
        emLearner.setMaxNumberOfSteps(emMaxSteps);
        emLearner.setNumberOfRestarts(emNumRestarts);
        // fix starting point or not?
        emLearner.setReuseFlag(false);
        emLearner.setThreshold(emThreshold);

        return emLearner.em(network, data);
    }
}
