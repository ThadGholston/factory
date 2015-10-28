package edu.gsu.dmlab.indexes.interfaces;

import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.geometry.Rectangle2D;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;

/**
 * Created by thad on 10/11/15.
 */
public interface IIndexer<T extends IBaseDataType> {
    DateTime getFirstTime();
    DateTime getLastTime();
    ArrayList<T> filterOnInterval(EventType type, Interval timePeriod);
    ArrayList<T> filterOnIntervalAndLocation(EventType type, Interval timePeriod, Rectangle2D boundingBox);
    ArrayList<T> getAll();
}
