package voltric.clustering;


import voltric.data.dataset.DiscreteDataCase;
import voltric.data.dataset.DiscreteDataSet;
import voltric.graph.AbstractNode;
import voltric.graph.DirectedNode;
import voltric.graph.Edge;
import voltric.graph.UndirectedGraph;
import voltric.learner.ParallelEmLearner;
import voltric.model.BeliefNode;
import voltric.model.LTM;
import voltric.reasoner.CliqueTreePropagation;
import voltric.util.Function;
import voltric.util.ScoreCalculator;
import voltric.util.StringPair;
import voltric.util.Utils;
import voltric.variables.DiscreteVariable;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Created by equipo on 13/01/2017.
 */
public class BridgedIslands {

    /**
     * The collection of hierarchies. Each hierarchy represents a LCM and is
     * indexed by the variable at its root.
     */
    private Map<DiscreteVariable, LTM> _hierarchies;

    /**
     * The ArrayList of manifest variables with orders.
     */
    protected ArrayList<DiscreteVariable> _Variables = new ArrayList<DiscreteVariable>();

    /**
     * The collection of manifest variables that wait to do UD-test.
     */
    protected Set<DiscreteVariable> _VariablesSet = new HashSet<DiscreteVariable>();

    /**
     * The collection of posterior distributions P(Y|d) for each latent variable
     * Y at the root of a hierarchy and each data case d in the training data.
     * USELESS in PEM version But keep it for future use if we need soft
     * assignment or to keep record
     */
    private Map<DiscreteVariable, Map<DiscreteDataCase, Function>> _latentPosts;

    /**
     * The collection of pairwise mutual information.
     */
    private Map<DiscreteVariable, Map<DiscreteVariable, Double>> _mis;

    /**
     * Save bestPair of observed variables for every latent variable(LCM)
     */
    Map<String, ArrayList<DiscreteVariable>> _bestpairs = new HashMap<String, ArrayList<DiscreteVariable>>();

    /**
     * Threshold for EM.
     */
    protected final double _emThreshold = 0.01;

    /**
     * Parameter for EM.
     */
    protected final int _EmMaxSteps = 80; // 50

    /**
     * Parameter for EM.
     */
    protected final int _EmNumRestarts = 10; // 5

    /**
     * Maximum number of island size
     */
    protected final int _maxIsland = 10;

    /**
     * Threshold for UD-test.
     */
    protected final double _UDthreshold = 3;

    protected void initialize(DiscreteDataSet data) {
        System.out.println("=== Initialization ===");

        // initialize data structures for P(Y|d).
        _latentPosts = new HashMap<DiscreteVariable, Map<DiscreteDataCase, Function>>();

        // initialize hierarchies
        // _hirearchies will be used to keep all LCMs found by U-test.
        _hierarchies = new HashMap<DiscreteVariable, LTM>();

        _VariablesSet = new HashSet<DiscreteVariable>();

        _mis = new HashMap<DiscreteVariable, Map<DiscreteVariable, Double>>();

        // add all manifest variable to variable set _VariableSet.
        for (DiscreteVariable var : data.getVariables()) {
            _VariablesSet.add(var);
        }
    }

    public Collection<LTM> find(DiscreteDataSet _data)
            throws FileNotFoundException, UnsupportedEncodingException {

        this.findIslands(_data);

        return _hierarchies.values();
    }

    public LTM learnLTM(DiscreteDataSet _data) throws FileNotFoundException, UnsupportedEncodingException {

        // Find the islands
        this.findIslands(_data);

        // Build the tree
        return this.BuildLatentTree(_data);
    }

    /** Code for the "find" and "learnLTM" method */
    private void findIslands(DiscreteDataSet _data){
        int i = 1;
        initialize(_data);
        // Call lcmLearner iteratively and learn the LCMs.
        while (!isDone()) {
            System.out.println("======================= Learn Island : " + i
                    + " , number of variables left: " + _VariablesSet.size()
                    + "  =================================");
            if (_VariablesSet.size() == 3) {
                if (_mis.isEmpty()) {
                    ArrayList<DiscreteVariable> bestPair = new ArrayList<DiscreteVariable>();
                    // compute MI and find the pair with the largest MI
                    // value
                    long startMI = System.currentTimeMillis();
                    _mis = computeMis(bestPair, _data);
                    System.out.println(
                            "======================= _mis has been calculated  =================================");
                    System.out.println("--- ComputingMI Time: "
                            + (System.currentTimeMillis() - startMI) + " ms ---");

                }
                ArrayList<DiscreteVariable> bestP = new ArrayList<DiscreteVariable>();
                findBestPair(bestP, _VariablesSet);
                // System.out.println("Best Pair " + bestP.get(0).getName()
                // +" and " + bestP.get(1).getName());
                ArrayList<DiscreteVariable> Varstemp = new ArrayList<DiscreteVariable>(_VariablesSet);
                DiscreteDataSet data_proj = _data.project(Varstemp);
                LTM subModel = LCM3N(Varstemp, data_proj);
                updateHierarchies(subModel, bestP);
                updateVariablesSet(subModel);
                break;
            }

            ArrayList<DiscreteVariable> bestPair = new ArrayList<DiscreteVariable>();
            // _mis only needs to compute once

            if (_mis.isEmpty()) {
                // compute MI and find the pair with the largest MI value
                long startMI = System.currentTimeMillis();
                _mis = computeMis(bestPair, _data);
                System.out.println(
                        "======================= _mis has been calculated  =================================");
                System.out.println("--- ComputingMI Time: "
                        + (System.currentTimeMillis() - startMI) + " ms ---");
                // System.out.println("Best Pair " +
                // bestPair.get(0).getName() +" and " +
                // bestPair.get(1).getName());

            } else {
                findBestPair(bestPair, _VariablesSet);
                // System.out.println("Best Pair " +
                // bestPair.get(0).getName() +" and " +
                // bestPair.get(1).getName());
            }

            Set<DiscreteVariable> cluster = new HashSet<DiscreteVariable>(bestPair);
            // try to find the closest variable to make the cluster have 3
            // variables now
            ArrayList<DiscreteVariable> ClosestVariablePair = findShortestOutLink(_mis, null,
                    cluster, _VariablesSet);
            ArrayList<DiscreteVariable> cluster_3n = new ArrayList<DiscreteVariable>(bestPair);

            // cluster_3n is an array containing 3 variables : bestpair and
            // ClosestVariablePair.get(1)
            LTM subModel = null;
            if (!ClosestVariablePair.isEmpty()) {
                cluster_3n.add(ClosestVariablePair.get(1));
                cluster.add(ClosestVariablePair.get(1));
            }
            // m0
            LTM m0 = LCM3N(cluster_3n, _data.project(cluster_3n));
            // cluster is the working set
            while (true) {
                ClosestVariablePair = findShortestOutLink(_mis, bestPair, cluster,
                        _VariablesSet);
                cluster.add(ClosestVariablePair.get(1));
                DiscreteDataSet data_proj2l = _data.project(new ArrayList<DiscreteVariable>(cluster));
                LTM m1 = EmLCM_learner(m0, ClosestVariablePair.get(1), bestPair,
                        data_proj2l);
                LTM minput = m1.clone();
                LTM m2 = EmLTM_2L_learner(minput, bestPair, ClosestVariablePair,
                        data_proj2l);
                m0 = m1.clone();
                double mulModelBIC = ScoreCalculator.computeBic(m2, data_proj2l);
                double uniModelBIC = ScoreCalculator.computeBic(m1, data_proj2l);

                if (mulModelBIC - uniModelBIC > _UDthreshold) {
                    if (_VariablesSet.size() - cluster.size() == 0) {
                        // split m2 to 2 LCMs subModel1 and subModel2
                        LTM subModel1 = m1.clone();
                        for (int id = 0; id < 2; id++) {
                            Edge e = subModel1.getNode(ClosestVariablePair.get(id))
                                    .getEdge(subModel1.getRoot());
                            // Should remove node first then edge.
                            subModel1.removeNode(
                                    subModel1.getNode(ClosestVariablePair.get(id)));
                            subModel1.removeEdge(e);
                        }
                        // To get subModel2
                        HashSet<String> donotUpdate = new HashSet<String>();
                        // learn an LCM with ClosestVariablePair and any
                        // other
                        // one node
                        LTM subModel2 = new LTM();
                        ArrayList<DiscreteVariable> cluster_sub2_3node = new ArrayList<DiscreteVariable>(
                                ClosestVariablePair);
                        cluster_sub2_3node.add(bestPair.get(1));
                        // subModel2 = LTM.createLCM(cluster_sub2_3node, 2);
                        subModel2 = LCM3N(cluster_sub2_3node,
                                _data.project(cluster_sub2_3node));
                        // copy parameters from m2 to submodel2
                        ArrayList<DiscreteVariable> var2s = new ArrayList<DiscreteVariable>(
                                subModel2.getNode(ClosestVariablePair.get(0)).getCpt()
                                        .getVariables());
                        subModel2.getNode(ClosestVariablePair.get(0)).getCpt()
                                .setCells(var2s, m2.getNode(ClosestVariablePair.get(0))
                                        .getCpt().getCells());
                        var2s = new ArrayList<DiscreteVariable>(
                                subModel2.getNode(ClosestVariablePair.get(1)).getCpt()
                                        .getVariables());
                        subModel2.getNode(ClosestVariablePair.get(1)).getCpt()
                                .setCells(var2s, m2.getNode(ClosestVariablePair.get(1))
                                        .getCpt().getCells());
                        donotUpdate.add(ClosestVariablePair.get(0).getName());
                        donotUpdate.add(ClosestVariablePair.get(1).getName());

                        ParallelEmLearner emLearner = new ParallelEmLearner();
                        emLearner.setLocalMaximaEscapeMethod("ChickeringHeckerman");
                        emLearner.setMaxNumberOfSteps(_EmMaxSteps);
                        emLearner.setNumberOfRestarts(_EmNumRestarts);
                        // fix starting point or not?
                        emLearner.setReuseFlag(false);
                        emLearner.setThreshold(_emThreshold);
                        emLearner.setDontUpdateNodes(donotUpdate);
                        subModel2 = (LTM) emLearner.em(subModel2,
                                data_proj2l.project(cluster_sub2_3node));

                        // remove the edge of other node
                        Edge e2 = subModel2.getNode(bestPair.get(1))
                                .getEdge(subModel2.getRoot());
                        subModel2.removeNode(subModel2.getNode(bestPair.get(1)));
                        subModel2.removeEdge(e2);

                        updateHierarchies(subModel1, bestPair);
                        updateVariablesSet(subModel1);
                        updateHierarchies(subModel2, ClosestVariablePair);
                        updateVariablesSet(subModel2);
                        break;
                    } else {
                        for (int id = 0; id < 2; id++) {
                            Edge e = m1.getNode(ClosestVariablePair.get(id))
                                    .getEdge(m1.getRoot());
                            // Should remove node first then edge.
                            m1.removeNode(m1.getNode(ClosestVariablePair.get(id)));
                            m1.removeEdge(e);
                        }
                        updateHierarchies(m1, bestPair);
                        updateVariablesSet(m1);
                        break;
                    }
                } else if (_VariablesSet.size() - cluster.size() == 0
                        || (cluster.size() >= _maxIsland
                        && (_VariablesSet.size() - cluster.size()) >= 3)) {
                    subModel = m1;
                    updateHierarchies(subModel, bestPair);
                    updateVariablesSet(subModel);
                    break;
                }
            }
            i++;
        }
    }

    private LTM BuildLatentTree(DiscreteDataSet _data) throws FileNotFoundException, UnsupportedEncodingException {
        System.out.println("===========BuildLatentTree=============");

        // long LatentPostTime = System.currentTimeMillis();
        if (_latentPosts.isEmpty()) {
            for (DiscreteVariable var : _hierarchies.keySet()) {
                LTM subModel = _hierarchies.get(var);
                updateStats(subModel, _data);
            }
        }
        // System.out.println("Compute Latent Posts Time: " +
        // (System.currentTimeMillis() - LatentPostTime) + " ms ---");

        LTM latentTree = new LTM();

        // Construct tree: first, add all manifest nodes and latent nodes.
        // Second, copy the edges and CPTs in each LCMs.
        for (DiscreteVariable var : _hierarchies.keySet()) {
            LTM tempTree = _hierarchies.get(var);

            for (AbstractNode node : tempTree.getNodes()) {
                latentTree.addNode(((BeliefNode) node).getVariable());
            }

            // copy the edges and CPTs
            for (AbstractNode node : tempTree.getNodes()) {
                BeliefNode bNode = (BeliefNode) node;

                if (!bNode.isRoot()) {
                    BeliefNode parent = (BeliefNode) bNode.getParent();

                    BeliefNode newNode = latentTree.getNode(bNode.getVariable());
                    BeliefNode newParent = latentTree.getNode(parent.getVariable());

                    latentTree.addEdge(newNode, newParent);
                    newNode.setCpt(bNode.getCpt().clone()); // copy the
                    // parameters of
                    // manifest
                    // variables
                } else {
                    latentTree.getNodeByName(node.getName()).setCpt(bNode.getCpt().clone());
                }
            }
        }

        UndirectedGraph mst = learnMaximumSpanningTree(_hierarchies, _data);

        // Choose a root with more than 3 observed variables
        Queue<AbstractNode> frontier = new LinkedList<AbstractNode>();
        frontier.offer(mst.getNodes().peek());

        // add the edges among latent nodes.
        while (!frontier.isEmpty()) {
            AbstractNode node = frontier.poll();
            DirectedNode dNode = (DirectedNode) latentTree.getNode(node.getName());

            for (AbstractNode neighbor : node.getNeighbors()) {
                DirectedNode dNeighbor = (DirectedNode) latentTree.getNode(neighbor.getName());
                if (!dNode.hasParent(dNeighbor)) {
                    latentTree.addEdge(dNeighbor, dNode);
                    frontier.offer(neighbor);
                }
            }
        }

        ArrayList<DiscreteVariable> LatVarsOrdered = latentTree.getLatVarsfromTop();
        for (DiscreteVariable v : LatVarsOrdered) {
            if (!latentTree.getNode(v).isRoot()) {
                // construct a LTM with 4 observed variables 2 latent variables
                // copy parameters
                HashSet<String> donotUpdate = new HashSet<String>();
                LTM lTM_4n = new LTM();
                BeliefNode parent = latentTree.getNode(v).getParent();

                BeliefNode h2 = lTM_4n.addNode(new DiscreteVariable(2));
                BeliefNode h1 = lTM_4n.addNode(new DiscreteVariable(2));

                for (DiscreteVariable vtemp : _bestpairs.get(parent.getName())) {
                    lTM_4n.addEdge(lTM_4n.addNode(vtemp), h1);
                    ArrayList<DiscreteVariable> var2s = new ArrayList<DiscreteVariable>(lTM_4n.getNode(vtemp).getCpt().getVariables());
                    lTM_4n.getNode(vtemp).getCpt().setCells(var2s, latentTree.getNode(vtemp).getCpt().getCells());
                    donotUpdate.add(vtemp.getName());
                }

                for (DiscreteVariable vtemp : _bestpairs.get(v.getName())) {
                    lTM_4n.addEdge(lTM_4n.addNode(vtemp), h2);
                    ArrayList<DiscreteVariable> var2s = new ArrayList<DiscreteVariable>(lTM_4n.getNode(vtemp).getCpt().getVariables());
                    lTM_4n.getNode(vtemp).getCpt().setCells(var2s, latentTree.getNode(vtemp).getCpt().getCells());
                    donotUpdate.add(vtemp.getName());
                }
                lTM_4n.addEdge(h2, h1);
                LTM temp = _hierarchies.get(parent.getVariable());
                ArrayList<DiscreteVariable> var2s = new ArrayList<DiscreteVariable>(lTM_4n.getRoot().getCpt().getVariables());
                lTM_4n.getRoot().getCpt().setCells(var2s, temp.getRoot().getCpt().getCells());
                donotUpdate.add(h1.getName());

                ArrayList<DiscreteVariable> cluster4var = new ArrayList<DiscreteVariable>(lTM_4n.getManifestVars());

                ParallelEmLearner emLearner = new ParallelEmLearner();
                emLearner.setLocalMaximaEscapeMethod("ChickeringHeckerman");
                emLearner.setMaxNumberOfSteps(_EmMaxSteps);
                emLearner.setNumberOfRestarts(_EmNumRestarts);
                emLearner.setReuseFlag(false);
                emLearner.setThreshold(_emThreshold);
                emLearner.setDontUpdateNodes(donotUpdate);

                LTM LTM4var = (LTM) emLearner.em(lTM_4n, _data.project(cluster4var));

                ArrayList<DiscreteVariable> vars = new ArrayList<DiscreteVariable>(latentTree.getNode(v).getCpt().getVariables());
                latentTree.getNode(v).getCpt().setCells(vars, LTM4var.getNode(h2.getVariable()).getCpt().getCells());
            }
        }

		/*
		 * EM for each layer
		 */

		/*
		 * ParallelEmLearner emLearner = new ParallelEmLearner();
		 * emLearner.setLocalMaximaEscapeMethod("ChickeringHeckerman");
		 * emLearner.setMaxNumberOfSteps(20); emLearner.setNumberOfRestarts(1);
		 * emLearner.setReuseFlag(true); emLearner.setThreshold(_emThreshold);
		 *
		 * latentTree = (LTM) emLearner.em(latentTree,
		 * _data.synchronize(latentTree));
		 */
        return latentTree;
    }

    /**
     * Update the collections of P(Y|d). Specifically, remove the entries for
     * all the latent variables in the given sub-model except the root, and
     * compute P(Y|d) for the latent variable at the root and each data case d.
     */
    private void updateStats(LTM subModel, DiscreteDataSet _data) {
        BeliefNode root = subModel.getRoot();
        DiscreteVariable latent = root.getVariable();
        // Function prior = root.getCpt();

        for (DirectedNode child : root.getChildren()) {
            _latentPosts.remove(((BeliefNode) child).getVariable());
        }

        Map<DiscreteDataCase, Function> latentPosts = new HashMap<DiscreteDataCase, Function>();

        CliqueTreePropagation ctp = new CliqueTreePropagation(subModel);

        Map<DiscreteVariable, Integer> varIdx = _data.createVariableToIndexMap();

        DiscreteVariable[] subVars = subModel.getManifestVars().toArray(new DiscreteVariable[0]);
        int nSubVars = subVars.length;
        int[] subStates = new int[nSubVars];

        // update for every data case
        for (DiscreteDataCase dataCase : _data.getData()) {
            // project states
            int[] states = dataCase.getStates();
            for (int i = 0; i < nSubVars; i++) {
                subStates[i] = states[varIdx.get(subVars[i])];
            }

            // set evidence and propagate
            ctp.setEvidence(subVars, subStates);
            ctp.propagate();

            // compute P(Y|d)
            Function post = ctp.computeBelief(latent);
            latentPosts.put(dataCase, post);
        }

        _latentPosts.put(latent, latentPosts);
    }

    private UndirectedGraph learnMaximumSpanningTree(Map<DiscreteVariable, LTM> hierarchies, DiscreteDataSet _data) {
        // initialize the data structure for pairwise MI
        List<StringPair> pairs = new ArrayList<StringPair>();

        // the collection of latent variables.
        List<DiscreteVariable> vars = new ArrayList<DiscreteVariable>(hierarchies.keySet());

        List<DiscreteVariable> varPair = new ArrayList<DiscreteVariable>(2);
        varPair.add(null);
        varPair.add(null);

        int nVars = vars.size();

        // enumerate all pairs of latent variables
        for (int i = 0; i < nVars; i++) {
            DiscreteVariable vi = vars.get(i);
            varPair.set(0, vi);

            for (int j = i + 1; j < nVars; j++) {
                DiscreteVariable vj = vars.get(j);
                varPair.set(1, vj);

                // compute empirical MI
                Function pairDist = computeEmpDist(varPair, _data);
                double mi = Utils.computeMutualInformation(pairDist);

                // keep both I(vi; vj) and I(vj; vi)
                pairs.add(new StringPair(vi.getName(), vj.getName(), mi));
            }
        }

        // sort the pairwise MI.
        Collections.sort(pairs);

        // building MST using Kruskal's algorithm
        UndirectedGraph mst = new UndirectedGraph();

        // nVars = latentTree.getNumberOfNodes();
        HashMap<String, ArrayList<String>> components = new HashMap<String, ArrayList<String>>();
        // move the node with more than 2 variables to the first place
        boolean flag = false;
        for (DiscreteVariable var : hierarchies.keySet()) {
            String name = var.getName();
            mst.addNode(name);

            if (hierarchies.get(var).getLeafVars().size() >= 3 && !flag) {
                mst.move2First(name);
                flag = true;
            }
            ArrayList<String> component = new ArrayList<String>(nVars);
            component.add(name);
            components.put(name, component);
        }

        // examine pairs in descending order w.r.t. MI
        for (int i = pairs.size() - 1; i >= 0; i--) {
            StringPair pair = pairs.get(i);
            String a = pair.GetStringA();
            String b = pair.GetStringB();
            ArrayList<String> aComponent = components.get(a);
            ArrayList<String> bComponent = components.get(b);

            // check whether a and b are in the same connected component
            if (aComponent != bComponent) {
                // connect nodes
                mst.addEdge(mst.getNode(a), mst.getNode(b));

                if (aComponent.size() + bComponent.size() == nVars) {
                    // early termination: the tree is done
                    break;
                }

                // merge connected component
                aComponent.addAll(bComponent);
                for (String c : bComponent) {
                    components.put(c, aComponent);
                }
            }
        }

        return mst;
    }

    /**
     * Return true if and only if the whole clustering procedure is done, or
     * equivalently, there is only one hierarchy left.
     */
    private boolean isDone() {
        return _VariablesSet.size() < 1;
    }

    /**
     * Update the collection of hierarchies.
     */
    private void updateHierarchies(LTM subModel, ArrayList<DiscreteVariable> bestPair) {
        BeliefNode root = subModel.getRoot();
        _bestpairs.put(root.getName(), bestPair);
        // add new hierarchy
        _hierarchies.put(root.getVariable(), subModel);

    }

    /**
     * Update variable set.
     *
     * @param subModel
     */
    private void updateVariablesSet(LTM subModel) {
        BeliefNode root = subModel.getRoot();

        for (DirectedNode child : root.getChildren()) {
            _VariablesSet.remove(((BeliefNode) child).getVariable());
            _Variables.remove(((BeliefNode) child).getVariable());
        }
    }

    /**
     * Learn a 3 node LCM
     *
     */
    private LTM LCM3N(ArrayList<DiscreteVariable> variables3, DiscreteDataSet data_proj) {
        LTM LCM_new = LTM.createLCM(variables3, 2);

        ParallelEmLearner emLearner = new ParallelEmLearner();
        emLearner.setLocalMaximaEscapeMethod("ChickeringHeckerman");
        emLearner.setMaxNumberOfSteps(_EmMaxSteps);
        emLearner.setNumberOfRestarts(_EmNumRestarts);
        // fix starting point or not?
        emLearner.setReuseFlag(false);
        emLearner.setThreshold(_emThreshold);

        LCM_new = (LTM) emLearner.em(LCM_new, data_proj.project(variables3));

        return LCM_new;
    }

    protected Map<DiscreteVariable, Map<DiscreteVariable, Double>> computeMis(
            ArrayList<DiscreteVariable> bestPair, DiscreteDataSet _data) {
        return computeMisByCount(bestPair, _data);
    }

    protected Map<DiscreteVariable, Map<DiscreteVariable, Double>> computeMisByCount(
            ArrayList<DiscreteVariable> bestPair, DiscreteDataSet _data) {
        List<DiscreteVariable> vars = new ArrayList<DiscreteVariable>(_VariablesSet);

        EmpiricalMiComputerForBinaryData computer = new EmpiricalMiComputerForBinaryData(
                _data, vars);
        ArrayList<double[]> miArray = computer.computerPairwise();

        return processMi(bestPair, miArray, vars);
    }

    /**
     * Find the closest variable to cluster. Note: Never move the bestpair out
     *
     * @param mis
     * @param cluster
     * @return
     */
    private ArrayList<DiscreteVariable> findShortestOutLink(
            Map<DiscreteVariable, Map<DiscreteVariable, Double>> mis, ArrayList<DiscreteVariable> bestPair,
            Set<DiscreteVariable> cluster, Set<DiscreteVariable> VariablesSet) {
        double maxMi = Double.NEGATIVE_INFINITY;
        DiscreteVariable bestInCluster = null, bestOutCluster = null;

        for (DiscreteVariable inCluster : cluster) {
            boolean a = bestPair == null;
            if (a || !bestPair.contains(inCluster)) {
                for (Map.Entry<DiscreteVariable, Double> entry : mis.get(inCluster).entrySet()) {
                    DiscreteVariable outCluster = entry.getKey();
                    double mi = entry.getValue();

                    // skip variables already in cluster
                    if (cluster.contains(outCluster)
                            || !(VariablesSet.contains(outCluster))) {
                        continue;
                    }

                    // keep the variable with max MI.
                    if (mi > maxMi) {
                        maxMi = mi;
                        bestInCluster = inCluster;
                        bestOutCluster = outCluster;
                    }
                }
            }
        }

        // Set<Variable> ClosestVariablePair = new HashSet<Variable>();
        ArrayList<DiscreteVariable> ClosestVariablePair = new ArrayList<DiscreteVariable>();
        ClosestVariablePair.add(bestInCluster);
        ClosestVariablePair.add(bestOutCluster);

        return ClosestVariablePair;
    }

    private LTM EmLCM_learner(LTM modelold, DiscreteVariable x, ArrayList<DiscreteVariable> bestPair,
                              DiscreteDataSet data_proj) {

        ArrayList<DiscreteVariable> cluster3node = new ArrayList<DiscreteVariable>(bestPair);
        cluster3node.add(x);
        // Learn a 3node LTM : bestpair and newly added node
        LTM LCM3var = LTM.createLCM(cluster3node, 2);
        LCM3var.randomlyParameterize();
        HashSet<String> donotUpdate = new HashSet<String>();

        ArrayList<DiscreteVariable> var2s = new ArrayList<DiscreteVariable>(
                LCM3var.getNode(bestPair.get(0)).getCpt().getVariables());
        LCM3var.getNode(bestPair.get(0)).getCpt().setCells(var2s,
                modelold.getNode(bestPair.get(0)).getCpt().getCells());
        donotUpdate.add(bestPair.get(0).getName());
        var2s = new ArrayList<DiscreteVariable>(
                LCM3var.getNode(bestPair.get(1)).getCpt().getVariables());
        LCM3var.getNode(bestPair.get(1)).getCpt().setCells(var2s,
                modelold.getNode(bestPair.get(1)).getCpt().getCells());
        donotUpdate.add(bestPair.get(1).getName());
        var2s = new ArrayList<DiscreteVariable>(LCM3var.getRoot().getCpt().getVariables());
        LCM3var.getRoot().getCpt().setCells(var2s,
                modelold.getRoot().getCpt().getCells());
        donotUpdate.add(LCM3var.getRoot().getName());

        ParallelEmLearner emLearner = new ParallelEmLearner();
        emLearner.setLocalMaximaEscapeMethod("ChickeringHeckerman");
        emLearner.setMaxNumberOfSteps(_EmMaxSteps);
        emLearner.setNumberOfRestarts(_EmNumRestarts);
        // fix starting point or not?
        emLearner.setReuseFlag(false);
        emLearner.setThreshold(_emThreshold);
        emLearner.setDontUpdateNodes(donotUpdate);
        LCM3var = (LTM) emLearner.em(LCM3var, data_proj.project(cluster3node));

        LTM uniModel = modelold.clone();

        uniModel.addNode(x);

        uniModel.addEdge(uniModel.getNode(x), uniModel.getRoot());
        ArrayList<DiscreteVariable> vars = new ArrayList<DiscreteVariable>(
                uniModel.getNode(x).getCpt().getVariables());
        uniModel.getNode(x).getCpt().setCells(vars,
                LCM3var.getNode(x).getCpt().getCells());

        return uniModel;
    }

    /**
     *
     * @param unimodel
     * @param bestPair
     * @param ClosestPair
     * @param data_proj
     * @return a model with two latent variables (without node relocation step)
     */

    private LTM EmLTM_2L_learner(LTM unimodel, ArrayList<DiscreteVariable> bestPair,
                                 ArrayList<DiscreteVariable> ClosestPair, DiscreteDataSet data_proj) {

        ArrayList<DiscreteVariable> cluster2BeAdded = new ArrayList<DiscreteVariable>(
                unimodel.getManifestVars());
        ArrayList<DiscreteVariable> cluster4var = new ArrayList<DiscreteVariable>(bestPair);

        // construct a LTM with 4 observed variables 2 latent variables
        LTM lCM = new LTM();
        BeliefNode h2 = lCM.addNode(new DiscreteVariable(2));
        BeliefNode h1 = lCM.addNode(new DiscreteVariable(2));

        for (DiscreteVariable var : bestPair) {
            lCM.addEdge(lCM.addNode(var), h1);
            cluster2BeAdded.remove(var);

        }

        for (DiscreteVariable var : ClosestPair) {
            lCM.addEdge(lCM.addNode(var), h2);
            cluster4var.add(var);
            cluster2BeAdded.remove(var);

        }
        lCM.addEdge(h2, h1);

        // copy parameters of unimodel to m1
        HashSet<String> donotUpdate = new HashSet<String>();
        ArrayList<DiscreteVariable> var1 = new ArrayList<DiscreteVariable>(
                lCM.getRoot().getCpt().getVariables());
        lCM.getRoot().getCpt().setCells(var1, unimodel.getRoot().getCpt().getCells());

        ArrayList<DiscreteVariable> var2s = new ArrayList<DiscreteVariable>(
                lCM.getNode(bestPair.get(0)).getCpt().getVariables());
        lCM.getNode(bestPair.get(0)).getCpt().setCells(var2s,
                unimodel.getNode(bestPair.get(0)).getCpt().getCells());
        var2s = new ArrayList<DiscreteVariable>(
                lCM.getNode(bestPair.get(1)).getCpt().getVariables());
        lCM.getNode(bestPair.get(1)).getCpt().setCells(var2s,
                unimodel.getNode(bestPair.get(1)).getCpt().getCells());

        donotUpdate.add(h1.getName());
        donotUpdate.add(bestPair.get(0).getName());
        donotUpdate.add(bestPair.get(1).getName());

        ParallelEmLearner emLearner = new ParallelEmLearner();
        emLearner.setLocalMaximaEscapeMethod("ChickeringHeckerman");
        emLearner.setMaxNumberOfSteps(_EmMaxSteps);
        emLearner.setNumberOfRestarts(_EmNumRestarts);
        // fix starting point or not?
        emLearner.setReuseFlag(false);
        emLearner.setThreshold(_emThreshold);
        emLearner.setDontUpdateNodes(donotUpdate);

        LTM LTM4var = (LTM) emLearner.em(lCM, data_proj.project(cluster4var));
        // System.out.println("--- Total Time for checking (EM): " +
        // (System.currentTimeMillis() - startcheck) + " ms ---");

        // Add the rest of variables to m1 and copy parameters
        LTM multimodel = LTM4var.clone();
        for (DiscreteVariable v : cluster2BeAdded) {

            multimodel.addEdge(multimodel.addNode(v), multimodel.getRoot());
            var2s = new ArrayList<DiscreteVariable>(
                    multimodel.getNode(v).getCpt().getVariables());
            multimodel.getNode(v).getCpt().setCells(var2s,
                    unimodel.getNode(v).getCpt().getCells());
        }

        return multimodel;
    }

    public class EmpiricalMiComputer {
        private final DiscreteDataSet data;
        private final List<DiscreteVariable> variables;
        private final boolean normalize;

        public EmpiricalMiComputer(DiscreteDataSet data, List<DiscreteVariable> variables,
                                   boolean normalize) {
            this.data = data;
            this.normalize = normalize;
            this.variables = variables;
        }

        /**
         * Computes the mutual information between two discrete variables.
         *
         * @param vi
         * @param vj
         * @return
         * @throws Exception
         */
        protected double compute(DiscreteVariable vi, DiscreteVariable vj) {
            Function pairDist = computeEmpDist(Arrays.asList(vi, vj), data);
            double mi = Utils.computeMutualInformation(pairDist);

            // use normalized version of MI.
            if (normalize) {
                // this version used in Strehl & Ghosh (2002)
                double enti = Utils.computeEntropy(pairDist.sumOut(vj));
                double entj = Utils.computeEntropy(pairDist.sumOut(vi));
                if (mi != 0) {
                    mi /= Math.sqrt(enti * entj);
                }
            }

            return mi;
        }

        /**
         * Computes a the mutual information between each pair of variables. It
         * does not contain any valid value on the diagonal.
         *
         * @return mutual information for each pair of variables
         */
        public double[][] computerPairwise() {
            Implementation implementation = new Implementation();
            implementation.computeParallel();
            return implementation.values;
        }

        /**
         * Implementation for computing
         *
         * @author kmpoon
         *
         */
        public class Implementation {
            private double[][] values;

            private Implementation() {
                this.values = new double[variables.size()][variables.size()];
            }

            // private void compute() {
            // computeFirstRange(0, variables.size());
            // }

            private void computeParallel() {
                ForkJoinPool pool = new ForkJoinPool();
                pool.invoke(new ParallelComputation(0, variables.size()));
            }

            private void computeFirstRange(int start, int end) {
                for (int i = start; i < end; i++) {
                    computeSecondRange(i, i + 1, variables.size());
                }
            }

            private void computeSecondRange(int base, int start, int end) {
                DiscreteVariable v1 = variables.get(base);
                for (int j = start; j < end; j++) {
                    DiscreteVariable v2 = variables.get(j);
                    values[base][j] = compute(v1, v2);
                    values[j][base] = values[base][j];
                }
            }

            @SuppressWarnings("serial")
            public class ParallelComputation extends RecursiveAction {

                private final int start;
                private final int end;
                private static final int THRESHOLD = 10;

                private ParallelComputation(int start, int end) {
                    this.start = start;
                    this.end = end;
                }

                private void computeDirectly() {
                    computeFirstRange(start, end);
                }

                @Override
                protected void compute() {
                    int length = end - start;
                    if (length <= THRESHOLD) {
                        computeDirectly();
                        return;
                    }

                    int split = length / 2;
                    invokeAll(new ParallelComputation(start, start + split),
                            new ParallelComputation(start + split, end));
                }
            }
        }

    }

    /**
     * Compute the empirical distribution of the given pair of variables
     */
    private Function computeEmpDist(List<DiscreteVariable> varPair, DiscreteDataSet _data) {
        DiscreteVariable[] vars = _data.getVariables();

        DiscreteVariable vi = varPair.get(0);
        DiscreteVariable vj = varPair.get(1);

        int viIdx = -1, vjIdx = -1;

        // retrieve P(Y|d) for latent variables and locate manifest variables
        Map<DiscreteDataCase, Function> viPosts = _latentPosts.get(vi);
        if (viPosts == null) {
            viIdx = Arrays.binarySearch(vars, vi);
        }

        Map<DiscreteDataCase, Function> vjPosts = _latentPosts.get(vj);
        if (vjPosts == null) {
            vjIdx = Arrays.binarySearch(vars, vj);
        }

        Function empDist = Function.createFunction(varPair);

        for (DiscreteDataCase datum : _data.getData()) {
            int[] states = datum.getStates();

            // If there is missing data, continue;
            if ((viIdx != -1 && states[viIdx] == -1)
                    || (vjIdx != -1 && states[vjIdx] == -1)) {
                continue;
            }
            // P(vi, vj|d) = P(vi|d) * P(vj|d)
            Function freq;

            if (viPosts == null) {
                freq = Function.createIndicatorFunction(vi, states[viIdx]);
            } else {
                freq = viPosts.get(datum);
            }

            if (vjPosts == null) {
                freq = freq.times(Function.createIndicatorFunction(vj, states[vjIdx]));
            } else {
                freq = freq.times(vjPosts.get(datum));
            }

            freq = freq.times(datum.getWeight());

            empDist.plus(freq);
        }

        empDist.normalize();

        return empDist;
    }

    private static Map<DiscreteVariable, Map<DiscreteVariable, Double>> processMi(List<DiscreteVariable> bestPair,
                                                                                  List<double[]> miArray, List<DiscreteVariable> vars) {
        // convert the array to map

        // initialize the data structure for pairwise MI
        Map<DiscreteVariable, Map<DiscreteVariable, Double>> mis = new HashMap<DiscreteVariable, Map<DiscreteVariable, Double>>(
                vars.size());

        double maxMi = Double.NEGATIVE_INFINITY;
        DiscreteVariable first = null, second = null;

        for (int i = 0; i < vars.size(); i++) {
            double[] row = miArray.get(i);

            Map<DiscreteVariable, Double> map = new HashMap<DiscreteVariable, Double>(vars.size());
            for (int j = 0; j < vars.size(); j++) {
                map.put(vars.get(j), row[j]);

                // find the best pair
                if (row[j] > maxMi) {
                    maxMi = row[j];
                    first = vars.get(i);
                    second = vars.get(j);
                }
            }

            mis.put(vars.get(i), map);

            // to allow garbage collection
            miArray.set(i, null);
        }

        // set the best pair
        bestPair.add(first);
        bestPair.add(second);

        return mis;

    }

    /**
     *
     * Return the best pair of variables with max MI in _mis.
     */
    private void findBestPair(ArrayList<DiscreteVariable> bestPair, Set<DiscreteVariable> VariablesSet) {
        // Initialize vars as _VarisblesSet
        List<DiscreteVariable> vars = new ArrayList<DiscreteVariable>(VariablesSet);

        List<DiscreteVariable> varPair = new ArrayList<DiscreteVariable>(2);
        varPair.add(null);
        varPair.add(null);

        double maxMi = Double.NEGATIVE_INFINITY;
        DiscreteVariable first = null, second = null;

        int nVars = vars.size();

        // enumerate all pairs of variables
        for (int i = 0; i < nVars; i++) {
            DiscreteVariable vi = vars.get(i);
            varPair.set(0, vi);

            for (int j = i + 1; j < nVars; j++) {
                DiscreteVariable vj = vars.get(j);
                varPair.set(1, vj);

                double mi = _mis.get(vi).get(vj);

                // update max MI and indices of best pair
                if (mi > maxMi) {
                    maxMi = mi;
                    first = vi;
                    second = vj;
                }
            }
        }

        // set the best pair
        bestPair.add(first);
        bestPair.add(second);
    }
}
