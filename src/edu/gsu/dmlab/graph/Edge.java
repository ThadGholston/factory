package edu.gsu.dmlab.graph;


import org.jgrapht.graph.DefaultEdge;

/**
 * Created by thad on 10/3/15.
 */
public class Edge extends DefaultEdge {

    private int capacity;

    public Edge(int capacity){
        super();
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
