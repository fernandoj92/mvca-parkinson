package parkinson.automatic;

import ferjorosa.io.newBifWriter;
import org.apache.commons.io.FilenameUtils;
import voltric.clustering.BridgedIslands;
import voltric.data.dataset.DiscreteDataSet;
import voltric.io.data.DataFileLoader;
import voltric.learner.ParallelEmLearner;
import voltric.model.LTM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by equipo on 08/02/2017.
 */
public class BI_Learn {

    public static void learnAndSaveAllModels() {
        // Seleccionamos el directorio en el que se van a recoger todos los
        String input_path = "data/automatic_learn";
        File[] inputFiles = new File(input_path).listFiles(x -> x.getName().endsWith(".arff"));

        String output_path = "results/automatic_learn/BridgedIslands/";

        for (File inputFile : inputFiles) {
            try {
                if (inputFile.isFile()) {
                    //Create the DiscreteDataSet
                    DiscreteDataSet data = new DiscreteDataSet(DataFileLoader.loadData(input_path + "/" + inputFile.getName()));

                    // Learn the LTM
                    LTM ltm = learnBIModel(data);

                    // Save it in BIF format
                    newBifWriter writer = new newBifWriter(new FileOutputStream(output_path + "BI_" + FilenameUtils.removeExtension(inputFile.getName()) + ".bif"), false);
                    writer.write(ltm);
                }
            }catch(Exception e){
                System.out.println("Error with " + inputFile.getName());
                e.printStackTrace();
            }
        }
    }

    private static LTM learnBIModel(DiscreteDataSet dataSet) throws FileNotFoundException, UnsupportedEncodingException {

        BridgedIslands bi = new BridgedIslands();
        LTM ltm = bi.learnLTM(dataSet);

        // Learn the parameters one more time (for the scores and sheet)
        ltm = learnParameters(ltm, dataSet);

        // Scores and sheet
        System.out.println(ltm.toString(1));
        System.out.println("BIC: "+ ltm.getBICScore(dataSet));
        System.out.println("AIC: "+ ltm.getLoglikelihood(dataSet));
        System.out.println("LL: "+ ltm.getAICScore(dataSet));

        return ltm;
    }

    private static void saveModel(LTM ltm, String fileName) throws FileNotFoundException, UnsupportedEncodingException{
        String outputPath = "results/" + fileName + ".bif";
        //ltm.saveAsBif(outputPath);

        newBifWriter writer = new newBifWriter(new FileOutputStream(outputPath), false);
        writer.write(ltm);
    }

    // TODO: Preguntar a Poon, creo que todavia no se puede.
    private static LTM loadModel(String fileName){
        return null;
    }

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
