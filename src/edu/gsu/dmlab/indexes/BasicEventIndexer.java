package edu.gsu.dmlab.indexes;

import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.geometry.GeometryUtilities;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.geometry.Rectangle2D;
import edu.gsu.dmlab.indexes.interfaces.AbsMatIndexer;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import org.apache.commons.configuration.ConfigurationException;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Seconds;

import java.util.*;

/**
 * Created by thad on 10/11/15.
 */
public class BasicEventIndexer extends AbsMatIndexer implements IEventIndexer {
    Duration frameSpan;
    Interval globalTimePeriod;
    HashMap<Long, ArrayList<IEvent>> frames;
    public BasicEventIndexer(ArrayList regionalList) throws ConfigurationException {
        super(regionalList);
        frameSpan = new Duration(0,1); //TODO: Get real frame interval
        globalTimePeriod = new Interval(0, 1); // TODO: Get real duration
        this.buildIndex();
    }

    @Override
    protected void buildIndex(){
        objectList.parallelStream().forEach(event -> indexEvent((IEvent) event));
    }

    @Override
    public ArrayList<IEvent> filterOnInterval(EventType type, Interval timePeriod) {
        HashMap<UUID, IEvent> results = new HashMap<>();
        if(!this.globalTimePeriod.overlaps(timePeriod)){
            return (ArrayList<IEvent>)results.values();
        }
        Interval intersection = this.globalTimePeriod.overlap(timePeriod);
        ArrayList<Long> periodIndexes = getFrameIndex(intersection);
        for(Long index: periodIndexes){
            for(IEvent event: this.frames.getOrDefault(index, new ArrayList<>())){
                if (event.getTimePeriod().overlaps(intersection)) {
                    results.put(event.getUUID(), event);
                }
            }
        }
        return (ArrayList<IEvent>)results.values();
    }

    private void indexEvent(IEvent event){
        insertEventIntoSearchSpace(event);
        resizeGlobalPeriod(event.getTimePeriod());
        buildFrameIndex(event);
    }

    private void insertEventIntoSearchSpace(IEvent event){
        Point2D[] shape = event.getShape();
        Point2D[] searchArea = GeometryUtilities.getScaledSearchArea(shape, regionDivisor);
        Rectangle2D boundingBox = GeometryUtilities.createBoundingBox(searchArea);
        for(int x = (int)boundingBox.getMinX(); x < boundingBox.getMaxX(); x++){
            for(int y = (int)boundingBox.getMinY(); y < boundingBox.getMaxY(); y++){
                if (GeometryUtilities.isInsideSearchArea(new Point2D(x, y), searchArea)){
                    if (searchSpace[x][y][event.getType().getValue()].size() == 0){
                        searchSpace[x][y][event.getType().getValue()].add(event);
                    } else {
                        for (int i = 0; i < searchSpace[x][y][event.getType().getValue()].size(); i++){
                            if (!event.isBefore((IEvent)searchSpace[x][y][event.getType().getValue()].get(i)) == false){
                                searchSpace[x][y][event.getType().getValue()].add(i, event);
                            }
                        }
                    }

                }
            }
        }
    }

    private void buildFrameIndex(IEvent event){
        Interval timePeriod = event.getTimePeriod();
        resizeGlobalPeriod(timePeriod);
        ArrayList<Long> indexes = getFrameIndex(timePeriod);
        for (Long index: indexes){
            ArrayList<IEvent> frame = frames.getOrDefault(index, new ArrayList<>());
            frame.add(event);
            this.frames.put(index, frame);
        }
    }

    private void resizeGlobalPeriod(Interval timePeriod){
        //TODO: Resize globalTimePeriod
        if (this.globalTimePeriod == null){
            long length = Seconds.secondsIn(timePeriod).getSeconds()/ this.frameSpan.getStandardSeconds();
            DateTime end = timePeriod.getStart().plus(this.frameSpan.getStandardSeconds() * length + this.frameSpan.getStandardSeconds());
            this.globalTimePeriod = new Interval(timePeriod.getStart(), end);
        } else {
            this.globalTimePeriod = union(this.globalTimePeriod, timePeriod);
        }
    }

    private ArrayList<Long> getFrameIndex(Interval timePeriod){
        ArrayList<Long> indexes = new ArrayList<>();
        Duration difference = new Duration(timePeriod.overlap(this.globalTimePeriod));
        long beginningIndex = difference.getStandardSeconds() / this.frameSpan.getStandardSeconds() + 1;
        long endingIndex = beginningIndex + new Duration(timePeriod).getStandardSeconds() + this.frameSpan.getStandardSeconds();
        for (long index = beginningIndex; index <= endingIndex; index++){
            Duration durationAfterIndexStart = new Duration(this.frameSpan.getStandardSeconds() * index);
            Interval indexPeriod = new Interval(this.globalTimePeriod.getStart().plus(durationAfterIndexStart), this.globalTimePeriod.getStart().plus(durationAfterIndexStart.getStandardSeconds() + this.frameSpan.getStandardSeconds()));
            if (timePeriod.overlaps(indexPeriod)){
                indexes.add(index);
            }
        }
        return indexes;
    }


    private Interval union( Interval firstInterval, Interval secondInterval )
    {
        // Purpose: Produce a new Interval instance from the outer limits of any pair of Intervals.

        // Take the earliest of both starting date-times.
        DateTime start =  firstInterval.getStart().isBefore( secondInterval.getStart() )  ? firstInterval.getStart() : secondInterval.getStart();
        // Take the latest of both ending date-times.
        DateTime end =  firstInterval.getEnd().isAfter( secondInterval.getEnd() )  ? firstInterval.getEnd() : secondInterval.getEnd();
        // Instantiate a new Interval from the pair of DateTime instances.
        Interval unionInterval = new Interval( start, end );

        return unionInterval;
    }

    @Override
    public int getExpectedChangePerFrame(Interval timePeriod) {
        if (timePeriod.getEnd().isBefore(this.globalTimePeriod.getStart())){
            return 0;
        }else if (timePeriod.getStart().isAfter(this.globalTimePeriod.getEnd())) {
            return 0;
        }
        Interval intersection = this.globalTimePeriod.overlap(timePeriod);
        ArrayList<Long> periodIndexes = getFrameIndex(intersection);
        double sum = 0.0;
        if (periodIndexes.size() > 1){
            ArrayList<IEvent> events = this.frames.getOrDefault(0, new ArrayList<>());
            double lastValue = events.size();
            for (int i = 0; i < periodIndexes.size(); i++){
                events= this.frames.getOrDefault(i, new ArrayList<>());
                double currentValue = events.size();
                sum += Math.abs(currentValue - lastValue);
                lastValue = currentValue;
            }
        }
        return (int) sum / periodIndexes.size();
    }

}
