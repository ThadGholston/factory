package edu.gsu.dmlab.indexes.interfaces;

import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;

/**
 * Created by thad on 9/21/15.
 */
public interface IEventIndexer extends IIndexer {
    int getExpectedChangePerFrame(Interval timePeriod);
    ArrayList<IEvent> getEventsInNeighborhood(DateTime begin, DateTime end, ArrayList<Point2D> searchArea, double neighborHoodMultiply);
}
