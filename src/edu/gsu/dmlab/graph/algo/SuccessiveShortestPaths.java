package edu.gsu.dmlab.graph.algo;

import edu.gsu.dmlab.graph.Graph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

/**
 * Created by thad on 9/26/15.
 */
public class SuccessiveShortestPaths {
    Graph graph;
    int source;
    int sink;
    public SuccessiveShortestPaths(Graph graph, int source, int sink) {
        this.graph = graph;
        this.source = source;
        this.sink = sink;
    }

    public long findFlowCost(Integer source, Integer sink) {
        return 0;
    }

    public int[] getCapacity() {
        // return capacity;
        //TODO: implement this
        return new int[0];
    }
}
