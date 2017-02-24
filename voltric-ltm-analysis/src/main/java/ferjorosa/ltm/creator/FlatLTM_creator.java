package ferjorosa.ltm.creator;

import ferjorosa.ltm.MWST;
import ferjorosa.ltm.learning.parameters.LTM_Learner;
import voltric.data.dataset.DiscreteDataSet;
import voltric.graph.*;
import voltric.model.BeliefNode;
import voltric.model.LTM;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * This class creates LTMs from Independent partitions using different strategies to connect them
 */
public class FlatLTM_creator {

    /**
     * Constructs a LTM from a collection of LTMs and the Directed tree corresponding to its the tree formed by the islands
     * roots.
     *
     * @param islands the collection of islands, representing the different partitions of the LTM.
     * @param latentVarTree the tree that connects the islands.
     * @param dataSet the dataset used in the learning process.
     * @return a new LTM that connects the set of islands with fully learned parameters.
     */
    // TODO: Add a parameter that represents the existence of LCM_EM (it uses previously-learned parameters as input)
    public static LTM createFromIslands(Collection<LTM> islands, DirectedTree latentVarTree, DiscreteDataSet dataSet){

        // check if the provided tree corresponds with the roots of the islands (same size, same objects)
        if(islands.size() != latentVarTree.getNumberOfNodes())
            throw new IllegalArgumentException("The latent var tree doesn't correspond with the provided islands");

        // The names of the nodes will be used as the identifier to match with the islands' roots.
        List<String> latentVarTreeNames = latentVarTree.getNodes().stream().map(x-> x.getName()).collect(Collectors.toList());

        // Check if the nodes coincide
        for(LTM island: islands){
            if(!latentVarTreeNames.contains(island.getRoot().getName()))
                throw new IllegalArgumentException("The latent var tree doesn't correspond with the provided islands");
        }

        LTM latentTree = new LTM();

        // Construct tree: first, add all manifest nodes and latent nodes.
        // Second, copy the edges and CPTs in each LCMs.
        for (LTM island : islands) {

            for (AbstractNode node : island.getNodes()) {
                latentTree.addNode(((BeliefNode) node).getVariable());
            }

            // copy the edges and CPTs
            for (AbstractNode node : island.getNodes()) {
                BeliefNode bNode = (BeliefNode) node;

                if (!bNode.isRoot()) {
                    BeliefNode parent = (BeliefNode) bNode.getParent();

                    BeliefNode newNode = latentTree.getNode(bNode.getVariable());
                    BeliefNode newParent = latentTree.getNode(parent.getVariable());

                    latentTree.addEdge(newNode, newParent);
                    newNode.setCpt(bNode.getCpt().clone()); // copy the parameters of manifest variables
                } else {
                    latentTree.getNodeByName(node.getName()).setCpt(bNode.getCpt().clone());
                }
            }
        }

        // Once the islands have been copied into the new LCM, we have to connect them using the provided
        // directed tree where each node that represents the root of each island.

        // First we iterate through the edges of the tree and we add them to the new tree using the node name as identifier
        for(Edge edge: latentVarTree.getEdges()){
            // The corresponding nodes
            BeliefNode newEdgeHead = latentTree.getNodeByName(edge.getHead().getName());
            BeliefNode newEdgeTail = latentTree.getNodeByName(edge.getTail().getName());
            // The latentTree's new edge is created
            latentTree.addEdge(newEdgeHead, newEdgeTail);
        }

        // Finally the parameters of the new tree are fully learned
        return LTM_Learner.learnParameters(latentTree, dataSet);
    }

    /**
     * Returns a LTM with a hierarchy in the LV subgraph which has been create using the Chow-Liu algorithm
     * and whose root has been chosen with respect to the model's score (BIC score).
     *
     * @param islands the set of partitions of the new flat LTM.
     * @param dataSet the dataSet that is going to be used in he learning process.
     * @return
     */
    public static LTM applyChowLiuWithBestRoot(Collection<LTM> islands, DiscreteDataSet dataSet){
        MWST mwst = new MWST(islands, dataSet);
        UndirectedGraph chowLiuTree = mwst.learnMWST();

        LTM bestLTM = null;

        // All the possible roots for the new LTM are evaluated (N-1 nodes => N-1 executions)
        for(AbstractNode node : chowLiuTree.getNodes()){
            DirectedTree dTree = new DirectedTree(chowLiuTree, (UndirectedNode) node);
            LTM tempLTM = createFromIslands(islands, dTree, dataSet); // The parameters of the returned LTM have been properly learned to have BIC score
            if(bestLTM == null)
                bestLTM = tempLTM;
            else if(bestLTM.getBICScore(dataSet) > tempLTM.getBICScore(dataSet))
                bestLTM = tempLTM;
        }

        return bestLTM;
    }

    /**
     * Returns a LTM with a hierarchy in the LV subgraph that has been created using the Chow-Liu algorithm
     * and whose root has been chosen with respect to the model's score (BIC score).
     *
     * @param islands the set of partitions of the new flat LTM
     * @param dataSet the dataSet that is going to be used in he learning process.
     * @return the new LTM whose root has been randomly selected from the Chow-Liu tree fromed with the collection of
     * islands' roots.
     */
    public static LTM applyChowLiuWithRandomRoot(List<LTM> islands, DiscreteDataSet dataSet){

        MWST mwst = new MWST(islands, dataSet);
        UndirectedGraph chowLiuTree = mwst.learnMWST();

        Random random = new Random();
        int islandIndex = random.nextInt(islands.size() - 1);

        UndirectedNode randomlySelectedNode = (UndirectedNode) chowLiuTree.getNodes().get(islandIndex);
        // The randomly selected node will act as the root of the tree
        DirectedTree dTree = new DirectedTree(chowLiuTree, randomlySelectedNode);

        return createFromIslands(islands, dTree, dataSet);
    }
}
