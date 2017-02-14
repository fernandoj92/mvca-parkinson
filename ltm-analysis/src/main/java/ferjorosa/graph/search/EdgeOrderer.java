package ferjorosa.graph.search;

import ferjorosa.graph.AbstractNode;
import ferjorosa.graph.Edge;

import java.util.Collection;

/**
 * Gives an ordering to those alternatives edges so that the search 
 * explores the edges following this ordering. 
 * @author leonard
 *
 */
public interface EdgeOrderer {
    public Collection<Edge> order(AbstractNode current, Collection<Edge> edges);
}
