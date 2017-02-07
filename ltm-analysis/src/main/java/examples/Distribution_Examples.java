package examples;

import org.latlab.learner.MleLearner;
import org.latlab.model.BayesNet;
import org.latlab.model.BeliefNode;
import org.latlab.util.DataSet;
import org.latlab.util.DataSetLoader;
import org.latlab.util.Function;
import org.latlab.util.Variable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by equipo on 16/01/2017.
 */
public class Distribution_Examples {

    public static void main(String[] args){
        BayesNet sprinklerNetwork = mleSprinklerFull();
        //playWithSprinklerDistributions(mleSprinklerFull());
        testConditionalDivide(sprinklerNetwork);
    }

    private static void testConditionalDivide(BayesNet network){

        BeliefNode cloudy = network.getNodeByName("cloudy");
        BeliefNode sprinkler = network.getNodeByName("sprinkler");
        BeliefNode rain = network.getNodeByName("rain");
        BeliefNode wetGrass = network.getNodeByName("wetGrass");

        // P(S,R|C) == P(R,S|C)
        Function jointSprinklerRain = sprinkler.getCpt().times(rain.getCpt());
        System.out.println(jointSprinklerRain.toString(1));

        // P(S,R,C) = P(S,R|C) P(C)
        Function jointRainSprinklerCloudy = rain.getCpt().times(sprinkler.getCpt()).times(cloudy.getCpt());
        System.out.println(jointRainSprinklerCloudy.toString(1));


    }

    private static void playWithSprinklerDistributions(BayesNet network){

        BeliefNode cloudy = network.getNodeByName("cloudy");
        BeliefNode sprinkler = network.getNodeByName("sprinkler");
        BeliefNode rain = network.getNodeByName("rain");
        BeliefNode wetGrass = network.getNodeByName("wetGrass");

        System.out.println("------------- Cloudy -------------");
        System.out.println(cloudy.getCpt().toString(1));

        System.out.println("------------- Sprinkler -------------");
        System.out.println(sprinkler.getCpt().toString(1));

        System.out.println("------- Joint(Cloudy, Sprinkler) -------");
        Function joint = cloudy.getCpt().times(sprinkler.getCpt());
        System.out.println(joint.toString(1));

        System.out.println("------- Conditional(Sprinkler | Cloudy) -------");
        // Llama a Function2D.myDivide(function)
        Function conditionalSprinkler = joint.myDivide(cloudy.getCpt());
        // Llama a Function2D.divide(function)
        joint.divide(cloudy.getCpt());
        System.out.println(joint.toString(1));
        System.out.println(conditionalSprinkler.toString(1));

        System.out.println("------------- Full Joint -------------");
        Function fullJoint =
                cloudy.getCpt()
                .times(sprinkler.getCpt())
                .times(rain.getCpt())
                .times(wetGrass.getCpt());
        System.out.println(fullJoint.toString(1));

        System.out.println("------------- Full Joint | Cloudy -------------");
        // Llama a Function.myDivide(function)
        Function conditionalFullJoint = fullJoint.myDivide(cloudy.getCpt());
        // Llama a Function.divide(function)
        fullJoint.divide(cloudy.getCpt());
        System.out.println(fullJoint.toString(1));
        System.out.println(conditionalFullJoint.toString(1));
    }

    private static BayesNet mleSprinklerFull(){

        BayesNet network = new BayesNet("network");

        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/sprinklerData300.arff"));

            Map<String, Variable> variableNamesMap = new HashMap<>();
            for(Variable v: data.getVariables())
                variableNamesMap.put(v.getName(),v);

            BeliefNode cloudy = network.addNode(variableNamesMap.get("cloudy"));
            BeliefNode sprinkler = network.addNode(variableNamesMap.get("sprinkler"));
            BeliefNode rain = network.addNode(variableNamesMap.get("rain"));
            BeliefNode wetGrass = network.addNode(variableNamesMap.get("wetGrass"));

            network.addEdge(sprinkler, cloudy);
            network.addEdge(rain, cloudy);
            network.addEdge(wetGrass, sprinkler);
            network.addEdge(wetGrass, rain);
/*
            for(Variable var: data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
*/
            MleLearner.computeMle(network, data);
/*
            for(Variable var: data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
*/
        }catch(Exception e){
            e.printStackTrace();
        }

        return network;
    }
}
