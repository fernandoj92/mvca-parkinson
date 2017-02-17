package examples;

import ferjorosa.ltm.MWST;
import voltric.clustering.IslandFinder;
import voltric.data.dataset.DiscreteDataSet;
import voltric.graph.AbstractNode;
import voltric.graph.UndirectedGraph;
import voltric.io.data.DataFileLoader;
import voltric.model.LTM;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by equipo on 13/02/2017.
 */
public class MWST_Example {

    public static void main(String[] args){
        try {
            DiscreteDataSet data = new DiscreteDataSet(DataFileLoader.loadData("data/Asia_train.arff"));

            IslandFinder islandFinder = new IslandFinder();
            Collection<LTM> ltms = islandFinder.find(data);

            MWST mwstCreator = new MWST(ltms, data);
            UndirectedGraph mwst = mwstCreator.learnMWST();

            // Choose the first node of the mwst as root
            Queue<AbstractNode> frontier = new LinkedList<AbstractNode>();
            frontier.offer(mwst.getNodes().peek());

            // Tener el MWST no es suficiente, es tmb necesario construir el nuevo LTM y luego utilizando el MWST unir las diferentes islas
            // seleccionando la raiz que mas nos convenga
            int i = 0;

        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
