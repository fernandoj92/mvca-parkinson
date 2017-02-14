package examples;

import ferjorosa.graph.AbstractNode;
import ferjorosa.ltm.MWST;
import org.latlab.clustering.IslandFinder;
import ferjorosa.graph.UndirectedGraph;
import org.latlab.model.LTM;
import ferjorosa.data.DataSet;
import ferjorosa.data.DataSetLoader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by equipo on 13/02/2017.
 */
public class MWST_Example {

    public static void main(String[] args){
        try {
            DataSet data = new DataSet(DataSetLoader.convert("data/Asia_train.arff"));

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
