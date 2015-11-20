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

@SuppressWarnings("serial")
public class Track implements ITrack {

	private UUID uniqueId = null;
	IEvent headEvent;
	IEvent tailEvent;
	int size;
	ArrayList<IEvent> eventsList = null;
	boolean outDated = true;

	public Track(IEvent event) {
		this();
		this.headEvent = event;
		this.tailEvent = event;
	}

	public Track(Collection<IEvent> events) {
		this.eventsList = new ArrayList<IEvent>();
		events.forEach(ev -> {
			this.eventsList.add(ev);
		});
		this.headEvent = this.eventsList.get(0);
		this.tailEvent = this.eventsList.get(this.eventsList.size() - 1);
	}

	public Track(IEvent headEvent, IEvent tailEvent) {
		this();
		this.headEvent = headEvent;
		this.tailEvent = tailEvent;
	}

	private Track() {
		this.uniqueId = UUID.randomUUID();
	}

	@Override
	public long getStartTimeMillis() {
		return this.getFirst().getTimePeriod().getStartMillis();
	}

	@Override
	public long getEndTimeMillis() {
		return this.getLast().getTimePeriod().getEndMillis();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<IEvent> getEvents() {
		this.getFirst();
		this.getLast();
		if (this.outDated) {
			this.update();
		}
		return (ArrayList<IEvent>) this.eventsList.clone();
	}

	@Override
	public IEvent getFirst() {
		while (this.headEvent.getPrevious() != null) {
			this.outDated = true;
			this.headEvent = this.headEvent.getPrevious();
		}
		return this.headEvent;
	}

	@Override
	public IEvent getLast() {
		while (this.tailEvent.getNext() != null) {
			this.outDated = true;
			this.tailEvent = this.tailEvent.getNext();
		}
		return this.tailEvent;
	}

	@Override
	public Interval getTimePeriod() {
		return new Interval(this.getStartTimeMillis(), this.getEndTimeMillis());
	}

	@Override
	public int compareTime(IBaseDataType baseDataType) {
		ITrack track = (ITrack) baseDataType;
		return this.getFirst().getTimePeriod().getStart()
				.compareTo(track.getTimePeriod().getStart());
	}

	@Override
	public boolean intersects(Interval interval) {
		return interval.overlaps(interval);
	}

	@Override
	public boolean isBefore(Interval timePeriod) {
		return this.getTimePeriod().isBefore(timePeriod);
	}

	@Override
	public boolean isBefore(IBaseDataType obj) {
		return this.getTimePeriod().isBefore(obj.getTimePeriod());
	}

	@Override
	public boolean isAfter(Interval timePeriod) {
		return this.getTimePeriod().isAfter(timePeriod);
	}

	@Override
	public boolean isAfter(IBaseDataType obj) {
		return this.getTimePeriod().isAfter(obj.getTimePeriod());
	}

	@Override
	public EventType getType() {
		return this.getFirst().getType();
	}

	@Override
	public UUID getUUID() {
		return this.uniqueId;
	}

	@Override
	public int size() {
		this.getFirst();
		this.getLast();
		if (this.outDated) {
			this.update();
		}
		return this.eventsList.size();
	}

	private void update() {
		IEvent first = this.getFirst();
		@SuppressWarnings("unused")
		IEvent last = this.getLast();

		ArrayList<IEvent> evList = new ArrayList<IEvent>();
		while (first.getNext() != null) {
			evList.add(first);
			first = first.getNext();
		}
		this.eventsList = evList;
		this.outDated = false;
	}
}