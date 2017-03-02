package parkinson.automatic;



import ferjorosa.io.newBifWriter;
import org.apache.commons.io.FilenameUtils;
import voltric.data.dataset.DiscreteDataSet;
import voltric.io.data.DataFileLoader;
import voltric.learner.ParallelEmLearner;
import voltric.model.LTM;
import voltric.variables.DiscreteVariable;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by equipo on 09/02/2017.
 */
public class LCM_Learn {

    public static void main(String[] args){
        learnAndSaveAllModels();
    }

    //  TODO: do not use until it is able to choose the best cardinality
    public static void learnAndSaveAllModels(){
        // Seleccionamos el directorio en el que se van a recoger todos los
        String input_path = "data/automatic_learn/";
        File[] inputFiles = new File(input_path).listFiles(x -> x.getName().endsWith(".arff"));

        String output_path = "results/automatic_learn/LCM/";

        for (File inputFile : inputFiles) {
            try {
                if (inputFile.isFile()) {
                    //Create the DiscreteDataSet
                    DiscreteDataSet data = new DiscreteDataSet(DataFileLoader.loadData(input_path + inputFile.getName(), DiscreteVariable.class));

                    System.out.println("------------------------------------------------------------------------------");
                    System.out.println("------------------------------------------------------------------------------");
                    System.out.println("------------------------------------------------------------------------------");

                    System.out.println("############## "+ data.getName() + " ############## \n");

                    // Learn the LTM
                    LTM ltm = learnBestLCMVaryingCardinality(data);

                    // Save it in BIF format
                    newBifWriter writer = new newBifWriter(new FileOutputStream(output_path + "LCM_" + FilenameUtils.removeExtension(inputFile.getName()) + ".bif"), false);
                    writer.write(ltm);
                }
            }catch(Exception e){
                System.out.println("Error with " + inputFile.getName());
                e.printStackTrace();
            }
        }
    }

    // Learns the best LCMusing a varying cardinality, not just a greedy carinality increasement
    public static LTM learnBestLCMVaryingCardinality(DiscreteDataSet dataSet){
        //Boolean bic_keep_improve = true;
        int cardinality = 10;

        LTM bestLCM = learnParameters(learnLCM(dataSet, cardinality), dataSet);
        bestLCM.setName("Best LCM");
        printResult(bestLCM, dataSet);
        LTM currentLCM = bestLCM;

        while(cardinality > 1){
            currentLCM = learnParameters(learnLCM(dataSet, cardinality), dataSet);
            currentLCM.setName("Current LCM");

            printResult(currentLCM, dataSet);

            if(currentLCM.getBICScore(dataSet) > bestLCM.getBICScore(dataSet))
                bestLCM = currentLCM;

            // Cardinality minus 1
            cardinality--;
        }
        System.out.println("-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-");
        System.out.println("Best LCM: ");
        printResult(bestLCM, dataSet);
        return bestLCM;
    }

    private static LTM learnLCM(DiscreteDataSet dataSet, int cardinality){
        return learnParameters(LTM.createLCM(dataSet.getVariables(), cardinality), dataSet);

    }

    private static void printResult(LTM ltm, DiscreteDataSet dataSet){
        System.out.println("========================");
        System.out.println(ltm.getName() + " wtih C="+ltm.getLatVarsfromTop().get(0).getCardinality());
        System.out.println("BIC: "+ ltm.getBICScore(dataSet));
        System.out.println("LL: "+ ltm.getLoglikelihood(dataSet));
        System.out.println("========================");
        System.out.println();
    }

    /**
     *  Used to get the BIC and LL, because the parameters are deleted afterwards or not learned
     * @param ltm
     * @param data
     * @return
     */
    private static LTM learnParameters(LTM ltm, DiscreteDataSet data){

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

}
