package examples;

import org.latlab.learner.EmLearner;
import org.latlab.learner.MleLearner;
import org.latlab.model.BayesNet;
import org.latlab.model.BeliefNode;
import ferjorosa.data.DataSet;
import ferjorosa.data.DataSetLoader;
import org.latlab.util.Variable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Fernando on 07/01/2017.
 */
public class EM_Example {

    public static void main(String[] args){
        //sprinklerFullComparison();
        //sprinklerRainCloudyComparison();
        sprinklerLatentCloudyComparison();
    }

    // Devuelve lo mismo (yo creo que se da cuenta de que todo es observable asi que hace mle el EM)
    private static void sprinklerFullComparison(){

        System.out.println("---------------- MLE Sprinkler Full ----------------");
        System.out.println("---------------------------------------------------");
        mleSprinklerFull();

        System.out.println("---------------- EM Sprinkler Full ----------------");
        System.out.println("---------------------------------------------------");
        emSprinklerFull();
    }

    private static void sprinklerRainCloudyComparison(){

        System.out.println("---------------- MLE Sprinkler With Rain and Cloudy ----------------");
        System.out.println("--------------------------------------------------------------------");
        mleSprinklerRainCloudy();

        System.out.println("---------------- EM Sprinkler With Rain and Cloudy -----------------");
        System.out.println("--------------------------------------------------------------------");
        emSprinklerRainLatentCloudy();
    }

    private static void sprinklerLatentCloudyComparison(){

        System.out.println("---------------- MLE Sprinkler With Latent Cloudy ----------------");
        System.out.println("--------------------------------------------------------------------");
        mleSprinklerLatentCloudy();

        System.out.println("---------------- EM Sprinkler With Latent Cloudy -----------------");
        System.out.println("--------------------------------------------------------------------");
        emSprinklerLatentCloudy();
    }

    private static void mleSprinklerFull(){
        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/sprinklerData300.arff"));

            Map<String, Variable> variableNamesMap = new HashMap<>();
            for(Variable v: data.getVariables())
                variableNamesMap.put(v.getName(),v);

            BayesNet network = new BayesNet("network");

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

            for(Variable var: data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));


        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private static void emSprinklerFull() {

        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/sprinklerData300.arff"));

            Map<String, Variable> variableNamesMap = new HashMap<>();
            for (Variable v : data.getVariables())
                variableNamesMap.put(v.getName(), v);

            BayesNet network = new BayesNet("network");

            BeliefNode cloudy = network.addNode(variableNamesMap.get("cloudy"));
            BeliefNode sprinkler = network.addNode(variableNamesMap.get("sprinkler"));
            BeliefNode rain = network.addNode(variableNamesMap.get("rain"));
            BeliefNode wetGrass = network.addNode(variableNamesMap.get("wetGrass"));

            network.addEdge(sprinkler, cloudy);
            network.addEdge(rain, cloudy);
            network.addEdge(wetGrass, sprinkler);
            network.addEdge(wetGrass, rain);
/*
            for (Variable var : data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
*/
            EmLearner emLearner = new EmLearner();
            network = emLearner.em(network,data);

            for (Variable var : data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void mleSprinklerRainCloudy(){
        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/sprinklerData300_rain_cloudy.arff"));

            Map<String, Variable> variableNamesMap = new HashMap<>();
            for (Variable v : data.getVariables())
                variableNamesMap.put(v.getName(), v);

            BayesNet network = new BayesNet("network");

            BeliefNode cloudy = network.addNode(variableNamesMap.get("cloudy"));
            BeliefNode rain = network.addNode(variableNamesMap.get("rain"));

            network.addEdge(rain, cloudy);
/*
            for (Variable var : data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
*/
            MleLearner.computeMle(network, data);

            for (Variable var : data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void emSprinklerRainLatentCloudy(){

        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/sprinklerData300_rain.arff"));

            Map<String, Variable> variableNamesMap = new HashMap<>();
            for (Variable v : data.getVariables())
                variableNamesMap.put(v.getName(), v);

            // Latent variable
            Variable cloudyVar = new Variable(2);

            BayesNet network = new BayesNet("network");

            BeliefNode rain = network.addNode(variableNamesMap.get("rain"));
            BeliefNode cloudy = network.addNode(cloudyVar);

            network.addEdge(rain, cloudy);
/*
            for (Variable var : data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
*/
            EmLearner emLearner = new EmLearner();
            network = emLearner.em(network,data);

            for (Variable var : data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
            System.out.println(network.getNode(cloudyVar).getCpt().toString(1));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // Its the same as the full example
    private static void mleSprinklerLatentCloudy(){
        mleSprinklerFull();
    }

    private static void emSprinklerLatentCloudy(){

        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/sprinklerDataHidden.arff"));

            Map<String, Variable> variableNamesMap = new HashMap<>();
            for (Variable v : data.getVariables())
                variableNamesMap.put(v.getName(), v);

            // Latent variable
            Variable latentCloudyVar = new Variable(2);

            BayesNet network = new BayesNet("network");

            BeliefNode sprinkler = network.addNode(variableNamesMap.get("sprinkler"));
            BeliefNode rain = network.addNode(variableNamesMap.get("rain"));
            BeliefNode wetGrass = network.addNode(variableNamesMap.get("wetGrass"));
            BeliefNode latentCloudy = network.addNode(latentCloudyVar);

            network.addEdge(sprinkler, latentCloudy);
            network.addEdge(rain, latentCloudy);
            network.addEdge(wetGrass, sprinkler);
            network.addEdge(wetGrass, rain);
/*
            for (Variable var : data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
*/
            EmLearner emLearner = new EmLearner();
            network = emLearner.em(network,data);

            for (Variable var : data.getVariables())
                System.out.println(network.getNode(var).getCpt().toString(1));
            System.out.println(network.getNode(latentCloudyVar).getCpt().toString(1));

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
