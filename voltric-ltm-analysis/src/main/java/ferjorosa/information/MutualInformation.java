package ferjorosa.information;

import voltric.data.dataset.DiscreteDataSet;
import voltric.util.Function;
import voltric.util.Utils;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Esto seria un object en Scala
 */
public class MutualInformation {

    /**
     * Returns all the pairwise MI values for the passed variables.
     *
     * @param data
     * @param variables
     * @return
     */
    public static Map<DiscreteVariable, Map<DiscreteVariable, Double>> computePairwise(DiscreteDataSet data, List<DiscreteVariable> variables){
        int numberOfVariables = variables.size();
        double totalWeight = data.getTotalWeight();
        ArrayList<double[]> f = new FrequencyCounter().computeSequential(data, variables);
        ArrayList<double[]> results = new ArrayList<double[]>(numberOfVariables);


        for(int i = 0; i<numberOfVariables;i++){
            results.add(new double[numberOfVariables]);
        }

        for (int i = 0; i < numberOfVariables; i++) {
            for (int j = i + 1; j < numberOfVariables; j++) {
                double[] pi = getMarginal(f.get(i)[i] / totalWeight);
                double[] pj = getMarginal(f.get(j)[j]/ totalWeight);

                double[][] pij = new double[2][2];
                pij[1][1] = f.get(i)[j] / totalWeight;
                pij[1][0] = pi[1] - pij[1][1];
                pij[0][1] = pj[1] - pij[1][1];
                pij[0][0] = 1 - pi[1] - pj[1] + pij[1][1];

                double mi = 0;
                for (int xi = 0; xi < 2; xi++) {
                    for (int xj = 0; xj < 2; xj++) {
                        if (pij[xi][xj] > 0) {
                            mi +=
                                    pij[xi][xj]
                                            * Math.log(pij[xi][xj]
                                            / (pi[xi] * pj[xj]));
                        }
                    }
                }


                results.get(i)[j] = mi;
                results.get(j)[i] = mi;
                assert !Double.isNaN(mi);
            }
        }
        return processMi(results, variables);
    }

    // TODO: No va a tener mucho uso, ya que la variable condicionante suele ser latente y por lo tanto se necesita aprender
    // la BN primero.
    public static double computeConditionalMutualInformation(DiscreteDataSet data, DiscreteVariable varX, DiscreteVariable varY, DiscreteVariable condVar){
        throw new RuntimeException("Not implemented yet");
    }

    public static double computePairwise(Function dist){
        return Utils.computeMutualInformation(dist);
    }

    public static double computeConditionalMutualInformation(Function dist, DiscreteVariable condVar) {
        return Utils.computeConditionalMutualInformation(dist, condVar);
    }

    // Used in computePairwise
    private static double[] getMarginal(double p_1) {
        double[] result = { 1 - p_1, p_1 };
        return result;
    }

    // Modified version of IslandFinder
    private static Map<DiscreteVariable, Map<DiscreteVariable, Double>> processMi(List<double[]> miArray, List<DiscreteVariable> vars) {
        // convert the array to map

        // initialize the data structure for pairwise MI
        Map<DiscreteVariable, Map<DiscreteVariable, Double>> mis = new HashMap<DiscreteVariable, Map<DiscreteVariable, Double>>(vars.size());

        for (int i = 0; i < vars.size(); i++) {
            double[] row = miArray.get(i);

            Map<DiscreteVariable, Double> map = new HashMap<DiscreteVariable, Double>(vars.size());
            for (int j = 0; j < vars.size(); j++) {
                map.put(vars.get(j), row[j]);
            }

            mis.put(vars.get(i), map);

            // to allow garbage collection
            miArray.set(i, null);
        }

        return mis;

    }
}
