package edu.gsu.dmlab.indexes;

import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.indexes.interfaces.AbstractMatrixRangeIndexer;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;

/**
 * Created by thad on 10/11/15.
 */
public class MatrixBasedEventIndexer extends AbstractMatrixRangeIndexer implements IEventIndexer {
    public MatrixBasedEventIndexer(ArrayList list) {
        super(list);
    }

    @Override
    protected void index(){

    }

    @Override
    public int getExpectedChangePerFrame(Interval timePeriod) {
        return 0;
    }

    @Override
    public ArrayList<IEvent> getEventsInNeighborhood(DateTime begin, DateTime end, ArrayList<Point2D> searchArea, double neighborHoodMultiply) {
        return null;
    }
}
