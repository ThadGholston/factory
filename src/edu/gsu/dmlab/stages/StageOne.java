package edu.gsu.dmlab.stages;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.geometry.GeometryUtilities;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.geometry.Rectangle2D;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.stages.interfaces.Stage;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Seconds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by thad on 9/23/15.
 */
public class StageOne extends Stage {

    private IPositionPredictor positionPredictor;

    public StageOne(IPositionPredictor positionPredictor, IEventIndexer eventIndexer, Configuration configuration) throws ConfigurationException {
        super(positionPredictor, eventIndexer);
        this.positionPredictor = positionPredictor;
    }

    @Override
    public ArrayList<ITrack> process() {
        DateTime startTime = indexer.getFirstTime();
        DateTime endTime = indexer.getLastTime();
        HashMap<UUID, ITrack> eventToTrack = new HashMap<>();
        /*for each event between start and end, find events
		 * that are in the next frame after the current one we are looking at.
		 * Then link an event and the current one together iff it is the only
		 * one in the search box determined by our position predictor and the
		 * length of the current event.
		 */
        Interval timePeriod = new Interval(startTime, endTime);
        for (IEvent event: (ArrayList<IEvent>) indexer.filterOnInterval(timePeriod)){
            ITrack track = eventToTrack.getOrDefault(event.getUUID(), null);
            if (track != null || track.getLast().getUUID() == event.getUUID()){
                int positionOfTrack = track.indexOf(event);
                ArrayList<IEvent> inSearchArea = findElementsInSearchArea(track, event, positionOfTrack);
                if (inSearchArea.size() == 1){
                    UUID possibleEventUUID = inSearchArea.get(0).getUUID();
                    ITrack possibleTrack = eventToTrack.getOrDefault(possibleEventUUID, null);
                    if (possibleTrack != null &&
                            possibleTrack.getFirst().getUUID() == possibleEventUUID){
                        track.addAll(possibleTrack);
                        for (IEvent e: possibleTrack){
                            eventToTrack.put(e.getUUID(), track);
                        }
                    }
                }
            }
        }
        return (ArrayList<ITrack>) eventToTrack.values();
    }




}
