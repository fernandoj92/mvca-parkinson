package ferjorosa.ltm.learning.parameters;

import org.latlab.learner.ParallelEmLearner;
import org.latlab.model.LTM;
import ferjorosa.data.DataSet;

/**
 * Created by equipo on 14/02/2017.
 */
public class LTM_Learner {

    public static LTM  learnParameters(LTM ltm, DataSet data){

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

        return (LTM) emLearner.em(ltm, data);
    }

    public static LTM  learnParameters(LTM ltm, DataSet data, int emMaxSteps, int emNumRestarts, double emThreshold){

        ParallelEmLearner emLearner = new ParallelEmLearner();
        emLearner.setLocalMaximaEscapeMethod("ChickeringHeckerman");
        emLearner.setMaxNumberOfSteps(emMaxSteps);
        emLearner.setNumberOfRestarts(emNumRestarts);
        // fix starting point or not?
        emLearner.setReuseFlag(false);
        emLearner.setThreshold(emThreshold);

        return (LTM) emLearner.em(ltm, data);
    }
}
