package ferjorosa.ltm.learning.structure;

import ferjorosa.ltm.learning.parameters.LTM_Learner;
import voltric.data.dataset.DiscreteDataSet;
import voltric.model.BayesNet;
import voltric.model.BeliefNode;
import voltric.model.LTM;
import voltric.variables.DiscreteVariable;

import java.util.Collection;

/**
 * Especificamente creado para los SharedLTMs
 */
public class BayesNet_CardinalitySearch {

    public static BayesNet globalBestCardinalityIncrease(BayesNet network, Collection<BeliefNode> latentNodes, DiscreteDataSet dataSet, int max){
        for(BeliefNode latentNode: latentNodes)
            if(!network.containsNode(latentNode))
                throw new IllegalArgumentException("The latent nodes don't belong to the network");


    }

    // TODO: Develop a latentVars array for bayes net too, so it can be tested if the argument node is a LV
    public static BayesNet bestCardinalityIncrease(BayesNet network, BeliefNode node, DiscreteDataSet dataSet, int max){
        if(!network.containsNode(node))
            throw new IllegalArgumentException("The argument node should be part of the Bayes net");

        BayesNet bestBayesNet = null;

        DiscreteVariable latentVar = node.getVariable();

        for(int i = 0; i <= max - latentVar.getCardinality() ; i++ ){

            BayesNet newBayesNet = network.increaseCardinality(latentVar, i);
            newBayesNet = LTM_Learner.learnParameters(newBayesNet, dataSet);

            if(bestLTM == null)
                bestLTM = newLTM;
            else if(bestLTM.getBICScore(dataSet) < newLTM.getBICScore(dataSet)) { // BIC score is a negative score
                bestLTM = newLTM;
            }
        }

        return bestLTM;
    }
}
