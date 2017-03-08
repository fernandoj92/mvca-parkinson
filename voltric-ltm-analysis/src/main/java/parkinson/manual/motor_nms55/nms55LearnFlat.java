package parkinson.manual.motor_nms55;

import ferjorosa.io.newBifWriter;
import ferjorosa.learning.parameters.BayesNet_Learner;
import ferjorosa.learning.parameters.LTM_Learner;
import ferjorosa.learning.structure.LTM_CardinalitySearch;
import ferjorosa.ltm.creator.FlatLTM_creator;
import org.apache.commons.io.FilenameUtils;
import voltric.data.dataset.DiscreteDataSet;
import voltric.io.data.DataFileLoader;
import voltric.model.BayesNet;
import voltric.model.LTM;
import voltric.variables.DiscreteVariable;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Lo creo por separado para no haber colisiones en git, que hay commits no pusheados en otro PC
 */
public class nms55LearnFlat {

    public static void main(String[] args){
        learnAndSaveAllModels();
    }

    // Los datasets utilizados en este script deberian ser variantes del Motor 25, ya que se da por supuesto
    // que contará con una serie de atributos

    public static void learnAndSaveAllModels(){
        // Seleccionamos el directorio en el que se van a recoger todos los datasets
        String input_path = "data/parkinson/";
        File[] inputFiles = new File(input_path).listFiles(x -> x.getName().endsWith(".arff")); // 3 archivos

        String output_path = "results/manual_learn/motor_nms12/";

        for (File inputFile : inputFiles) {
            try {
                if (inputFile.isFile()) {
                    //Create the DiscreteDataSet
                    DiscreteDataSet data = new DiscreteDataSet(DataFileLoader.loadData(input_path + inputFile.getName(), DiscreteVariable.class));

                    System.out.println("------------------------------------------------------------------------------");
                    System.out.println("------------------------------------------------------------------------------");
                    System.out.println("------------------------------------------------------------------------------");

                    System.out.println("############## "+ data.getName() + " ############## \n");

                    // Produce an island (view) for each domain
                    ArrayList<LTM> domainIslands = createDomainIslands(data);

                    /** FLAT LTM 3 Domain islands */

                    // Create a flat LTM and learn its parameters
                    LTM flatLTM = LTM_Learner.learnParameters(createDomainsFlatLTM(domainIslands, data), data);
                    // Save it in BIF format
                    newBifWriter flatLTMwriter_3D = new newBifWriter(new FileOutputStream(output_path + "Flat_" + FilenameUtils.removeExtension(inputFile.getName()) + ".bif"), false);
                    flatLTMwriter_3D.write(flatLTM);
                    System.out.println("----- Flat LTM for "+ data.getName() + "has been learned ----- \n");
                    System.out.println("The resulting BIC score is: "+ flatLTM.getBICScore(data) +" \n");
                    System.out.println("The resulting AIC score is: "+ flatLTM.getAICcScore(data) +" \n");
                    System.out.println("The resulting LL score is: "+ flatLTM.getLoglikelihood(data) +" \n");

                }
            }catch(Exception e){
                System.out.println("Error with " + inputFile.getName());
                e.printStackTrace();
            }
        }
    }

    private static LTM createDomainsFlatLTM(ArrayList<LTM> domainIslands, DiscreteDataSet dataSet){
        LTM flatLTM = FlatLTM_creator.applyChowLiuWithBestRoot(domainIslands, dataSet);
        // Recordar que al ser manual no hay refinamiento del modelo (cambio de nodos entre particiones)
        return LTM_CardinalitySearch.globalBestCardinalityIncrease(flatLTM, dataSet, 12);
    }

    private static ArrayList<LTM> createDomainIslands(DiscreteDataSet dataSet){
        ArrayList<LTM> motorDomainIslands = createMotorDomainIslands(dataSet);
        ArrayList<LTM> nmsDomainIslands = createNMSDomainIslands(dataSet);

        ArrayList<LTM> allDomainIslands = new ArrayList<>();
        allDomainIslands.addAll(motorDomainIslands);
        allDomainIslands.addAll(nmsDomainIslands);

        return allDomainIslands;
    }

    private static ArrayList<LTM> createMotorDomainIslands(DiscreteDataSet dataSet){
        /** first we create the domains islands */
        ArrayList<LTM> domainIslands = new ArrayList<>();

        // Motor evaluation
        ArrayList<DiscreteVariable> motorEvalVariables = new ArrayList<>();
        motorEvalVariables.add(dataSet.getVariable("scm1rue"));
        motorEvalVariables.add(dataSet.getVariable("scm1lue"));
        motorEvalVariables.add(dataSet.getVariable("scm2rue"));
        motorEvalVariables.add(dataSet.getVariable("scm2lue"));
        motorEvalVariables.add(dataSet.getVariable("scm3rue"));
        motorEvalVariables.add(dataSet.getVariable("scm3lue"));
        motorEvalVariables.add(dataSet.getVariable("scm4rue"));
        motorEvalVariables.add(dataSet.getVariable("scm4lue"));
        motorEvalVariables.add(dataSet.getVariable("scm5rise"));
        motorEvalVariables.add(dataSet.getVariable("scm6post"));
        motorEvalVariables.add(dataSet.getVariable("scm7gait"));
        motorEvalVariables.add(dataSet.getVariable("scm8spee"));
        motorEvalVariables.add(dataSet.getVariable("scm9free"));
        motorEvalVariables.add(dataSet.getVariable("scm10swa"));
        LTM motorEvalIsland = LTM.createLCM(motorEvalVariables, 2); // Minimum cardinality
        domainIslands.add(motorEvalIsland);

        // Daily Living
        ArrayList<DiscreteVariable> dailyLivingVariables = new ArrayList<>();
        dailyLivingVariables.add(dataSet.getVariable("scm11spe"));
        dailyLivingVariables.add(dataSet.getVariable("scm12fee"));
        dailyLivingVariables.add(dataSet.getVariable("scm13dre"));
        dailyLivingVariables.add(dataSet.getVariable("scm14hyg"));
        dailyLivingVariables.add(dataSet.getVariable("scm15cha"));
        dailyLivingVariables.add(dataSet.getVariable("scm16wal"));
        dailyLivingVariables.add(dataSet.getVariable("scm17han"));
        LTM dailyLivingIsland = LTM.createLCM(dailyLivingVariables, 2); // Minimum cardinality
        domainIslands.add(dailyLivingIsland);

        // Motor complications
        ArrayList<DiscreteVariable> motorCompVariables = new ArrayList<>();
        motorCompVariables.add(dataSet.getVariable("scm18dpr"));
        motorCompVariables.add(dataSet.getVariable("scm19dsv"));
        motorCompVariables.add(dataSet.getVariable("scm20fpr"));
        motorCompVariables.add(dataSet.getVariable("scm21fsv"));
        LTM motorCompIsland = LTM.createLCM(motorCompVariables, 2); // Minimum cardinality
        domainIslands.add(motorCompIsland);

        return domainIslands;
    }

    private static ArrayList<LTM> createNMSDomainIslands(DiscreteDataSet dataSet){
        /** first we create the domains islands */
        ArrayList<LTM> domainIslands = new ArrayList<>();

        // d1 - Cardiovascular
        ArrayList<DiscreteVariable> d1Variables = new ArrayList<>();
        d1Variables.add(dataSet.getVariable("d1_lightheaded"));
        d1Variables.add(dataSet.getVariable("d1_fainting"));
        LTM d1Island = LTM.createLCM(d1Variables, 2); // Minimum cardinality
        domainIslands.add(d1Island);

        // d2 - Sleep/fatigue
        ArrayList<DiscreteVariable> d2Variables = new ArrayList<>();
        d2Variables.add(dataSet.getVariable("d2_drowsiness"));
        d2Variables.add(dataSet.getVariable("d2_fatigue"));
        d2Variables.add(dataSet.getVariable("d2_insomnia"));
        d2Variables.add(dataSet.getVariable("d2_rls"));
        LTM d2Island = LTM.createLCM(d2Variables, 2); // Minimum cardinality
        domainIslands.add(d2Island);

        // d3 - Mood/apathy
        ArrayList<DiscreteVariable> d3Variables = new ArrayList<>();
        d3Variables.add(dataSet.getVariable("d3_loss_interest"));
        d3Variables.add(dataSet.getVariable("d3_loss_activities"));
        d3Variables.add(dataSet.getVariable("d3_anxiety"));
        d3Variables.add(dataSet.getVariable("d3_depression"));
        d3Variables.add(dataSet.getVariable("d3_flat_affect"));
        d3Variables.add(dataSet.getVariable("d3_loss_pleasure"));
        LTM d3Island = LTM.createLCM(d3Variables, 2);
        domainIslands.add(d3Island);

        // d4 - Perception/hallucination
        ArrayList<DiscreteVariable> d4Variables = new ArrayList<>();
        d4Variables.add(dataSet.getVariable("d4_hallucination"));
        d4Variables.add(dataSet.getVariable("d4_delusion"));
        d4Variables.add(dataSet.getVariable("d4_diplopia"));
        LTM d4Island = LTM.createLCM(d4Variables, 2);
        domainIslands.add(d4Island);

        // d5 - Attentio/memory
        ArrayList<DiscreteVariable> d5Variables = new ArrayList<>();
        d5Variables.add(dataSet.getVariable("d5_loss_concentration"));
        d5Variables.add(dataSet.getVariable("d5_forget_explicit"));
        d5Variables.add(dataSet.getVariable("d5_forget_implicit"));
        LTM d5Island = LTM.createLCM(d5Variables, 2);
        domainIslands.add(d5Island);

        // d6 - Gastrointestinal
        ArrayList<DiscreteVariable> d6Variables = new ArrayList<>();
        d6Variables.add(dataSet.getVariable("d6_drooling"));
        d6Variables.add(dataSet.getVariable("d6_swallowing"));
        d6Variables.add(dataSet.getVariable("d6_constipation"));
        LTM d6Island = LTM.createLCM(d6Variables, 2);
        domainIslands.add(d6Island);

        // d7 - Urinary
        ArrayList<DiscreteVariable> d7Variables = new ArrayList<>();
        d7Variables.add(dataSet.getVariable("d7_urinary_urgency"));
        d7Variables.add(dataSet.getVariable("d7_urinary_frequency"));
        d7Variables.add(dataSet.getVariable("d7_nocturia"));
        LTM d7Island = LTM.createLCM(d7Variables, 2);
        domainIslands.add(d7Island);

        // d8 - Sexual
        ArrayList<DiscreteVariable> d8Variables = new ArrayList<>();
        d8Variables.add(dataSet.getVariable("d8_sex_drive"));
        d8Variables.add(dataSet.getVariable("d8_sex_dysfunction"));
        LTM d8Island = LTM.createLCM(d8Variables, 2);
        domainIslands.add(d8Island);

        // d9 - Miscellaneous
        ArrayList<DiscreteVariable> d9Variables = new ArrayList<>();
        d9Variables.add(dataSet.getVariable("d9_unexplained_pain"));
        d9Variables.add(dataSet.getVariable("d9_taste_smell"));
        d9Variables.add(dataSet.getVariable("d9_weight_change"));
        d9Variables.add(dataSet.getVariable("d9_sweating"));
        LTM d9Island = LTM.createLCM(d9Variables, 2);
        domainIslands.add(d9Island);

        return domainIslands;
    }
}
