package voltric.graph;

/**
 *
 */
public class DirectedTree extends DirectedAcyclicGraph {

    private DirectedNode _root;

    public DirectedTree(DirectedNode root){
        this._root = root;
    }


    public DirectedTree(DirectedAcyclicGraph dag, DirectedNode root){

        // Small fix-around to check the conditions before calling the Super() constructor
        // This could be easily avoided in Scala with 'require()'
        super(checkDagIsTree(dag, root));

        // Directed tree's root
        this._root = root;
    }

    public DirectedTree(UndirectedGraph undirectedTree, UndirectedNode root){
        if(!TreeChecker.isTree(undirectedTree))
            throw new IllegalArgumentException("The provided undirected graph should be a tree");

        if(!TreeChecker.isRoot(undirectedTree, root))
            throw new IllegalArgumentException("The provided node is not the root of the tree");

        createEdges(undirectedTree, root);
    }

    public DirectedNode getRoot(){
        return _root;
    }

    public void setRoot(DirectedNode root){
        this._root = root;
    }

    private void createEdges(UndirectedGraph undirectedTree, AbstractNode root){
        Boolean visited[] = new Boolean[undirectedTree.getNumberOfNodes()];
        for (int i = 0; i < visited.length; i++)
            visited[i] = false;

        addEdgesRecursively(undirectedTree, visited, root);
    }

    private void addEdgesRecursively(UndirectedGraph undirectedTree, Boolean[] visited, AbstractNode root){
        // Marks current node as visited
        visited[undirectedTree.getNodes().indexOf(root)] = true;

        // Añade un edge desde el nodo index a todos los nodos vecinos
        for(AbstractNode node: root.getNeighbors()){
            if(!visited[undirectedTree.getNodes().indexOf(node)]){
                addEdge(node, root);
                addEdgesRecursively(undirectedTree, visited, node);
            }
        }
    }

    private static DirectedAcyclicGraph checkDagIsTree(DirectedAcyclicGraph dag, DirectedNode root){
        if(!TreeChecker.isTree(dag))
            throw new IllegalArgumentException("The provided DAG should be a tree");

        if(!TreeChecker.isRoot(dag, root))
            throw new IllegalArgumentException("The provided node is not the root of the tree");

        return dag;
    }
}
