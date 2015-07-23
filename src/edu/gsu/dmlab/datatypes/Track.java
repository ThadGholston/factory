/**
 * File: Track.java is the public object for event tracks processed by tracking algorithms
 * implemented by the Data Mining Lab at Georgia State University
 * 
 * @author Dustin Kempton
 * @version 05/12/2015 
 * @Owner Data Mining Lab, Georgia State University
 *
 */
package edu.gsu.dmlab.datatypes;

import java.util.ArrayList;

import org.joda.time.Interval;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;

public class Track implements ITrack {

	IEvent first;
	IEvent last;
	Interval trackInterval = null;

	public Track(IEvent first, IEvent last) {
		if (first == null)
			throw new IllegalArgumentException(
					"IEvent first cannot be null in Track constructor.");
		if (last == null)
			throw new IllegalArgumentException(
					"IEvent last cannot be null in Track constructor.");

		this.first = first;
		this.last = last;
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
		IEvent current = this.getFirst();
		ArrayList<IEvent> list = new ArrayList<IEvent>();
		do {
			list.add(current);
			current = current.getNext();
		} while (!(current == null));

		IEvent[] results = new IEvent[list.size()];
		list.toArray(results);
		return results;
	}

	@Override
	public IEvent getFirst() {
		if (this.first.getPrevious() != null) {
			this.first = this.first.getPrevious();
			this.trackInterval = null;
			while (this.first.getPrevious() != null) {
				this.first = this.first.getPrevious();
			}
		}
		return this.first;
	}

	@Override
	public IEvent getLast() {
		if (this.last.getNext() != null) {
			this.last = this.last.getNext();
			this.trackInterval = null;
			while (this.last.getNext() != null) {
				this.last = this.last.getNext();
			}
		}
		return this.last;
	}

	@Override
	public Interval getTimePeriod() {
		if (this.trackInterval == null) {
			this.trackInterval = new Interval(this.getStartTimeMillis(),
					this.getEndTimeMillis());
		}
		return this.trackInterval;
	}

}
