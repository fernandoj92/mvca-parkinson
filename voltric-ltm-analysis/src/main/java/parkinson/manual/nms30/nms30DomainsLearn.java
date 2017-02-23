package parkinson.manual.nms30;

import ferjorosa.io.newBifWriter;
import ferjorosa.ltm.creator.FlatLTM_creator;
import ferjorosa.ltm.learning.parameters.LTM_Learner;
import org.apache.commons.io.FilenameUtils;
import voltric.data.dataset.DiscreteDataSet;
import voltric.io.data.DataFileLoader;
import voltric.model.BayesNet;
import voltric.model.BeliefNode;
import voltric.model.LTM;
import voltric.variables.DiscreteVariable;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by equipo on 10/02/2017.
 */
public class nms30DomainsLearn {

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
                    //Create the DiscreteDataSet
                    DiscreteDataSet data = new DiscreteDataSet(DataFileLoader.loadData(input_path + inputFile.getName(), DiscreteVariable.class));

                    System.out.println("------------------------------------------------------------------------------");
                    System.out.println("------------------------------------------------------------------------------");
                    System.out.println("------------------------------------------------------------------------------");

                    System.out.println("############## "+ data.getName() + " ############## \n");

                    // Produce an island (view) for each domain
                    ArrayList<LTM> domainIslands = createDomainIslands(data);

                    // Create a flat LTM and learn its parameters
                    LTM flatLTM = LTM_Learner.learnParameters(createDomainsFlatLTM(domainIslands, data), data);

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

    private static ArrayList<LTM> createDomainIslands(DiscreteDataSet dataSet){
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
        d1Variables.add(dataSet.getVariable("d8_sex_drive"));
        d1Variables.add(dataSet.getVariable("d8_sex_dysfunction"));
        LTM d8Island = LTM.createLCM(d8Variables, 2);
        domainIslands.add(d8Island);

        // d9 - Miscellaneous
        ArrayList<DiscreteVariable> d9Variables = new ArrayList<>();
        d2Variables.add(dataSet.getVariable("d9_unexplained_pain"));
        d2Variables.add(dataSet.getVariable("d9_taste_smell"));
        d2Variables.add(dataSet.getVariable("d9_weight_change"));
        d2Variables.add(dataSet.getVariable("d9_sweating"));
        LTM d9Island = LTM.createLCM(d9Variables, 2);
        domainIslands.add(d9Island);

        return domainIslands;
    }

    /** =============================================================================== */
    /** ======================== LTM creation Strategies ============================== */
    /** =============================================================================== */

    /**
     *
     * @param domainIslands
     * @param dataSet
     * @return
     */
    // Chow Liu with best root, flat LTM
    // TODO: ALL THE LVS HAVE CARDINALITY = 2
    private static LTM createDomainsFlatLTM(ArrayList<LTM> domainIslands, DiscreteDataSet dataSet){
        LTM flatLTM = FlatLTM_creator.applyChowLiuWithBestRoot(domainIslands, dataSet);



        return flatLTM;
    }

    /**
     *
     * @param domainIslands
     * @return
     */
    // Basicamente es una multi-level de 2 niveles:
    // El primer nivel es la variable latente maxima cuyos hijos son las LVs del segundo nivel
    // Y el segundo nivel esta formado por las diferentes islas, donde los hijos de cada LV son MVs
    private static LTM createManualMultiLevelLTM(ArrayList<LTM> domainIslands){

        LTM multiLevelLTM = new LTM();
        DiscreteVariable level1Root = new DiscreteVariable(2); // start with cardinality = 2
        BeliefNode level1RootBeliefNode = multiLevelLTM.addNode(level1Root);

        for(LTM island: domainIslands) {
            multiLevelLTM.addDisconnectedLTM(island);
            String islandRootName = island.getRoot().getName();
            multiLevelLTM.addEdge(multiLevelLTM.getNode(islandRootName), level1RootBeliefNode);
        }

        // Una vez ya hemos generado el LTM multi-level, aplicamos una búsqueda local de la cardinalidad
        // que va de abajo a arriba, es decir, busca primero la mejor cardinalidad de cada isla por separado
        // y luego busca la mejor cardinalidad del primer nivel
        // TODO: En este caso como no hemos resuelto aun el problema de completar los datos tipo EM, el primer nivel utiliza EM global



        return multiLevelLTM;
    }

    /**
     *
     * @param domainIslands
     * @param dataSet
     * @return
     */
    private static BayesNet createDomainsAugmentedLTM(ArrayList<LTM> domainIslands, DiscreteDataSet dataSet){
        return null;
    }

    /**
     *
     * @param domainIslands
     * @param dataSet
     * @return
     */
    // Chow Liu Forest, with best Root in LTMs, no arcs between attributes
    private static BayesNet createDomainsLFM(ArrayList<LTM> domainIslands, DiscreteDataSet dataSet){
        return null;
    }

    /**
     *
     * @param domainIslands
     * @param dataSet
     * @return
     */
    private static BayesNet createDomainsAugmentedLFM(ArrayList<LTM> domainIslands, DiscreteDataSet dataSet){
        return null;
    }

}
