package parkinson;

import org.latlab.clustering.BridgedIslands;
import org.latlab.io.bif.BifWriter;
import org.latlab.learner.ParallelEmLearner;
import org.latlab.model.LTM;
import org.latlab.util.DataSet;
import org.latlab.util.DataSetLoader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by equipo on 08/02/2017.
 */
public class BI_Learn {

    public static void main(String[] args){
        try {
            String inFileName = "Asia_train";
            String outFileName = "Asia_train_3";

            String path = "data/ " + inFileName + ".arff";
            DataSet data = new DataSet(DataSetLoader.convert("data/"+ inFileName +".arff"));

            // Learn the LTM
            LTM ltm = learnBIModel(data);



            // Save it in BIF format
            saveModel(ltm, outFileName);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static LTM learnBIModel(DataSet dataSet) throws FileNotFoundException, UnsupportedEncodingException{

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

        BifWriter writer = new BifWriter(new FileOutputStream(outputPath), false);
        writer.write(ltm);
    }

    // TODO: Preguntar a Poon, creo que todavia no se puede.
    private static LTM loadModel(String fileName){
        return null;
    }

    private static LTM learnParameters(LTM ltm, DataSet data){

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
