package examples;

import org.latlab.learner.EmLearner;
import org.latlab.learner.MleLearner;
import org.latlab.model.BayesNet;
import org.latlab.model.BeliefNode;
import org.latlab.model.LTM;
import org.latlab.util.DataSet;
import org.latlab.util.DataSetLoader;
import org.latlab.util.Variable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fer on 9/03/17.
 */
public class EM_Example_Asia {

    public static void main(String[] args){
        //asiaHiddenVisitComparison();
        asiaLCMcomparison();
    }

    private static void asiaHiddenVisitComparison(){

        System.out.println("---------------- MLE Asia with VisitAsia node not hidden ----------------");
        System.out.println("--------------------------------------------------------------------");
        mleAsiaFull();

        System.out.println("---------------- EM Asia with VisitAsia node hidden -----------------");
        System.out.println("--------------------------------------------------------------------");
        emAsiaVisitHidden();
    }

    private static void asiaLCMcomparison(){

        System.out.println("---------------- Asia variables with a hidden LCM root -----------------");
        System.out.println("--------------------------------------------------------------------");
        asiaLCMFull();
    }

    private static void mleAsiaFull(){

        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/Asia_train.arff"));

            Map<String, Variable> variableNamesMap = new HashMap<>();
            for (Variable v : data.getVariables())
                variableNamesMap.put(v.getName(), v);

            BayesNet network = new BayesNet("network");

            BeliefNode vVisitToAsia = network.addNode(variableNamesMap.get("vVisitToAsia"));
            BeliefNode vTuberculosis = network.addNode(variableNamesMap.get("vTuberculosis"));
            BeliefNode vSmoking = network.addNode(variableNamesMap.get("vSmoking"));
            BeliefNode vLungCancer = network.addNode(variableNamesMap.get("vLungCancer"));
            BeliefNode vTbOrCa = network.addNode(variableNamesMap.get("vTbOrCa"));
            BeliefNode vXRay = network.addNode(variableNamesMap.get("vXRay"));
            BeliefNode vBronchitis = network.addNode(variableNamesMap.get("vBronchitis"));
            BeliefNode vDyspnea = network.addNode(variableNamesMap.get("vDyspnea"));

            network.addEdge(vTuberculosis, vVisitToAsia);
            network.addEdge(vLungCancer, vSmoking);
            network.addEdge(vBronchitis, vSmoking);
            network.addEdge(vTbOrCa, vTuberculosis);
            network.addEdge(vTbOrCa, vLungCancer);
            network.addEdge(vDyspnea, vBronchitis);
            network.addEdge(vXRay, vTbOrCa);
            network.addEdge(vDyspnea, vTbOrCa);
/*
            for (Variable var : data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
*/
            MleLearner.computeMle(network, data);

            for (Variable var : network.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void emAsiaVisitHidden(){

        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/Asia_trainHidden.arff"));

            Map<String, Variable> variableNamesMap = new HashMap<>();
            for (Variable v : data.getVariables())
                variableNamesMap.put(v.getName(), v);

            BayesNet network = new BayesNet("network");

            BeliefNode vVisitToAsiaHidden = network.addNode(new Variable(2));// VisitToAsiaHidden con 2 estados tmb
            BeliefNode vTuberculosis = network.addNode(variableNamesMap.get("vTuberculosis"));
            BeliefNode vSmoking = network.addNode(variableNamesMap.get("vSmoking"));
            BeliefNode vLungCancer = network.addNode(variableNamesMap.get("vLungCancer"));
            BeliefNode vTbOrCa = network.addNode(variableNamesMap.get("vTbOrCa"));
            BeliefNode vXRay = network.addNode(variableNamesMap.get("vXRay"));
            BeliefNode vBronchitis = network.addNode(variableNamesMap.get("vBronchitis"));
            BeliefNode vDyspnea = network.addNode(variableNamesMap.get("vDyspnea"));

            network.addEdge(vTuberculosis, vVisitToAsiaHidden);
            network.addEdge(vLungCancer, vSmoking);
            network.addEdge(vBronchitis, vSmoking);
            network.addEdge(vTbOrCa, vTuberculosis);
            network.addEdge(vTbOrCa, vLungCancer);
            network.addEdge(vDyspnea, vBronchitis);
            network.addEdge(vXRay, vTbOrCa);
            network.addEdge(vDyspnea, vTbOrCa);
/*
            for (Variable var : data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
*/
            EmLearner emLearner = new EmLearner();
            emLearner.setMaxNumberOfSteps(100);
            emLearner.setNumberOfRestarts(50);
            emLearner.setThreshold(0.01);
            network = emLearner.em(network,data);

            for (Variable var : network.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void asiaLCMFull(){
        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/Asia_train.arff"));
            LTM network = LTM.createLCM(data.getVariables(),3);

            EmLearner emLearner = new EmLearner();
            emLearner.setMaxNumberOfSteps(100);
            emLearner.setNumberOfRestarts(50);
            emLearner.setThreshold(0.01);
            network = (LTM) emLearner.em(network,data);

            for (Variable var : network.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void asiaLCMHidden(){

    }
}
