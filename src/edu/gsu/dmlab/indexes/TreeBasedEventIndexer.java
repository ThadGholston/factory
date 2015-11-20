package edu.gsu.dmlab.indexes;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.factory.interfaces.IIndexFactory;
import edu.gsu.dmlab.geometry.GeometryUtilities;
import edu.gsu.dmlab.indexes.datastructures.GITree;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thad on 11/14/15.
 */
public class TreeBasedEventIndexer implements IEventIndexer {
    Duration frameSpan;
    Interval globalTimePeriod;
    private int regionDimension;
    private int regionDivisor;
    private GITree<IEvent> tree;
    private ArrayList<IEvent> regionalList;

    HashMap<Integer, ArrayList<IEvent>> frames = new HashMap<Integer, ArrayList<IEvent>>();

    public TreeBasedEventIndexer(ArrayList<IEvent> regionalList,
                                 int regionDimension, int regionDivisor, Duration frameSpan) throws IllegalArgumentException {
        if (regionalList == null) {
            throw new IllegalArgumentException();
        }
        if (regionDimension < 1) {
            throw new IllegalArgumentException("region dimension cannot be less than 1.");
        }
        if (regionDivisor < 1) {
            throw new IllegalArgumentException("region divisor cannot be less than 1.");
        }
        if (frameSpan == null)
            throw new IllegalArgumentException("FrameSpan cannot be null");

        if (frameSpan.getStandardSeconds() < 1)
            throw new IllegalArgumentException(
                    "FrameSpan cannot be a duration less than 1");
        this.regionalList = regionalList;
        this.frameSpan = frameSpan;
        this.globalTimePeriod = null; // the build index should
        this.regionDimension = regionDimension;
        this.regionDivisor = regionDivisor;
        this.tree = new GITree<>(4096, 4096);
        // expand this as events are
        // indexed.
        this.buildIndex();
    }

    @Override
    public DateTime getFirstTime() {
        return globalTimePeriod.getStart();
    }

    @Override
    public DateTime getLastTime() {
        return globalTimePeriod.getEnd();
    }

    @Override
    public ArrayList<IEvent> search(Interval timePeriod) {
        ArrayList<IEvent> events = tree.search(timePeriod.getStart(), timePeriod.getEnd());
        Map<UUID, IEvent> map = new ConcurrentHashMap<>();
        for (IEvent evnt: events){
            map.put(evnt.getUUID(), evnt);
        }
        ArrayList<IEvent> retList = new ArrayList<>();
        retList.addAll(map.values());
        return retList;
    }

    @Override
    public ArrayList<IEvent> search(Interval timePeriod, Polygon searchArea) {
        Polygon scaledSearchArea = GeometryUtilities.scalePolygon(searchArea,
                this.regionDivisor);
        Rectangle searchBoundingBox = scaledSearchArea.getBounds();
        ArrayList<IEvent> events = tree.search(timePeriod.getStart(), timePeriod.getEnd(), searchBoundingBox);
        Map<UUID, IEvent> map = new ConcurrentHashMap<>();
        for (IEvent evnt: events){
            map.put(evnt.getUUID(), evnt);
        }
        ArrayList<IEvent> retList = new ArrayList<>();
        retList.addAll(map.values());
        return retList;
    }

    @Override
    public ArrayList<IEvent> getAll() {
        return regionalList;
    }
    private void buildIndex() {

        // add all the objects to the index
        regionalList.parallelStream()
                .forEach(event -> indexEvent((IEvent) event));
    }

    private void indexEvent(IEvent event){
        insertEventIntoSearchSpace(event);
        resizeGlobalPeriod(event.getTimePeriod());
        buildFrameIndex(event);
    }

    private void insertEventIntoSearchSpace(IEvent event) {
        long startTime = event.getTimePeriod().getStartMillis();
        long endTime = event.getTimePeriod().getEndMillis();
        tree.insert(event, startTime, endTime, event.getBBox());
        Polygon shape = event.getShape();
        Polygon scaledShape = GeometryUtilities.scalePolygon(shape,
                regionDivisor);
        Rectangle boundingBox = scaledShape.getBounds();
        tree.insert(event, startTime, endTime, boundingBox);
    }

    private void buildFrameIndex(IEvent event) {
        Interval timePeriod = event.getTimePeriod();
        this.resizeGlobalPeriod(timePeriod);
        ArrayList<Integer> indexes = getFrameIndex(timePeriod);
        for (int index : indexes) {
            ArrayList<IEvent> frame = frames.getOrDefault(index, null);
            if (frame != null) {
                frame.add(event);
            } else {
                frame = new ArrayList<IEvent>();
                frame.add(event);
                this.frames.put(index, frame);
            }
        }
    }

    private void resizeGlobalPeriod(Interval timePeriod) {

        if (this.globalTimePeriod == null) {
            long length = timePeriod.toDurationMillis()
                    / this.frameSpan.getMillis() + 1;
            DateTime end = timePeriod.getStart().plus(
                    this.frameSpan.getMillis() * length);
            this.globalTimePeriod = new Interval(timePeriod.getStart(), end);
        } else {
            this.globalTimePeriod = union(this.globalTimePeriod, timePeriod);
        }
    }

    /***
     * This method assumes that the input interval is within the global time
     * period. Make sure to expand the global interval if this is not the case,
     * or trim the input timePeriod prior to calling.
     *
     * @param timePeriod
     *            the time period we wish to get valid index locations for
     * @return the set of index locations the input period intersects
     */
    private ArrayList<Integer> getFrameIndex(Interval timePeriod) {

        // find the beginning index location
        long beginningIndex = (this.globalTimePeriod.getStartMillis() - timePeriod
                .getStartMillis()) / this.frameSpan.getMillis();

        long numIdPositions = (timePeriod.toDurationMillis() / this.frameSpan
                .getMillis());

        if (timePeriod.toDurationMillis() % this.frameSpan.getMillis() != 0) {
            numIdPositions += 1;
        }

        // find the ending index location
        long endingIndex = beginningIndex + numIdPositions;

        // construct the array of indexes to return
        ArrayList<Integer> indexes = new ArrayList<>();
        for (long index = beginningIndex; index <= endingIndex; index++) {
            indexes.add((int) index);
        }
        return indexes;
    }

    private Interval union(Interval firstInterval, Interval secondInterval) {
        // Purpose: Produce a new Interval instance from the outer limits of any
        // pair of Intervals.

        // Take the earliest of both starting date-times.
        DateTime start = firstInterval.getStart().isBefore(
                secondInterval.getStart()) ? firstInterval.getStart()
                : secondInterval.getStart();
        // Take the latest of both ending date-times.
        DateTime end = firstInterval.getEnd().isAfter(secondInterval.getEnd()) ? firstInterval
                .getEnd() : secondInterval.getEnd();
        // Instantiate a new Interval from the pair of DateTime instances.
        Interval unionInterval = new Interval(start, end);

        return unionInterval;
    }

    @Override
    public int getExpectedChangePerFrame(Interval timePeriod) {
        if (timePeriod.getEnd().isBefore(this.globalTimePeriod.getStart())) {
            return 0;
        } else if (timePeriod.getStart()
                .isAfter(this.globalTimePeriod.getEnd())) {
            return 0;
        }
        Interval intersection = this.globalTimePeriod.overlap(timePeriod);
        ArrayList<Integer> periodIndexes = getFrameIndex(intersection);
        double sum = 0.0;
        if (periodIndexes.size() > 1) {
            ArrayList<IEvent> events = this.frames.getOrDefault(0,
                    new ArrayList<>());
            double lastValue = events.size();
            for (int i = 0; i < periodIndexes.size(); i++) {
                events = this.frames.getOrDefault(i, new ArrayList<>());
                double currentValue = events.size();
                sum += Math.abs(currentValue - lastValue);
                lastValue = currentValue;
            }
        }
        return (int) sum / periodIndexes.size();
    }
}
