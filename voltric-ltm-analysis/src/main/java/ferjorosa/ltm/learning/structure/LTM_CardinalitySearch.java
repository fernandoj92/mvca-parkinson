package ferjorosa.ltm.learning.structure;

import ferjorosa.ltm.learning.parameters.LTM_Learner;
import voltric.data.dataset.DiscreteDataSet;
import voltric.model.LTM;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Este metodo basicamente tiene 3 metodos que son:
 * - el de buscar la mejor cardinalidad en un intervalo (min 2)
 * - El de reducir la cardinalidad de sus LVs de forma greedy (min 2)
 * - El de aumentar la cardinaliad de sus LVs de formar greedy (min 2)
 */
// TODO: Revisar el paper de Liu, el pao final de revisar el flat LTM
public class LTM_CardinalitySearch {

    // Best cardinality increase from 2 to max for all the variables of the LTM
    // But with each increase, the full LTM is relearned (its parameters)
    public static LTM globalBestCardinalityIncrease(LTM ltm, DiscreteDataSet dataSet, int max) {

        // Reverse the latVarsFrom
        List<DiscreteVariable> latVarsFrombottom = new ArrayList<>(ltm.getLatVarsfromTop());
        Collections.reverse(latVarsFrombottom);

        LTM bestCardinalityIncreased = ltm.clone();

        for(DiscreteVariable latVar: latVarsFrombottom){
            bestCardinalityIncreased = bestCardinalityIncrease(bestCardinalityIncreased, latVar, dataSet, max);
        }

        return bestCardinalityIncreased;
    }

    // Best cardinality increase from 2 to max for the specific variable of the LTM
    // But with each increase, the full LTM is relearned (its parameters)
    private static LTM bestCardinalityIncrease(LTM ltm, DiscreteVariable latentVar, DiscreteDataSet dataSet, int max){

        if(!ltm.getLatVars().contains(latentVar))
            throw new IllegalArgumentException("The argument variable should be part of the LTM's latent variables set");

        LTM bestLTM = null;

        for(int i = 0; i <= max - latentVar.getCardinality() ; i++ ){

            LTM newLTM = ltm.increaseCardinality(latentVar, i);
            newLTM = LTM_Learner.learnParameters(newLTM, dataSet);

            if(bestLTM == null)
                bestLTM = newLTM;
            else if(bestLTM.getBICScore(dataSet) < newLTM.getBICScore(dataSet)) { // BIC score is a negative score
                bestLTM = newLTM;
            }
        }

        return bestLTM;
    }

    // TODO: Quizas no merece la pena este metodo porque es un caso especifico del bestCardinalityIncrease
    // Busca la mejor cardinalidad para un LCM desde su cardinalidad al max
    private static LTM bestCardinalityIncreaseLCM(LTM lcm, DiscreteDataSet dataSet, int max){

        if(lcm.getLatVars().size() > 1)
            throw new IllegalArgumentException("It has to be a LCM. Only 1 latent var allowed (the root)");

        DiscreteVariable lcmRoot = lcm.getRoot().getVariable();
        LTM bestLCM = null;

        for(int i = 0; i <= max - lcmRoot.getCardinality() ; i++ ){
            LTM newLCM = LTM_Learner.learnParameters(lcm.increaseCardinality(lcmRoot, i), dataSet);
            if(bestLCM == null)
                bestLCM = newLCM;
            else if(bestLCM.getBICScore(dataSet) > newLCM.getBICScore(dataSet))
                bestLCM = newLCM;
        }

        return bestLCM;
    }

    // From 2 to max. Search for the best cardinality value updating its value on batch, to avoid multiple full-EM executions.
    public static LTM localBestCardinalityIncrease(LTM ltm, DiscreteDataSet dataSet, int max) {

        return null;

    }

    public static LTM greedyCardinalityIncrease(LTM ltm, DiscreteDataSet dataSet) {
        return null;
    }

    public static LTM greedyCardinalityDecrease(LTM ltm, DiscreteDataSet dataSet) {
        return null;
    }

    public static LTM multiLevelBestCardinalityIncrease(LTM ltm, DiscreteDataSet dataSet){

        // Reverse the latVarsFrom
        List<DiscreteVariable> latVarsFrombottom = new ArrayList<>(ltm.getLatVarsfromTop());
        Collections.reverse(latVarsFrombottom);

        ArrayList<LTM> baseIslands = new ArrayList<>();

        for(DiscreteVariable latentVar: latVarsFrombottom){
            LTM subTree = ltm.getSubTree(latentVar);
            if(subTree.getLatVars().size() == 1)
                baseIslands.add(subTree);
        }

        ArrayList<LTM> improvedIslands = new ArrayList<>();

        for(LTM island: baseIslands){
            LTM bestCardinalityIncreaseIsland = bestCardinalityIncreaseLCM(island, dataSet, 10);
            improvedIslands.add(LTM_Learner.learnParameters(bestCardinalityIncreaseIsland, dataSet));
        }



        return null;
    }


}
