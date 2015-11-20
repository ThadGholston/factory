package edu.gsu.dmlab.indexes.datastructures;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.index.strtree.SIRtree;
import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import org.joda.time.DateTime;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thad on 11/18/15.
 */
public class GITree<T extends IBaseDataType> {
    Map<Coordinate, SIRtree> grid;
    private int deltaX;
    private int deltaY;

    public GITree(int deltaX, int deltaY) {
        this.deltaX = 4;
        this.deltaY = 4;
        grid = new ConcurrentHashMap<Coordinate, SIRtree>();
    }

    public void insert(T element, long startTime, long endTime, Rectangle mbr) {
        HashSet<Coordinate> cells = getCells(mbr);
        if (cells.size() == 0) {
            System.out.println("There is a problem when getting cells (GRt Index insertion)");
        } else {
            for (Coordinate c : cells) {
                if (grid.containsKey(c)) {
                    grid.get(c).insert(startTime, endTime, element);
                } else {
                    SIRtree cellIntervalRtree = new SIRtree();
                    cellIntervalRtree.insert(startTime, endTime, element);
                    grid.put(c, cellIntervalRtree);
                }
            }
        }
    }

    public ArrayList<T> search(DateTime startTime, DateTime endTime) {
        long startTimeInMilli = startTime.getMillis();
        long endTimeInMilli = endTime.getMillis();
        return temporalSearch(grid.keySet(), startTimeInMilli, endTimeInMilli);
    }

    public ArrayList<T> search(DateTime startTime, DateTime endTime, Rectangle mbr) {
        long startTimeInMilli = startTime.getMillis();
        long endTimeInMilli = endTime.getMillis();
        ArrayList<T> results = new ArrayList<>();
        HashSet<Coordinate> cells = spatialSearch(mbr);
        for (Coordinate c : cells) {
            SIRtree tree = grid.get(c);
            if (tree != null) {
                List<T> l = tree.query(startTimeInMilli, endTimeInMilli);
                results.addAll(l);
            }
        }
        return results;
    }

    private ArrayList<T> temporalSearch(Set<Coordinate> coordinates, long startTime, long endTime) {
        ArrayList<T> resultingTrajIDs = new ArrayList<>();
        for (Coordinate c : coordinates) {
            List<T> l = grid.get(c).query(startTime, endTime);
            resultingTrajIDs.addAll(l);
        }
        return resultingTrajIDs;
    }

    private HashSet<Coordinate> spatialSearch(Rectangle mbr) {
        return getCells(mbr);
    }

    private HashSet<Coordinate> getCells(Rectangle mbr) {
        HashSet<Coordinate> mbrCells = new HashSet<Coordinate>();
        double min_x = mbr.getMinX();
        double max_x = mbr.getMaxX();
        double min_y = mbr.getMinY();
        double max_y = mbr.getMaxY();

        for (Integer xCell = (int) (min_x / deltaX); xCell <= (int) (max_x / deltaX); xCell++) {
            for (Integer yCell = (int) (min_y / deltaY); yCell <= (int) (max_y / deltaY); yCell++) {
                Coordinate cell = new Coordinate(xCell.doubleValue(), yCell.doubleValue());
                mbrCells.add(cell);
            }
        }
        return mbrCells;
    }
}
