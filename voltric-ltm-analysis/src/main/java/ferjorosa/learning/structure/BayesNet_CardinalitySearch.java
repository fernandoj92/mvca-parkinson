package ferjorosa.learning.structure;

import ferjorosa.learning.parameters.BayesNet_Learner;
import voltric.data.dataset.DiscreteDataSet;
import voltric.model.BayesNet;
import voltric.model.BeliefNode;
import voltric.variables.DiscreteVariable;

import java.util.Collection;

/**
 * Especificamente creado para los SharedLTMs
 */
public class BayesNet_CardinalitySearch {

    public static BayesNet globalBestCardinalityIncrease(BayesNet network, Collection<DiscreteVariable> latentNodes, DiscreteDataSet dataSet, int max){
        for(DiscreteVariable latentVar: latentNodes)
            if(!network.containsVar(latentVar))
                throw new IllegalArgumentException("The latent nodes don't belong to the network");

        BayesNet bestCardinalityIncreased = network.clone();

        for(DiscreteVariable latentVar: latentNodes){
            bestCardinalityIncreased = bestCardinalityIncrease(bestCardinalityIncreased, latentVar, dataSet, max);
        }

        return bestCardinalityIncreased;
    }

    // TODO: Develop a latentVars array for bayes net too, so it can be tested if the argument node is a LV
    public static BayesNet bestCardinalityIncrease(BayesNet network, DiscreteVariable latentVar, DiscreteDataSet dataSet, int max){
        if(!network.containsVar(latentVar))
            throw new IllegalArgumentException("The argument node should be part of the Bayes net");

        BayesNet bestBayesNet = null;

        for(int i = 0; i <= max - latentVar.getCardinality() ; i++ ){

            BayesNet newBayesNet = network.increaseCardinality(latentVar, i);
            newBayesNet = BayesNet_Learner.learnParameters(newBayesNet, dataSet);

            if(bestBayesNet == null)
                bestBayesNet = newBayesNet;
            else if(bestBayesNet.getBICScore(dataSet) < newBayesNet.getBICScore(dataSet)) { // BIC score is a negative score
                bestBayesNet = newBayesNet;
            }
        }

        return bestBayesNet;
    }
}
