package parkinson;

import parkinson.automatic.BI_Learn;
import parkinson.automatic.LCM_Learn;

/**
 * Created by equipo on 09/02/2017.
 */
public class ParkinsonLearn {

    public static void main(String[] args){

        //BI_Learn.learnAndSaveAllModels();
        LCM_Learn.learnAndSaveAllModels();
    }

    private static void learnAndSaveAllAutomaticModels(){
        BI_Learn.learnAndSaveAllModelsOld();
        LCM_Learn.learnAndSaveAllModels();
    }

    private static void learnAndSaveAllManualModels(){

    }
}
