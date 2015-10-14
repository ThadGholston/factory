package edu.gsu.dmlab.indexes.interfaces;

import edu.gsu.dmlab.geometry.Point2D;
import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * Created by thad on 10/11/15.
 */
public interface IIndexer<T> {
    public ArrayList<T> getBetween(DateTime start, DateTime end);

    public ArrayList<T> getBetween(DateTime start, DateTime end, Point2D[] searchArea);

    public DateTime getFirstTime();

    public DateTime getLastTime();
}
