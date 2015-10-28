package edu.gsu.dmlab.graph.algo;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

/**
 * Created by thad on 9/26/15.
 */
public class SuccessiveShortestPaths {
    private SimpleDirectedWeightedGraph graph;
    public SuccessiveShortestPaths(SimpleDirectedWeightedGraph graph) {
        this.graph = graph;
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
