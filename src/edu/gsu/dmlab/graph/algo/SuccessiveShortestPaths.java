package edu.gsu.dmlab.graph.algo;

import java.util.ArrayList;

import edu.gsu.dmlab.graph.Edge;
import edu.gsu.dmlab.graph.algo.interfaces.IGraphProblemSolver;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

/**
 * Created by thad on 9/26/15.
 */
public class SuccessiveShortestPaths implements IGraphProblemSolver {
		
	@SuppressWarnings("unused")
	private long findFlowCost(Integer source, Integer sink) {
		return 0;
	}

	@Override
	public ArrayList<String[]> solve(
			SimpleDirectedWeightedGraph<String, Edge> graph, String source,
			String sink) {
		// We don't care to get the cost back. I only used it in the previous
		// work
		// to determine when we found the optimal number of tracks. I.E. when
		// the cost
		// started going back up instead of down.

		// We could return a solved graph?
		return null;
	}

	public int[] getCapacity() {
		// return capacity;
		// TODO: implement this
		return new int[0];
	}
}
