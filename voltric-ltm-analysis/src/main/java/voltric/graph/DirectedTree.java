package voltric.graph;

/**
 *
 */
public class DirectedTree extends DirectedAcyclicGraph {

    private DirectedNode _root;

    public DirectedTree(DirectedNode root){
        this.addNode(root.getName());
        this._root = root;
    }


    public DirectedTree(DirectedAcyclicGraph dag, DirectedNode root){

        // Small fix-around to check the conditions before calling the Super() constructor
        // This could be easily avoided in Scala with 'require()'
        super(checkDagIsTree(dag, root));

        // Directed tree's root
        this.addNode(root.getName());
        this._root = root;
    }

    public DirectedTree(UndirectedGraph undirectedTree, UndirectedNode root){

        if(!UndirectedTreeChecker.isTree(undirectedTree))
            throw new IllegalArgumentException("The provided undirected graph should be a tree");

        // create the directed tree's nodes
        createNodes(undirectedTree);
        // create the directed tree's edges
        createEdges(undirectedTree, root);
    }

    public DirectedNode getRoot(){
        return _root;
    }

    public void setRoot(DirectedNode root){
        this._root = root;
    }

    private void createNodes(UndirectedGraph undirectedTree){
        for(AbstractNode undirectedNode: undirectedTree.getNodes())
            this.addNode(undirectedNode.getName());
    }

    private void createEdges(UndirectedGraph undirectedTree, UndirectedNode root){
        Boolean visited[] = new Boolean[undirectedTree.getNumberOfNodes()];
        for (int i = 0; i < visited.length; i++)
            visited[i] = false;

        // Add the edges
        addEdgesRecursively(undirectedTree, visited, root);
    }

    // Basicamente iteramos por el grafo no dirigido y añadimos arcos entre los nodos del nuevo arbol dirigido
    private void addEdgesRecursively(UndirectedGraph undirectedTree, Boolean[] visited, UndirectedNode undirectedRoot){
        // Marks current node as visited
        visited[undirectedTree.getNodes().indexOf(undirectedRoot)] = true;

        // Directed equivalent root
        DirectedNode directedRoot = (DirectedNode) this.getNode(undirectedRoot.getName());

        // Añade un edge desde el nodo index a todos los nodos vecinos
        for(AbstractNode undirectedNode: undirectedRoot.getNeighbors()){
            // Undirected node's index
            int undirectedNodeIndex = undirectedTree.getNodes().indexOf(undirectedNode);

            // directed equivalent node
            DirectedNode directedNode = (DirectedNode) this.getNode(undirectedNode.getName());

            if(!visited[undirectedNodeIndex]){
                this.addEdge(directedNode, directedRoot);
                addEdgesRecursively(undirectedTree, visited, (UndirectedNode) undirectedNode);
            }
        }
    }

    private static DirectedAcyclicGraph checkDagIsTree(DirectedAcyclicGraph dag, DirectedNode root){
        if(!DirectedTreeChecker.isTree(dag))
            throw new IllegalArgumentException("The provided DAG should be a tree");

        if(!DirectedTreeChecker.isRoot(dag, root))
            throw new IllegalArgumentException("The provided node is not the root of the tree");

        return dag;
    }
}
