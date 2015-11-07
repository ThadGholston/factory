package edu.gsu.dmlab.graph;

/**
 * Created by thad on 11/7/15.
 */
public class Graph {

    int [][][] adjacencyMatrix;
    public Graph(int size){
        adjacencyMatrix = new int[size][size][2];
    }

    public void addEdge(int v1, int v2,int weight,int edgeCap){
        adjacencyMatrix[v1][v2][0] = weight;
        adjacencyMatrix[v1][v2][1] = edgeCap;
    }
}
