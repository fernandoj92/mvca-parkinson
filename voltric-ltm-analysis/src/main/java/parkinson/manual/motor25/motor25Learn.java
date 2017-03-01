package parkinson.manual.motor25;

import ferjorosa.io.newBifWriter;
import ferjorosa.learning.parameters.BayesNet_Learner;
import ferjorosa.learning.structure.BayesNet_CardinalitySearch;
import ferjorosa.ltm.creator.FlatLTM_creator;
import ferjorosa.learning.parameters.LTM_Learner;
import ferjorosa.learning.structure.LTM_CardinalitySearch;
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
public class motor25Learn {

    public static void main(String[] args){
        learnAndSaveAllModels();
    }

    // Los datasets utilizados en este script deberian ser variantes del Motor 25, ya que se da por supuesto
    // que contará con una serie de atributos

    public static void learnAndSaveAllModels(){
        // Seleccionamos el directorio en el que se van a recoger todos los datasets
        String input_path = "data/parkinson/motor/";
        File[] inputFiles = new File(input_path).listFiles(x -> x.getName().endsWith(".arff")); // 3 archivos

        String output_path = "results/manual_learn/motor/Domains/";

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
                    ArrayList<LTM> domain2Islands = create2DomainIslands(data);
                    ArrayList<LTM> domain3Islands = create3DomainIslands(data);

                    /** FLAT LTM 2 Domain islands */

                    // Create a flat LTM and learn its parameters
                    LTM flatLTM_2D = LTM_Learner.learnParameters(createDomainsFlatLTM(domain2Islands, data), data);
                    // Save it in BIF format
                    newBifWriter flatLTMwriter_2D = new newBifWriter(new FileOutputStream(output_path + "Flat_2D_" + FilenameUtils.removeExtension(inputFile.getName()) + ".bif"), false);
                    flatLTMwriter_2D.write(flatLTM_2D);
                    System.out.println("----- Flat LTM for "+ data.getName() + "has been learned with 2D islands ----- \n");
                    System.out.println("The resulting BIC score is: "+ flatLTM_2D.getBICScore(data) +" \n");
                    System.out.println("The resulting AIC score is: "+ flatLTM_2D.getAICcScore(data) +" \n");
                    System.out.println("The resulting LL score is: "+ flatLTM_2D.getLoglikelihood(data) +" \n");

                    /** MULTI-LEVEL LTM 3 Domain islands */

                    // Create a multi-level LTM and learn its parameters
                    LTM multiLevelLTM_2D = LTM_Learner.learnParameters(createManualMultiLevelLTM(domain2Islands, data), data);
                    // Save it in BIF format
                    newBifWriter multiLevelLTMWriter_2D = new newBifWriter(new FileOutputStream(output_path + "Multilevel_2D_" + FilenameUtils.removeExtension(inputFile.getName()) + ".bif"), false);
                    multiLevelLTMWriter_2D.write(multiLevelLTM_2D);
                    System.out.println("----- Multi-level LTM for "+ data.getName() + "has been learned with 2D islands ----- \n");
                    System.out.println("The resulting BIC score is: "+ multiLevelLTM_2D.getBICScore(data) +" \n");
                    System.out.println("The resulting AIC score is: "+ multiLevelLTM_2D.getAICcScore(data) +" \n");
                    System.out.println("The resulting LL score is: "+ multiLevelLTM_2D.getLoglikelihood(data) +" \n");

                    /** FLAT LTM 3 Domain islands */

                    // Create a flat LTM and learn its parameters
                    LTM flatLTM_3D = LTM_Learner.learnParameters(createDomainsFlatLTM(domain3Islands, data), data);
                    // Save it in BIF format
                    newBifWriter flatLTMwriter_3D = new newBifWriter(new FileOutputStream(output_path + "Flat_3D_" + FilenameUtils.removeExtension(inputFile.getName()) + ".bif"), false);
                    flatLTMwriter_3D.write(flatLTM_3D);
                    System.out.println("----- Flat LTM for "+ data.getName() + "has been learned with 3D islands----- \n");
                    System.out.println("The resulting BIC score is: "+ flatLTM_3D.getBICScore(data) +" \n");
                    System.out.println("The resulting AIC score is: "+ flatLTM_3D.getAICcScore(data) +" \n");
                    System.out.println("The resulting LL score is: "+ flatLTM_3D.getLoglikelihood(data) +" \n");

                    /** MULTI-LEVEL LTM 3 Domain islands */

                    // Create a multi-level LTM and learn its parameters
                    LTM multiLevelLTM_3D = LTM_Learner.learnParameters(createManualMultiLevelLTM(domain3Islands, data), data);
                    // Save it in BIF format
                    newBifWriter multiLevelLTMWriter_3D = new newBifWriter(new FileOutputStream(output_path + "Multilevel_3D_" + FilenameUtils.removeExtension(inputFile.getName()) + ".bif"), false);
                    multiLevelLTMWriter_3D.write(multiLevelLTM_3D);
                    System.out.println("----- Multi-level LTM for "+ data.getName() + "has been learned with 3D islands----- \n");
                    System.out.println("The resulting BIC score is: "+ multiLevelLTM_3D.getBICScore(data) +" \n");
                    System.out.println("The resulting AIC score is: "+ multiLevelLTM_3D.getAICcScore(data) +" \n");
                    System.out.println("The resulting LL score is: "+ multiLevelLTM_3D.getLoglikelihood(data) +" \n");

                    /** SHARED LTM */
                    // Create a shared LTM and learn its parameters
                    BayesNet sharedLTM = BayesNet_Learner.learnParameters(createMySharedLTM(data), data);
                    // Save it in BIF format
                    newBifWriter sharedLTMWriter = new newBifWriter(new FileOutputStream(output_path + "Shared_" + FilenameUtils.removeExtension(inputFile.getName()) + ".bif"), false);
                    sharedLTMWriter.write(sharedLTM);
                    System.out.println("----- Shared LTM for "+ data.getName() + "has been learned ----- \n");
                    System.out.println("The resulting BIC score is: "+ sharedLTM.getBICScore(data) +" \n");
                    System.out.println("The resulting AIC score is: "+ sharedLTM.getAICcScore(data) +" \n");
                    System.out.println("The resulting LL score is: "+ sharedLTM.getLoglikelihood(data) +" \n");
                }
            }catch(Exception e){
                System.out.println("Error with " + inputFile.getName());
                e.printStackTrace();
            }
        }
    }

    /**
     * A diferencia del NMS, no existen unos dominios bien definidos. Por lo que vamos a utilizar 2 tipos de separaciones
     * - La separación propuesta por el articulo (3 dominios principales)
     * - La separación propuesta por mi ()
     *
     * Otra posibilidad alternativa es la de combinar variables y luego además agruparlas en un menor número de vistas
     */




    private static ArrayList<LTM> create2DomainIslands(DiscreteDataSet dataSet){
        /** first we create the domains islands */
        ArrayList<LTM> domainIslands = new ArrayList<>();

        // Motor evaluation & Motor complications
        ArrayList<DiscreteVariable> motorVariables = new ArrayList<>();
        motorVariables.add(dataSet.getVariable("scm1rue"));
        motorVariables.add(dataSet.getVariable("scm1lue"));
        motorVariables.add(dataSet.getVariable("scm2rue"));
        motorVariables.add(dataSet.getVariable("scm2lue"));
        motorVariables.add(dataSet.getVariable("scm3rue"));
        motorVariables.add(dataSet.getVariable("scm3lue"));
        motorVariables.add(dataSet.getVariable("scm4rue"));
        motorVariables.add(dataSet.getVariable("scm4lue"));
        motorVariables.add(dataSet.getVariable("scm5rise"));
        motorVariables.add(dataSet.getVariable("scm6post"));
        motorVariables.add(dataSet.getVariable("scm7gait"));
        motorVariables.add(dataSet.getVariable("scm9free"));
        motorVariables.add(dataSet.getVariable("scm18dpr"));
        motorVariables.add(dataSet.getVariable("scm19dsv"));
        motorVariables.add(dataSet.getVariable("scm20fpr"));
        motorVariables.add(dataSet.getVariable("scm21fsv"));
        LTM motorIsland = LTM.createLCM(motorVariables, 2); // Minimum cardinality
        domainIslands.add(motorIsland);

        // Daily Living
        ArrayList<DiscreteVariable> dailyLivingVariables = new ArrayList<>();
        dailyLivingVariables.add(dataSet.getVariable("scm8spee"));
        dailyLivingVariables.add(dataSet.getVariable("scm10swa"));
        dailyLivingVariables.add(dataSet.getVariable("scm11spe"));
        dailyLivingVariables.add(dataSet.getVariable("scm12fee"));
        dailyLivingVariables.add(dataSet.getVariable("scm13dre"));
        dailyLivingVariables.add(dataSet.getVariable("scm14hyg"));
        dailyLivingVariables.add(dataSet.getVariable("scm15cha"));
        dailyLivingVariables.add(dataSet.getVariable("scm16wal"));
        dailyLivingVariables.add(dataSet.getVariable("scm17han"));
        LTM d1Island = LTM.createLCM(dailyLivingVariables, 2); // Minimum cardinality
        domainIslands.add(d1Island);

        return domainIslands;
    }

    private static ArrayList<LTM> create3DomainIslands(DiscreteDataSet dataSet){
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

    private static LTM createDomainsFlatLTM(ArrayList<LTM> domainIslands, DiscreteDataSet dataSet){
        LTM flatLTM = FlatLTM_creator.applyChowLiuWithBestRoot(domainIslands, dataSet);
        // Recordar que al ser manual no hay refinamiento del modelo (cambio de nodos entre particiones)
        return LTM_CardinalitySearch.globalBestCardinalityIncrease(flatLTM, dataSet, 6);
    }

    private static LTM createManualMultiLevelLTM(ArrayList<LTM> domainIslands, DiscreteDataSet dataSet){
        LTM multiLevelLTM = new LTM();
        DiscreteVariable level1Root = new DiscreteVariable(2); // start with cardinality = 2
        BeliefNode level1RootBeliefNode = multiLevelLTM.addNode(level1Root);

        for(LTM island: domainIslands) {
            multiLevelLTM = multiLevelLTM.addDisconnectedLTM(island);
            String islandRootName = island.getRoot().getName();
            multiLevelLTM.addEdge(multiLevelLTM.getNode(islandRootName), multiLevelLTM.getNode(level1RootBeliefNode.getName()));
        }

        // Una vez ya hemos generado el LTM multi-level, aplicamos una búsqueda local de la cardinalidad
        // que va de abajo a arriba, es decir, busca primero la mejor cardinalidad de cada isla por separado
        // y luego busca la mejor cardinalidad del primer nivel
        // TODO: En este caso como no hemos resuelto aun el problema de completar los datos tipo EM, el primer nivel utiliza EM global
        return LTM_CardinalitySearch.globalBestCardinalityIncrease(multiLevelLTM, dataSet, 6);
    }

    // Crea un 'LTM' donde las LVs tienen variables manifiestas compartidas
    private static BayesNet createMySharedLTM(DiscreteDataSet dataSet){

        // 1. Inicializamos las red bayesiana (un shared-LTM no es un LTM clasico)
        BayesNet sharedLTM = new BayesNet();

        // 2. Añadimos los 25 nodos (MVs)
        BeliefNode scm1rue = sharedLTM.addNode(dataSet.getVariable("scm1rue"));
        BeliefNode scm1lue = sharedLTM.addNode(dataSet.getVariable("scm1lue"));
        BeliefNode scm2rue = sharedLTM.addNode(dataSet.getVariable("scm2rue"));
        BeliefNode scm2lue = sharedLTM.addNode(dataSet.getVariable("scm2lue"));
        BeliefNode scm3rue = sharedLTM.addNode(dataSet.getVariable("scm3rue"));
        BeliefNode scm3lue = sharedLTM.addNode(dataSet.getVariable("scm3lue"));
        BeliefNode scm4rue = sharedLTM.addNode(dataSet.getVariable("scm4rue"));
        BeliefNode scm4lue = sharedLTM.addNode(dataSet.getVariable("scm4lue"));
        BeliefNode scm5rise = sharedLTM.addNode(dataSet.getVariable("scm5rise"));
        BeliefNode scm6post = sharedLTM.addNode(dataSet.getVariable("scm6post"));
        BeliefNode scm7gait = sharedLTM.addNode(dataSet.getVariable("scm7gait"));
        BeliefNode scm8spee = sharedLTM.addNode(dataSet.getVariable("scm8spee"));
        BeliefNode scm9free = sharedLTM.addNode(dataSet.getVariable("scm9free"));
        BeliefNode scm10swa = sharedLTM.addNode(dataSet.getVariable("scm10swa"));
        BeliefNode scm11spe = sharedLTM.addNode(dataSet.getVariable("scm11spe"));
        BeliefNode scm12fee = sharedLTM.addNode(dataSet.getVariable("scm12fee"));
        BeliefNode scm13dre = sharedLTM.addNode(dataSet.getVariable("scm13dre"));
        BeliefNode scm14hyg = sharedLTM.addNode(dataSet.getVariable("scm14hyg"));
        BeliefNode scm15cha = sharedLTM.addNode(dataSet.getVariable("scm15cha"));
        BeliefNode scm16wal = sharedLTM.addNode(dataSet.getVariable("scm16wal"));
        BeliefNode scm17han = sharedLTM.addNode(dataSet.getVariable("scm17han"));
        BeliefNode scm18dpr = sharedLTM.addNode(dataSet.getVariable("scm18dpr"));
        BeliefNode scm19dsv = sharedLTM.addNode(dataSet.getVariable("scm19dsv"));
        BeliefNode scm20fpr = sharedLTM.addNode(dataSet.getVariable("scm20fpr"));
        BeliefNode scm21fsv = sharedLTM.addNode(dataSet.getVariable("scm21fsv"));

        // 3. Añadimos las 3 variables latentes (LVs) con cardinalidad = 2
        BeliefNode armLV = sharedLTM.addNode(new DiscreteVariable(2));
        BeliefNode mouthLV = sharedLTM.addNode(new DiscreteVariable(2));
        BeliefNode legLV = sharedLTM.addNode(new DiscreteVariable(2));
        // 3.1 - Las añadimos a la coleccion de variables latentes
        ArrayList<DiscreteVariable> latentNodes = new ArrayList<>();
        latentNodes.add(armLV.getVariable());
        latentNodes.add(mouthLV.getVariable());
        latentNodes.add(legLV.getVariable());

        // 4. Creamos los edges que van desde las LVs a las MVs

        // 4.1 - Arm edges
        sharedLTM.addEdge(scm1rue, armLV);
        sharedLTM.addEdge(scm1lue, armLV);
        sharedLTM.addEdge(scm2rue, armLV);
        sharedLTM.addEdge(scm2lue, armLV);
        sharedLTM.addEdge(scm3rue, armLV);
        sharedLTM.addEdge(scm3lue, armLV);
        sharedLTM.addEdge(scm4rue, armLV);
        sharedLTM.addEdge(scm4lue, armLV);
        sharedLTM.addEdge(scm12fee, armLV);
        sharedLTM.addEdge(scm13dre, armLV);
        sharedLTM.addEdge(scm14hyg, armLV);
        sharedLTM.addEdge(scm15cha, armLV);
        sharedLTM.addEdge(scm17han, armLV);
        sharedLTM.addEdge(scm18dpr, armLV);
        sharedLTM.addEdge(scm19dsv, armLV);
        sharedLTM.addEdge(scm20fpr, armLV);
        sharedLTM.addEdge(scm21fsv, armLV);

        // 4.2 - Mouth edges
        sharedLTM.addEdge(scm8spee, mouthLV);
        sharedLTM.addEdge(scm9free, mouthLV);
        sharedLTM.addEdge(scm10swa, mouthLV);
        sharedLTM.addEdge(scm11spe, mouthLV);
        sharedLTM.addEdge(scm12fee, mouthLV);
        sharedLTM.addEdge(scm14hyg, mouthLV);
        sharedLTM.addEdge(scm20fpr, mouthLV);
        sharedLTM.addEdge(scm21fsv, mouthLV);

        // 4.3 - Leg edges
        sharedLTM.addEdge(scm5rise, legLV);
        sharedLTM.addEdge(scm6post, legLV);
        sharedLTM.addEdge(scm7gait, legLV);
        sharedLTM.addEdge(scm9free, legLV);
        sharedLTM.addEdge(scm12fee, legLV);
        sharedLTM.addEdge(scm13dre, legLV);
        sharedLTM.addEdge(scm14hyg, legLV);
        sharedLTM.addEdge(scm15cha, legLV);
        sharedLTM.addEdge(scm16wal, legLV);
        sharedLTM.addEdge(scm18dpr, legLV);
        sharedLTM.addEdge(scm19dsv, legLV);
        sharedLTM.addEdge(scm20fpr, legLV);
        sharedLTM.addEdge(scm21fsv, legLV);

        // 5. Parametrización aleatoria y aprendizaje de parametros
        sharedLTM.randomlyParameterize();
        sharedLTM = BayesNet_Learner.learnParameters(sharedLTM, dataSet);
        return BayesNet_CardinalitySearch.globalBestCardinalityIncrease(sharedLTM, latentNodes, dataSet, 6);
    }


}
