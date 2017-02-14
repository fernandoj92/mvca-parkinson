package examples;

import org.latlab.clustering.BridgedIslands;
import org.latlab.learner.ParallelEmLearner;
import org.latlab.model.BeliefNode;
import org.latlab.model.LTM;
import ferjorosa.data.DataSet;
import ferjorosa.data.DataSetLoader;
import org.latlab.util.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by equipo on 16/01/2017.
 */
public class AugmentLTM_Examples {

    public static void main(String[] args){
        asiaExamplesLCM();
    }

    private static void asiaExamplesLCM(){
        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/Asia_train.arff"));

            System.out.println("------------------------------");
            System.out.println("-------- Asia LCM (2) --------");
            System.out.println("------------------------------");
            LTM asiaLCM2 = learnParameters(LTM.createLCM(data.getVariables(), 2), data);
            System.out.println("BIC: "+ asiaLCM2.getBICScore(data));
            System.out.println("AIC: "+ asiaLCM2.getLoglikelihood(data));
            System.out.println("LL: "+ asiaLCM2.getAICScore(data));
            //showCTPs(asiaLCM2);

            System.out.println("------------------------------");
            System.out.println("-------- Asia LCM (3) --------");
            System.out.println("------------------------------");
            LTM asiaLCM3 = learnParameters(LTM.createLCM(data.getVariables(), 3), data);
            System.out.println("BIC: "+ asiaLCM3.getBICScore(data));
            System.out.println("AIC: "+ asiaLCM3.getLoglikelihood(data));
            System.out.println("LL: "+ asiaLCM3.getAICScore(data));
            //showCTPs(asiaLCM3);

            System.out.println("------------------------------");
            System.out.println("-------- Asia LCM (4) --------");
            System.out.println("------------------------------");
            LTM asiaLCM4 = learnParameters(LTM.createLCM(data.getVariables(), 4), data);
            System.out.println("BIC: "+ asiaLCM4.getBICScore(data));
            System.out.println("AIC: "+ asiaLCM4.getLoglikelihood(data));
            System.out.println("LL: "+ asiaLCM4.getAICScore(data));
            //showCTPs(asiaLCM4);

            System.out.println("------------------------------");
            System.out.println("-------- Asia 2 LTM (2, 2) --------");
            System.out.println("------------------------------");
            LTM handMadeAsia2LTM = handMade2LTM(data);
            System.out.println("BIC: "+ handMadeAsia2LTM.getBICScore(data));
            System.out.println("AIC: "+ handMadeAsia2LTM.getLoglikelihood(data));
            System.out.println("LL: "+ handMadeAsia2LTM.getAICScore(data));

            System.out.println("------------------------------");
            System.out.println("-------- Bridged Islands --------");
            System.out.println("------------------------------");
            LTM biLTM = learnWithBI(data);
            System.out.println("BIC: "+ biLTM.getBICScore(data));
            System.out.println("AIC: "+ biLTM.getLoglikelihood(data));
            System.out.println("LL: "+ biLTM.getAICScore(data));

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static LTM handMade2LTM(DataSet data){
        LTM ltm = new LTM();
        BeliefNode variable22 = ltm.addNode(new Variable(2));
        BeliefNode variable30 = ltm.addNode(new Variable(2));

        Map<String, Variable> variableNamesMap = new HashMap<>();
        for (Variable v : data.getVariables())
            variableNamesMap.put(v.getName(), v);

        BeliefNode vVisitToAsia = ltm.addNode(variableNamesMap.get("vVisitToAsia"));
        BeliefNode vTuberculosis = ltm.addNode(variableNamesMap.get("vTuberculosis"));
        BeliefNode vSmoking = ltm.addNode(variableNamesMap.get("vSmoking"));
        BeliefNode vLungCancer = ltm.addNode(variableNamesMap.get("vLungCancer"));
        BeliefNode vTbOrCa = ltm.addNode(variableNamesMap.get("vTbOrCa"));
        BeliefNode vXRay = ltm.addNode(variableNamesMap.get("vXRay"));
        BeliefNode vBronchitis = ltm.addNode(variableNamesMap.get("vBronchitis"));
        BeliefNode vDyspnea = ltm.addNode(variableNamesMap.get("vDyspnea"));

        ltm.addEdge(vVisitToAsia, variable22);
        ltm.addEdge(vDyspnea, variable22);
        ltm.addEdge(vBronchitis, variable22);
        ltm.addEdge(vXRay, variable30);
        ltm.addEdge(vTbOrCa, variable30);
        ltm.addEdge(vLungCancer, variable30);
        ltm.addEdge(vSmoking, variable30);
        ltm.addEdge(vTuberculosis, variable30);
        ltm.addEdge(variable30, variable22);

        return learnParameters(ltm, data);
    }

    private static LTM learnWithBI(DataSet data) throws IOException{
        return learnParameters(new BridgedIslands().learnLTM(data), data);
    }

    private static LTM learnParameters(LTM ltm, DataSet data){

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

    private static void showCTPs(LTM ltm){
        for(Variable var: ltm.getVariables())
            System.out.println(ltm.getNode(var).getCpt().toString(1));
    }
}
