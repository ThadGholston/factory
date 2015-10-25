package edu.gsu.dmlab.indexes.interfaces;

import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.geometry.Rectangle2D;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;

/**
 * Created by thad on 9/21/15.
 */
public interface IEventIndexer<T extends IBaseDataType> extends IIndexer<T> {
    int getExpectedChangePerFrame(Interval timePeriod);
}
