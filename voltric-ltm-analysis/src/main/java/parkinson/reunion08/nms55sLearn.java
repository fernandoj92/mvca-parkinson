package parkinson.reunion08;

import ferjorosa.io.newBifWriter;
import ferjorosa.learning.parameters.LTM_Learner;
import ferjorosa.learning.structure.LTM_CardinalitySearch;
import org.apache.commons.io.FilenameUtils;
import voltric.data.dataset.DiscreteDataSet;
import voltric.io.data.DataFileLoader;
import voltric.io.model.bif.BifWriter;
import voltric.model.BeliefNode;
import voltric.model.LTM;
import voltric.variables.DiscreteVariable;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by equipo on 02/03/2017.
 */
public class nms55sLearn {

    public static void main(String[] args){
        learnAndSaveAllModels();
    }

    public static void learnAndSaveAllModels(){
        // Seleccionamos el directorio en el que se van a recoger todos los datasets
        String input_path = "data/reunion 08-03/";
        File[] inputFiles = new File(input_path).listFiles(x -> x.getName().endsWith(".arff")); // 3 archivos

        String output_path = "results/reunion 08-03/";

        for (File inputFile : inputFiles) {
            try {
                if (inputFile.isFile()) {
                    //Create the DiscreteDataSet
                    DiscreteDataSet data = new DiscreteDataSet(DataFileLoader.loadData(input_path + inputFile.getName(), DiscreteVariable.class));

                    System.out.println("------------------------------------------------------------------------------");
                    System.out.println("------------------------------------------------------------------------------");
                    System.out.println("------------------------------------------------------------------------------");

                    System.out.println("############## "+ data.getName() + " ############## \n");

                    /** MULTI-LEVEL LTM */
                    // Create a multi-level LTM and learn its parameters
                    LTM multiLevelLTM = LTM_Learner.learnParameters(createMultiLevelLTM(data), data);
                    // Save it in BIF format
                    BifWriter multiLevelLTMWriter = new BifWriter(new FileOutputStream(output_path + "Old_Multilevel_" + FilenameUtils.removeExtension(inputFile.getName()) + ".bif"), false);
                    multiLevelLTMWriter.write(multiLevelLTM);
                    System.out.println("----- Multi-level LTM for "+ data.getName() + "has been learned ----- \n");
                    System.out.println("The resulting BIC score is: "+ multiLevelLTM.getBICScore(data) +" \n");
                    System.out.println("The resulting AIC score is: "+ multiLevelLTM.getAICcScore(data) +" \n");
                    System.out.println("The resulting LL score is: "+ multiLevelLTM.getLoglikelihood(data) +" \n");
                }
            }catch(Exception e){
                System.out.println("Error with " + inputFile.getName());
                e.printStackTrace();
            }
        }
    }

    private static LTM createMultiLevelLTM(DiscreteDataSet dataSet){
        /** Creamos 3 LVs. Una para cada vista y una general */

        // 1. Inicializamos el multiLvelLTM
        LTM multiLevelLTM = new LTM();

        // 2. Creamos las islas Motor25 y Nms30
        ArrayList<LTM> motorIslands = create3MotorDomainIslands(dataSet);
        ArrayList<LTM> nmsIslands = createNMSDomainIslands(dataSet);

        // 2.1 - Añadimos las islas 'Motor25' cuya root sera motorRoot
        for(LTM island: motorIslands) {
            multiLevelLTM = multiLevelLTM.addDisconnectedLTM(island);
        }

        // 2.2 - Añadimos las islas 'Nms30' cuya root sera nmsRoot
        for(LTM island: nmsIslands) {
            multiLevelLTM = multiLevelLTM.addDisconnectedLTM(island);
        }

        // 3. Añadimos las 3 variables latentes (LVs) con cardinalidad = 2
        BeliefNode motorNmsRoot = multiLevelLTM.addNode(new DiscreteVariable(2));
        BeliefNode motorRoot = multiLevelLTM.addNode(new DiscreteVariable(2));
        BeliefNode nmsRoot = multiLevelLTM.addNode(new DiscreteVariable(2));

        // 3.1 - Conectamos las variables latentes con sus islas

        // Motor
        for(LTM island: motorIslands) {
            String islandRootName = island.getRoot().getName();
            BeliefNode islandRootNode = multiLevelLTM.getNodeByName(islandRootName);
            multiLevelLTM.addEdge(islandRootNode, motorRoot);
        }

        // NMS
        for(LTM island: nmsIslands) {
            String islandRootName = island.getRoot().getName();
            BeliefNode islandRootNode = multiLevelLTM.getNodeByName(islandRootName);
            multiLevelLTM.addEdge(islandRootNode, nmsRoot);
        }

        // 3.2 - Creamos los edges de 'motorNmsRoot' a las 2 roots especificas motorRoot y nmsRoot
        multiLevelLTM.addEdge(motorRoot, motorNmsRoot);
        multiLevelLTM.addEdge(nmsRoot, motorNmsRoot);

        // 4. Parametrización aleatoria y aprendizaje de la cardinalidad y parametros
        multiLevelLTM.randomlyParameterize();
        multiLevelLTM = LTM_Learner.learnParameters(multiLevelLTM, dataSet);
        return LTM_CardinalitySearch.globalBestCardinalityIncrease(multiLevelLTM, dataSet, 10);
    }

    private static ArrayList<LTM> create3MotorDomainIslands(DiscreteDataSet dataSet){
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
