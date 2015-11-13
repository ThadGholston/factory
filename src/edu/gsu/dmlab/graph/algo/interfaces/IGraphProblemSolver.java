package edu.gsu.dmlab.graph.algo.interfaces;

import java.util.ArrayList;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import edu.gsu.dmlab.graph.Edge;

public interface IGraphProblemSolver {
	/**
	 * Solves the graph that was
	 * 
	 * @return
	 */
	public ArrayList<String[]> solve(
			SimpleDirectedWeightedGraph<String, Edge> graph, String source,
			String sink);
}
