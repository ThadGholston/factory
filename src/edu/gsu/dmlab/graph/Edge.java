package edu.gsu.dmlab.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Created by thad on 10/3/15.
 */
public class Edge extends DefaultWeightedEdge {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7185657615982000843L;
	double weight;

	public Edge(double weight) {
		super();
		this.weight = weight;
	}

	@Override
	public double getWeight() {
		return this.weight;
	}

	@Override
	public Object clone() {
		return new Edge(this.getWeight());
	}
}
