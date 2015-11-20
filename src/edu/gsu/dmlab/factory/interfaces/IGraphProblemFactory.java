package edu.gsu.dmlab.factory.interfaces;

import java.util.ArrayList;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.graph.Edge;
import edu.gsu.dmlab.graph.Graph;
import edu.gsu.dmlab.graph.algo.interfaces.IGraphProblemSolver;

public interface IGraphProblemFactory {
	public IGraphProblemSolver getGraphSolver();

	public Graph getGraph(ArrayList<ITrack> tracks);

	public Edge getEdge(double weight);
}
