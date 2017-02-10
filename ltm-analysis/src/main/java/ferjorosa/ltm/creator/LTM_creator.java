package ferjorosa.ltm.creator;

import ferjorosa.ltm.MWST;
import org.latlab.graph.UndirectedGraph;
import org.latlab.model.LTM;
import org.latlab.util.DataSet;

import java.util.List;

/**
 * This class creates LTMs from Independent partitions using different strategies to connect them
 */
public class LTM_creator {

    /**
     * Returns a LTM with a hierarchy in the LV subgraph which has been create using the Chow-Liu algorithm
     * and whose root has been chosen with respect to the model's score.
     *
     * @param islands
     * @param dataSet
     * @return
     */
    public static LTM applyChowLiuWithBestRoot(List<LTM> islands, DataSet dataSet){
        MWST mwst = new MWST(islands, dataSet);
        UndirectedGraph clTree = mwst.learnMWST();

        clTree.
    }

    /**
     * Returns a LTM with a hierarchy in the LV subgraph that has been created using the Chow-Liu algorithm
     * and whose root has been chosen with respect to the model's score.
     *
     * @param islands
     * @param dataSet
     * @return
     */
    public static LTM applyChowLiuWithRandomRoot(List<LTM> islands, DataSet dataSet){

    }
}
