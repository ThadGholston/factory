/**
 * File StageOne.java is the first stage in the iterative tracking 
 * algorithm of Kempton et al.,  http://dx.doi.org/10.1016/j.ascom.2015.10.005.
 * It uses the search area to link events into tracks iff there is one and only 
 * one available event as a possibility.
 * 
 * @author Dustin Kempton
 * @version 11/11/2015
 * @owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab.stages;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.factory.interfaces.IObjectFactory;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.stages.interfaces.IProcessingStage;
import edu.gsu.dmlab.util.Utility;
import edu.gsu.dmlab.util.interfaces.ISearchAreaProducer;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Seconds;

import java.awt.Polygon;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class StageOne implements IProcessingStage {

	ISearchAreaProducer searchAreaProducer;
	IEventIndexer eventIndexer;
	IObjectFactory factory;

	public StageOne(ISearchAreaProducer searchAreaProducer,
			IEventIndexer eventIndexer, IObjectFactory factory) {
		if (eventIndexer == null)
			throw new InvalidParameterException("IEventIndexer cannot be null");
		if (searchAreaProducer == null)
			throw new InvalidParameterException(
					"IPositionPredictor cannot be null");
		if (factory == null)
			throw new InvalidParameterException("Object Factory cannot be null");

		this.searchAreaProducer = searchAreaProducer;
		this.eventIndexer = eventIndexer;
		this.factory = factory;
	}

	@Override
	public ArrayList<ITrack> process() {
		DateTime startTime = this.eventIndexer.getFirstTime();
		DateTime endTime = this.eventIndexer.getLastTime();

		/*
		 * for each event between start and end, find events that are in the
		 * next frame after the current one we are looking at. Then link an
		 * event and the current one together iff it is the only one in the
		 * search box determined by our position predictor and the length of the
		 * current event.
		 */
		Interval timePeriod = new Interval(startTime, endTime);
		ArrayList<IEvent> events = this.eventIndexer
				.filterOnInterval(timePeriod);
		events.parallelStream().forEach(e -> linkToNext(e));

		// Now we determine how many tracks we have.
		HashMap<UUID, ITrack> tracksMap = new HashMap<>();
		for (int i = 0; i < events.size(); i++) {
			IEvent currEvent = events.get(i);
			while (currEvent.getPrevious() != null) {
				currEvent = currEvent.getPrevious();
			}

			// if it is already in the map then move along.
			UUID key = currEvent.getUUID();
			if (!tracksMap.containsKey(key)) {

				// put all the events into a list
				ArrayList<IEvent> trackEvents = new ArrayList<IEvent>();
				trackEvents.add(currEvent);
				while (currEvent.getNext() != null) {
					currEvent = currEvent.getNext();
					trackEvents.add(currEvent);
				}

				// create a track and add it to the map
				ITrack track = this.factory.getTrack(trackEvents);
				tracksMap.put(key, track);
			}
		}

		return (ArrayList<ITrack>) tracksMap.values();
	}

	private void linkToNext(IEvent event) {

		// only if the event isn't already linked to another event do we do
		// anything with it.
		if (event.getNext() == null) {
			// get the search are for the neighborhood
			double span = Seconds.secondsIn(event.getTimePeriod()).getSeconds()
					/ Utility.SECONDS_TO_DAYS;
			Polygon searchArea = this.searchAreaProducer.getSearchRegion(
					event.getBBox(), span);

			// get the time interval for the neighborhood
			Interval time = event.getTimePeriod();
			Interval nextTime = new Interval(time.getEndMillis(),
					time.getEndMillis() + time.toDuration().getMillis());

			// Get the events in the neighborhood
			ArrayList<IEvent> eventsInArea = this.eventIndexer
					.filterOnIntervalAndLocation(nextTime, searchArea);

			// Only if there is one do we process
			if (eventsInArea.size() == 1) {
				IEvent nextEvent = eventsInArea.get(0);
				// Only if the next event is not linked
				// do we link it to this event.
				if (nextEvent.getPrevious() == null) {
					event.setNext(nextEvent);
					nextEvent.setPrevious(event);
				}
			}
		}
	}
}
