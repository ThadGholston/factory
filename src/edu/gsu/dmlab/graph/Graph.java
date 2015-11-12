package edu.gsu.dmlab.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.factory.interfaces.IGraphProblemFactory;
import edu.gsu.dmlab.graph.algo.interfaces.IGraphProblemSolver;
import edu.gsu.dmlab.tracking.interfaces.ILocationProbCal;
import edu.gsu.dmlab.tracking.interfaces.IObsModel;

/**
 * Created by thad on 11/7/15.
 */
public class Graph {

	IGraphProblemFactory factory;
	ArrayList<ITrack> tracks;
	int multFactor;
	static final String SOURCE = "Source";
	static final String SINK = "Sink";
	ILocationProbCal enterLocProbCalc;
	ILocationProbCal exitLocProbCalc;
	IObsModel obsModel;
	SimpleDirectedWeightedGraph<String, Edge> grph;
	HashMap<String, ITrack> leftTracks;
	HashMap<String, ITrack> rightTracks;

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

		grph = new SimpleDirectedWeightedGraph<String, Edge>(Edge.class);
		grph.addVertex(SOURCE);
		grph.addVertex(SINK);

		this.leftTracks = new HashMap<String, ITrack>();
		this.rightTracks = new HashMap<String, ITrack>();
	}

	public int[] getSourceSink() {
		return new int[] { 0, 1 };
	}

	private void addEdgeFromSource(ITrack to, String vertexName) {
		double entPd = this.enterLocProbCalc.calcProb(to.getFirst());
		double entP = -(Math.log(entPd) * multFactor);
		Edge edg = this.factory.getEdge(entP);
		this.grph.addEdge(SOURCE, vertexName, edg);
	}

	private void addEdgeToSink(ITrack from, String vertexName) {
		double exPd = this.exitLocProbCalc.calcProb(from.getLast());
		double exP = -(Math.log(exPd) * multFactor);
		Edge edg = this.factory.getEdge(exP);
		this.grph.addEdge(vertexName, SINK, edg);
	}

	private void addObserVationEdge(ITrack track, String vertexName1,
			String vertexName2) {

		double Bi = this.obsModel.getObsProb(track.getFirst());
		int obsCost = (int) (Math.log(Bi / (1.0 - Bi)) * multFactor);
		Edge edg = this.factory.getEdge(obsCost);
		this.grph.addEdge(vertexName1, vertexName2, edg);
	}

	public void addEdge(ITrack from, ITrack to, int weight) {
		String fromUUID1 = from.getFirst().getUUID().toString() + 1;
		String fromUUID2 = from.getLast().getUUID().toString() + 2;
		String toUUID1 = to.getFirst().getUUID().toString() + 1;
		String toUUID2 = to.getLast().getUUID().toString() + 2;

		if (this.grph.addVertex(fromUUID1)) {
			this.addEdgeFromSource(from, fromUUID1);
			this.grph.addVertex(fromUUID2);
			this.addEdgeToSink(from, fromUUID2);
			this.addObserVationEdge(from, fromUUID1, fromUUID2);
			this.rightTracks.put(fromUUID2, from);
			this.leftTracks.put(fromUUID1, from);
		}

		if (this.grph.addVertex(toUUID1)) {
			this.addEdgeFromSource(to, toUUID1);
			this.grph.addVertex(toUUID2);
			this.addEdgeToSink(to, toUUID2);
			this.addObserVationEdge(to, toUUID1, toUUID2);
			this.rightTracks.put(toUUID2, to);
			this.leftTracks.put(toUUID1, to);
		}
		Edge edg = this.factory.getEdge(weight);
		this.grph.addEdge(fromUUID2, toUUID1, edg);
	}

	public boolean solve() {
		if (this.grph.outDegreeOf("Source") > 0) {
			IGraphProblemSolver solver = this.factory.getGraphSolver();
			ArrayList<String[]> edgesList = solver.solve(grph, SOURCE, SINK);
			for (int i = 0; i < edgesList.size(); i++) {
				String[] edges = edgesList.get(i);
				
			}
		}
		return false;
	}

	public ArrayList<ITrack> getTrackLinked() {
		this.solve();
		return null;
	}

}
