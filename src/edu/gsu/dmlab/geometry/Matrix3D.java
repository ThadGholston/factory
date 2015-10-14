package edu.gsu.dmlab.geometry;

import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * Created by thad on 9/24/15.
 */
public class Matrix3D<T extends IBaseDataType>  {

    private ArrayList<ArrayList<ArrayList<T>>> matrix;
//    private int dimension;
//    private int divisor;
    private int x;
    private int y;
    private int w;

    public Matrix3D(int x, int y, int w) {
        this.matrix = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.w = w;

        for(int i =0; i < x; i++){
            matrix.add(i, new ArrayList<ArrayList<T>>(y));
            for(int j = 0; j < y; j++){
                matrix.get(i).add(j, new ArrayList<T>());
            }
        }
    }

    public void add(int x, int y, T object) throws IndexOutOfBoundsException {
        try {
            matrix.get(x).get(y).add(object);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public ArrayList<T> get(int x, int y) throws IndexOutOfBoundsException {
        try {
            return matrix.get(x).get(y);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public void sort3Dimesion(Comparator<T> comparator) {
        IntStream.range(0, this.x).parallel().forEach(x ->{
            IntStream.range(0, this.y).parallel().forEach(y ->{
                matrix.get(x).get(y).parallelStream().sorted(comparator);
            });
        });
    }

}
