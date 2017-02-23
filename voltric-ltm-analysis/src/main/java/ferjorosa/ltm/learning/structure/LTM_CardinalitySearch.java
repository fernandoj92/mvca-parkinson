package ferjorosa.ltm.learning.structure;

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

    public static LTM greedyCardinalityIncrease(LTM ltm, DiscreteDataSet dataSet) {
        return null;
    }

    public static LTM greedyCardinalityDecrease(LTM ltm, DiscreteDataSet dataSet) {
        return null;
    }

    public static LTM multiLevelBestcardinalityChange(LTM ltm, DiscreteDataSet dataSet){

        // Reverse the latVarsFrom
        List<DiscreteVariable> latVarsFrombottom = new ArrayList<>(ltm.getLatVarsfromTop());
        Collections.reverse(latVarsFrombottom);

        for(DiscreteVariable latentVar: latVarsFrombottom){
            ltm.getSubTree(latentVar);
        }

        return null;
    }

    /**
     * From 2 to max. Search for the best cardinality value
     *
     * @param ltm
     * @param dataSet
     * @param max
     * @return
     */
    public static LTM globalBestCardinalityChange(LTM ltm, DiscreteDataSet dataSet, int max) {

        return null;
    }

    /**
     * From 2 to max. Search for the best cardinality value updating its value on batch, to avoid multiple full-EM
     * executions.
     *
     * @param ltm
     * @param dataSet
     * @param max
     * @return
     */
    public static LTM localBestCardinalityChange(LTM ltm, DiscreteDataSet dataSet, int max) {

        return null;

    }

    // Busca la mejor cardinalidad para un LCM desde 2 al max
    private static LTM searchBestCardinalityLCM(LTM lcm, DiscreteDataSet dataSet, int max){

        if(lcm.getLatVars().size() > 1)
            throw new IllegalArgumentException("It has to be a LCM. Only 1 latent var allowed (the root)");

        LTM bestLCM = new LTM();

        for(int i = 2; 2 <= max ; i++ ){

        }
    }
}
