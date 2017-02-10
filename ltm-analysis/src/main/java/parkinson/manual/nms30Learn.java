package parkinson.manual;

import ferjorosa.io.newBifWriter;
import org.apache.commons.io.FilenameUtils;
import org.latlab.learner.ParallelEmLearner;
import org.latlab.model.BayesNet;
import org.latlab.model.LTM;
import org.latlab.util.DataSet;
import org.latlab.util.DataSetLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by equipo on 10/02/2017.
 */
public class nms30Learn {

    // Los datasets utilizados en este script deberian ser variantes del NMS30 con 12 estados, ya que se da por supuesto
    // que contará con una serie de atributos

    public static void learnAndSaveAllModels(){

        // Seleccionamos el directorio en el que se van a recoger todos los datasets
        String input_path = "data/parkinson/nms12";
        File[] inputFiles = new File(input_path).listFiles(x -> x.getName().endsWith(".arff")); // 3 archivos

        String output_path = "results/manual_learn/nms12/Domains";

        for (File inputFile : inputFiles) {
            try {
                if (inputFile.isFile()) {
                    //Create the DataSet
                    DataSet data = new DataSet(DataSetLoader.convert(input_path + inputFile.getName()));

                    System.out.println("------------------------------------------------------------------------------");
                    System.out.println("------------------------------------------------------------------------------");
                    System.out.println("------------------------------------------------------------------------------");

                    System.out.println("############## "+ data.getName() + " ############## \n");

                    // Learn the LTM
                    //LTM ltm = learnParameters(createDomainsLTM(data), data);

                    // Save it in BIF format
                    newBifWriter writer = new newBifWriter(new FileOutputStream(output_path + "DomSym_" + FilenameUtils.removeExtension(inputFile.getName()) + ".bif"), false);
                    //writer.write(ltm);
                }
            }catch(Exception e){
                System.out.println("Error with " + inputFile.getName());
                e.printStackTrace();
            }
        }
    }

    // Chow Liu with best root, hierarchical LTM
    private static LTM createDomainsLTM(ArrayList<LTM> domainIslands, DataSet dataSet){
        return null;
    }

    private static BayesNet createDomainsAugmentedLTM(ArrayList<LTM> domainIslands, DataSet dataSet){
        return null;
    }

    // Chow Liu Forest, with best Root in LTMs, no arcs between attributes
    private static BayesNet createDomainsLFM(ArrayList<LTM> domainIslands, DataSet dataSet){
        return null;
    }

    private static BayesNet createDomainsAugmentedLFM(ArrayList<LTM> domainIslands, DataSet dataSet){
        return null;
    }

    private static ArrayList<LTM> createDomainIslands(DataSet dataSet){
        return null;
    }

    /**
     *  Used to get the BIC and LL, because the parameters are deleted afterwards or not learned
     * @param ltm
     * @param data
     * @return
     */
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
