package parkinson;

import ferjorosa.io.newBifWriter;
import org.apache.commons.io.FilenameUtils;
import org.latlab.model.LTM;
import org.latlab.util.DataSet;
import org.latlab.util.DataSetLoader;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by equipo on 09/02/2017.
 */
public class LCM_Learn {

    //  TODO: do not use until it is able to choose the best cardinality
    public static void learnAndSaveAllModels(){
        // Seleccionamos el directorio en el que se van a recoger todos los
        String input_path = "data/automatic_learn";
        File[] inputFiles = new File(input_path).listFiles(x -> x.getName().endsWith(".arff"));

        String output_path = "results/automatic_learn/LCM/";

        for (File inputFile : inputFiles) {
            try {
                if (inputFile.isFile()) {
                    //Create the DataSet
                    DataSet data = new DataSet(DataSetLoader.convert(input_path + "/" + inputFile.getName()));

                    // Learn the LTM
                    LTM ltm = learnBestLCM(data);

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

    //
    public static LTM learnBestLCM(DataSet dataSet){
        Boolean bic_keep_improve = true;
        int cardinality = 2;

        LTM bestLCM = learnLCM(dataSet, cardinality);
        LTM currentLCM = bestLCM;

        while(bic_keep_improve){
            cardinality++;
            currentLCM = learnLCM(dataSet, cardinality);
            if(currentLCM.getBICScore(dataSet) < bestLCM.getBICScore(dataSet))
                bestLCM = currentLCM;
            else
                bic_keep_improve = false;
        }
        return bestLCM;
    }

    private static LTM learnLCM(DataSet dataSet, int cardinality){
        return LTM.createLCM(dataSet.getVariables(), cardinality);
    }


}
