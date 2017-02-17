package ferjorosa.information;

import voltric.data.dataset.DiscreteDataCase;
import voltric.data.dataset.DiscreteDataSet;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * In my opinion this class shoul'nt have state. It should be an Object in Scala.
 *
 * This class counts with 2 "interface" methods (the ones accessed by the library's user) and another one that is on development
 * (which could be used in the marginals "computePairWiseMI" and "computePairWiseCMI")
 */
public class FrequencyCounter {

    // Metodo interfaz, es decir, el que se llama or parte del usuario
    public ArrayList<double[]> computeParallel(DiscreteDataSet data, List<DiscreteVariable> variables) {
        ParallelComputation c =
                new ParallelComputation(data, variables, 0, data.getNumberOfEntries());
        ForkJoinPool pool = new ForkJoinPool();
        // Intuyo que aqui
        pool.invoke(c);
        return c.frequencies;
    }
    // Metodo interfaz, es decir, el que se llama or parte del usuario
    public ArrayList<double[]> computeSequential(DiscreteDataSet data, List<DiscreteVariable> variables) {
        return computeFrequencies(data, variables, 0, data.getNumberOfEntries());
    }

    // Este método realmente es una marginalizacion del secuencial, ya que te permite calcular las frecuencias de un
    // intervalo de las variables
    public ArrayList<double[]> computeFrequencies(DiscreteDataSet data, List<DiscreteVariable> variables, int start, int end) {
        // the diagonal entries contain the frequencies of a single variable
        ArrayList<double[]> frequencies =
                new ArrayList<double[]>(variables.size());

        for(int i = 0; i<variables.size();i++){
            frequencies.add(new double[variables.size()]);
        }

        System.out.println("Initialized the map");


        ArrayList<DiscreteDataCase> cases = data.getData();
        for (int caseIndex = start; caseIndex < end; caseIndex++) {
            DiscreteDataCase c = cases.get(caseIndex);
            int[] states = c.getStates();
            double weight = c.getWeight();

            // find the indices of states that are greater than zero
            List<Integer> entries = new ArrayList<>(states.length);
            for (int s = 0; s < states.length; s++) {
                if (states[s] > 0) {
                    entries.add(s);
                }
            }

            // update the single and joint counts
            for (int i : entries) {
                //	int iInVariables = idMappingFromDataToVariables[i];
                int iInVariables = i;
                if (iInVariables < 0)
                    continue;

                for (int j : entries) {
                    //int jInVariables = idMappingFromDataToVariables[j];
                    int jInVariables = j;
                    if (jInVariables < 0)
                        continue;


                    double freq = frequencies.get(iInVariables)[jInVariables];
                    freq += weight;
                    frequencies.get(iInVariables)[jInVariables]=freq;

                }
            }
        }

        return frequencies;
    }

    @SuppressWarnings("serial")
    private class ParallelComputation extends RecursiveAction {

        private final int start;
        private final int end;
        private DiscreteDataSet data; // Esto seria una val en Scala (final en java solo sirve para las referencias, deberia ser immutable tmb)
        private List<DiscreteVariable> variables; // Esto seria una val en Scala (final en java solo sirve para las referencias, deberia ser immutable tmb)
        private static final int THRESHOLD = 500;
        private ArrayList<double[]> frequencies;

        private ParallelComputation(DiscreteDataSet data, List<DiscreteVariable> variables, int start, int end) {
            this.start = start;
            this.end = end;
            this.data = data;
            this.variables = variables;
        }

        private void computeDirectly() {
            frequencies = computeFrequencies(data, variables, start, end);
        }

        @Override
        protected void compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                computeDirectly();
                return;
            }

            //TODO: Dual core?
            int split = length / 2;
            ParallelComputation c1 =
                    new ParallelComputation(data, variables, start, start + split);
            ParallelComputation c2 =
                    new ParallelComputation(data, variables, start + split, end);
            invokeAll(c1, c2);

            // This is not very efficient for combining the results
            // from subtasks.
            frequencies = c1.frequencies;
            for (int i = 0; i < frequencies.size();i++) {
                for (int j = 0; j < frequencies.get(i).length; j++) {
                    double t1 = frequencies.get(i)[j];
                    double t2 = c2.frequencies.get(i)[j];
                    frequencies.get(i)[j] = t1+t2;
                }
            }
        }
    }
}
