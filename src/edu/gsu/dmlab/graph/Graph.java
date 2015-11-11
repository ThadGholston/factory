package edu.gsu.dmlab.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.factory.interfaces.IGraphProblemFactory;
import edu.gsu.dmlab.graph.algo.interfaces.IGraphProblemSolver;
import edu.gsu.dmlab.tracking.interfaces.ILocationProbCal;
import edu.gsu.dmlab.tracking.interfaces.IObsModel;

/**
 * Created by thad on 11/7/15.
 */
public class Graph {

	int[][][] adjacencyMatrix;
	int source, sink;
	protected int edgeCap = 1; // all edges in graph will have a flow of 1
	IGraphProblemFactory factory;
	ArrayList<ITrack> tracks;
	HashMap<UUID, ITrack> usedTracksMap;
	int multFactor;

	ILocationProbCal enterLocProbCalc;
	ILocationProbCal exitLocProbCalc;
	IObsModel obsModel;

	public Graph(ArrayList<ITrack> tracks, IGraphProblemFactory factory,
			ILocationProbCal enterLocProbCalc,
			ILocationProbCal exitLocProbCalc, IObsModel obsModel, int edgeCap,
			int multFactor) {

		if (exitLocProbCalc == null)
			throw new IllegalArgumentException(
					"Exit Prob Calculator cannot be null.");
		if (enterLocProbCalc == null)
			throw new IllegalArgumentException(
					"Enter Prob Calculator cannot be null.");
		if (obsModel == null)
			throw new IllegalArgumentException(
					"Observation Model cannot be null.");

		this.tracks = tracks;
		this.factory = factory;
		this.exitLocProbCalc = exitLocProbCalc;
		this.enterLocProbCalc = enterLocProbCalc;
		this.obsModel = obsModel;

		this.usedTracksMap = new HashMap<UUID, ITrack>();
	}

	public int[] getSourceSink() {
		return new int[] { 0, 1 };
	}

	@SuppressWarnings("unused")
	private void addEdgeFromSource(ITrack to, int weight) {
		// add for those that are actually being used

		double entPd = this.enterLocProbCalc.calcProb(to.getFirst());
		int entP = -(int) (Math.log(entPd) * multFactor);

	}

	@SuppressWarnings("unused")
	private void addEdgeToSink(ITrack from, int weight) {
		// add for those that are actually being used

		double exPd = this.exitLocProbCalc.calcProb(from.getLast());
		int exP = -(int) (Math.log(exPd) * multFactor);

	}

	@SuppressWarnings("unused")
	private void addObserVationEdge(ITrack track) {
		// add for those that are actually being used

		double Bi = this.obsModel.getObsProb(track.getFirst());
		int obsCost = (int) (Math.log(Bi / (1.0 - Bi)) * multFactor);
	}

	public void addEdge(ITrack from, ITrack to, int weight) {

	}

	public boolean solve() {
		@SuppressWarnings("unused")
		IGraphProblemSolver solver = this.factory.getGraphSolver(this,
				this.source, this.sink);
		return false;
	}

	public ArrayList<ITrack> getTrackLinked() {
		this.solve();
		return null;
	}

}
