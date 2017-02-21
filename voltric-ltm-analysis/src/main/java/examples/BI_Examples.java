package examples;

import voltric.clustering.BridgedIslands;
import voltric.clustering.IslandFinder;
import voltric.data.dataset.DiscreteDataSet;
import voltric.io.data.DataFileLoader;
import voltric.model.LTM;
import voltric.variables.DiscreteVariable;

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
            DiscreteDataSet data = new DiscreteDataSet(DataFileLoader.loadData("data/Asia_train.arff", DiscreteVariable.class));

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
            DiscreteDataSet data = new DiscreteDataSet(DataFileLoader.loadData("data/Asia_train.arff", DiscreteVariable.class));

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
