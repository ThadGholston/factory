package edu.gsu.dmlab.stages;

import edu.gsu.dmlab.ObjectFactory;
import edu.gsu.dmlab.datatypes.Track;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.stages.interfaces.Stage;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by thad on 9/23/15.
 */
public class StageOne extends Stage {

    private IPositionPredictor positionPredictor;
    HashMap<UUID, ITrack> eventToTrack = new HashMap<>();
    HashMap<UUID, ArrayList<UUID>> trackToEvent = new HashMap<>();
    public StageOne(IPositionPredictor positionPredictor, IEventIndexer eventIndexer) {
        super(positionPredictor, eventIndexer);
    }

    @Override
    public ArrayList<ITrack> process() {
        DateTime startTime = indexer.getFirstTime();
        DateTime endTime = indexer.getLastTime();

        /*for each event between start and end, find events
         * that are in the next frame after the current one we are looking at.
		 * Then link an event and the current one together iff it is the only
		 * one in the search box determined by our position predictor and the
		 * length of the current event.
		 */
        Interval timePeriod = new Interval(startTime, endTime);
        ArrayList<IEvent> events = indexer.filterOnInterval(timePeriod);
        events.parallelStream().forEach(e -> addTrack(e));
        events.parallelStream().forEach(e -> linkToNext(e));
        return (ArrayList<ITrack>) eventToTrack.values();
    }

    private void addTrack(IEvent event){
        ITrack track = new Track(event);
        eventToTrack.put(event.getUUID(), track);
        ArrayList<UUID> list = new ArrayList<>();
        list.add(track.getUUID());
        trackToEvent.put(track.getUUID(), list);
    }

    @Override
    protected void linkToNext(IEvent event){
        ITrack track = eventToTrack.get(event.getUUID());
        int eventPosition = track.indexOf(event);
        if (getIndex(track, eventPosition + 1) == null || getIndex(track, eventPosition - 2) == null){
            ArrayList<IEvent> elementsInSearchArea = findElementsInSearchArea(track, event);
            if (elementsInSearchArea.size() == 1){
                IEvent nextEvent = elementsInSearchArea.get(0);
                ITrack nextTrack = eventToTrack.get(nextEvent.getUUID());
                int nextPosition = nextTrack.indexOf(nextEvent);
                if (getIndex(nextTrack, nextPosition - 1) == null &&
                        event.getTimePeriod().getStart().isAfter(nextEvent.getTimePeriod().getStart()) &&
                        !event.getUUID().equals(nextEvent.getUUID())){
                    ITrack mergedTrack = ObjectFactory.getNewTrack(track, nextTrack);
                    mergedTrack.parallelStream().forEach(e -> eventToTrack.put(e.getUUID(), mergedTrack));
                }
            }
        }
    }



}
