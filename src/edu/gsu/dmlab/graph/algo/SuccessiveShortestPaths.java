/**
 * File: SuccessiveShortestPath.java is used to find the
 * optimal flow through the passed in SimpleDirectedWeightedGraph
 * where some of the edges are negative and the graph is a DAG.
 *   
 * @author Dustin Kempton
 * @version 11/13/15 
 * @Owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab.graph.algo;

import java.util.ArrayList;
import java.util.List;

import edu.gsu.dmlab.graph.Edge;
import edu.gsu.dmlab.graph.algo.interfaces.IGraphProblemSolver;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;

public class SuccessiveShortestPaths implements IGraphProblemSolver {

	@SuppressWarnings("unused")
	private long findFlowCost(Integer source, Integer sink) {
		return 0;
	}

	@Override
	public ArrayList<String[]> solve(
			SimpleDirectedWeightedGraph<String, Edge> graph, String source,
			String sink) {

		@SuppressWarnings("unchecked")
		SimpleDirectedWeightedGraph<String, Edge> graphCopy = (SimpleDirectedWeightedGraph<String, Edge>) graph
				.clone();
		boolean done = false;
		ArrayList<String[]> edgesInSolution = new ArrayList<String[]>();
		while (!done) {

			// find the path from source to sink
			DijkstraShortestPath<String, Edge> dijkstra = new DijkstraShortestPath<String, Edge>(
					graphCopy, source, sink);
			GraphPath<String, Edge> path = dijkstra.getPath();

			// if path exists and costs is negative process otherwise we are
			// done
			if (path != null && path.getWeight() <= 0.0) {
				List<Edge> edges = path.getEdgeList();
				for (Edge edge : edges) {

					String edgSource = graphCopy.getEdgeSource(edge);
					String edgTarget = graphCopy.getEdgeTarget(edge);
					
					//if the edge is not from source or to the sink add to returned set
					if (!(edgSource.equalsIgnoreCase(source) || edgTarget
							.equalsIgnoreCase(sink))) {
						String[] tmpVerts = { edgSource, edgTarget };
						edgesInSolution.add(tmpVerts);
					}
					graphCopy.removeEdge(edge);
				}
			} else {
				done = true;
			}
		}

		return edgesInSolution;
	}

}
