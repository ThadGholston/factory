package edu.gsu.dmlab.indexes.interfaces;


import edu.gsu.dmlab.datatypes.interfaces.IEvent;

import org.joda.time.Interval;



/**
 * Created by thad on 9/21/15.
 */
public interface IEventIndexer extends IIndexer<IEvent>{
    int getExpectedChangePerFrame(Interval timePeriod);
}
