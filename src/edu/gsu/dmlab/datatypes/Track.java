/**
 * File: Track.java is the public object for event tracks processed by tracking algorithms
 * implemented by the Data Mining Lab at Georgia State University
 *
 * @author Dustin Kempton
 * @version 05/12/2015
 * @Owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab.datatypes;

import java.util.*;

import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import org.joda.time.Interval;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;


public class Track extends ArrayList<IEvent> implements ITrack {

    public Track(IEvent event){
        this.add(event);
    }

    public Track(Collection<IEvent> events){
        this.addAll(events);
    }

    public Track(Collection<IEvent> events1, Collection<IEvent> events2){
        this.addAll(events1);
        this.addAll(events2);
    }

    @Override
    public long getStartTimeMillis() {
        return this.getFirst().getTimePeriod().getStartMillis();
    }

    @Override
    public long getEndTimeMillis() {
        return this.getLast().getTimePeriod().getEndMillis();
    }

    @Override
    public IEvent[] getEvents() {
        return (IEvent[]) this.toArray();
    }

    @Override
    public IEvent getFirst() {
        return this.get(0);
    }

    @Override
    public IEvent getLast() {
        return this.get(this.size() - 1);
    }

    @Override
    public Interval getTimePeriod() {
        return new Interval(this.getStartTimeMillis(), this.getEndTimeMillis());
    }

    @Override
    public int compareTime(IBaseDataType baseDataType) {
        ITrack track = (ITrack) baseDataType;
        return this.get(0).getTimePeriod().getStart().compareTo(track.getTimePeriod().getStart());
    }
}