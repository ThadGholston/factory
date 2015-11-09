package edu.gsu.dmlab.stages.interfaces;

import edu.gsu.dmlab.ObjectFactory;
import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.geometry.GeometryUtilities;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.geometry.Rectangle2D;
import edu.gsu.dmlab.indexes.interfaces.IIndexer;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Seconds;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * Created by thad on 9/23/15.
 */
public abstract class Stage<T extends IBaseDataType> {
    protected Configuration configuration;
    protected IIndexer indexer;
    protected IPositionPredictor positionPredictor;

    public Stage(IPositionPredictor positionPredictor, IIndexer<T> indexer) {
        if (indexer == null)
            throw new InvalidParameterException("IEventIndexer cannot be null");
        if (positionPredictor == null)
            throw new InvalidParameterException("IPositionPredictor cannot be null");
        this.indexer = indexer;
    }

    protected ArrayList<T> findElementsInSearchArea(ITrack track, IEvent event){
        double span;
        DateTime startSearch;
        DateTime endSearch;
        Point2D[] searchArea;
        ArrayList<IEvent> results = new ArrayList<>();
        int positionOfEvent = track.indexOf(event);
        if (track.get(positionOfEvent - 1) == null || track.get(positionOfEvent - 2) == null){
            //get the search area to find tracks that may belong linked to the current one being processed
            span = (event.getTimePeriod().toDurationMillis() / 1000) / secondsToDaysConstant;
            //set the time span to search in as the end of our current track+the
            //the span of the frame for the last event in our current track being processed.
            startSearch = event.getTimePeriod().getEnd();
            endSearch = startSearch.plus(event.getTimePeriod().toDurationMillis());
            searchArea = this.positionPredictor.getSearchRegion(event.getBBox(), span);
        } else {
            Interval interval = track.get(track.indexOf(event) - 1).getTimePeriod();
            startSearch = event.getTimePeriod().getEnd();
            endSearch = startSearch.plusSeconds(Seconds.secondsIn(event.getTimePeriod()).getSeconds());
            span = Seconds.secondsBetween(startSearch, endSearch).getSeconds() / secondsToDaysConstant;

            float[] motionVect = trackNormMeanMovement(track, positionOfEvent);
            searchArea = this.positionPredictor.getSearchRegion(event.getBBox(), motionVect, span);
        }
        Rectangle2D boundingBox = GeometryUtilities.createBoundingBox(searchArea);
        return this.indexer.filterOnIntervalAndLocation( new Interval(startSearch, endSearch), boundingBox);
    }

    protected Integer getIndex(ITrack track, int position){
        try {
            return track.indexOf(position);
        }catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    private float[] trackNormMeanMovement(ITrack track, int position) {
        float xMovement = 0;
        float yMovement = 0;
        float [] motionNormMean = new float[2];

        IEvent previousEvent = track.get(position);
        for(int eventIndex = position +  1; eventIndex < track.size(); eventIndex++){
            IEvent currentEvent = track.get(eventIndex);
            Point2D currentLocation = currentEvent.getLocation();
            Point2D firstLocation = previousEvent.getLocation();
            xMovement += currentLocation.getX() - firstLocation.getX();
            yMovement += currentLocation.getY() - firstLocation.getY();
            previousEvent = currentEvent;
        }

        if (track.size()>0) {
            motionNormMean[0] = xMovement / track.size();
            motionNormMean[1] = yMovement / track.size();
        } else {
            motionNormMean[0] = 0;
            motionNormMean[1] = 0;
        }
        return motionNormMean;
    }

    protected final int secondsToDaysConstant = 60 * 60 & 24;

    protected abstract ArrayList<ITrack> process();

    protected abstract void linkToNext(IEvent event);
}
