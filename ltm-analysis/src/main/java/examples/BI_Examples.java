package examples;

import org.latlab.clustering.BridgedIslands;
import org.latlab.clustering.IslandFinder;
import org.latlab.model.LTM;
import ferjorosa.data.DataSet;
import ferjorosa.data.DataSetLoader;

import java.util.Collection;

/**
 * Created by equipo on 13/01/2017.
 */
public class BI_Examples {

    public static void main(String[] args){
        //islandFinderExample();
        BIExample();
    }

    private static void islandFinderExample(){
        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/Asia_train.arff"));

            IslandFinder islandFinder = new IslandFinder();
            Collection<LTM> ltms = islandFinder.find(data);
            for(LTM ltm: ltms)
                System.out.println(ltm.toString(1));

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void BIExample(){
        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/Asia_train.arff"));

            BridgedIslands bi = new BridgedIslands();
            LTM ltm = bi.learnLTM(data);
            System.out.println(ltm.toString(1));
            System.out.println("BIC: "+ ltm.getBICScore(data));
            System.out.println("AIC: "+ ltm.getLoglikelihood(data));
            System.out.println("LL: "+ ltm.getAICScore(data));

        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
