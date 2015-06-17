/**
 * 
 * File: GenaricEvent.java is a genaric event type used to represent a single
 * detection of a solar event taken from HEK.  
 * @author Dustin Kempton
 * @version 05/12/2015 
 * @Owner Data Mining Lab, Georgia State University
 */

package edu.gsu.dmlab.datatypes;

import java.util.UUID;

import org.joda.time.Interval;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;

public class GenaricEvent implements IEvent {

	private int id;
	private Point[] poly = null;
	private Rect bBox = null;
	private Point location = null;
	private Interval timePeriod = null;
	private String type = null;

	IEvent next = null;
	IEvent previous = null;
	UUID uniqueId = null;

	public GenaricEvent(int id, Interval timePeriod, Point location, Rect bbox,
			Point[] poly, String type) {
		if (timePeriod == null)
			throw new IllegalArgumentException(
					"Interval cannot be null in GenaricEvet constructor.");
		if (location == null)
			throw new IllegalArgumentException(
					"Point cannot be null in GenaricEvet constructor.");
		if (bbox == null)
			throw new IllegalArgumentException(
					"Rect cannot be null in GenaricEvet constructor.");
		if (poly == null)
			throw new IllegalArgumentException(
					"Point[] cannot be null in GenaricEvet constructor.");
		if (type == null)
			throw new IllegalArgumentException(
					"Type cannot be null in GenaricEvet constructor.");

		this.id = id;
		this.poly = poly;
		this.bBox = bbox;
		this.location = location;
		this.timePeriod = timePeriod;
		this.type = type;
		this.uniqueId = UUID.randomUUID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gsu.dmlab.datatypes.interfaces.IEvent#getId()
	 */
	@Override
	public int getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gsu.dmlab.datatypes.interfaces.IEvent#getLocation()
	 */
	@Override
	public Point getLocation() {
		return this.location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gsu.dmlab.datatypes.interfaces.IEvent#getBBox()
	 */
	@Override
	public Rect getBBox() {
		return this.bBox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gsu.dmlab.datatypes.interfaces.IEvent#getShape()
	 */
	@Override
	public Point[] getShape() {
		return this.poly;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gsu.dmlab.datatypes.interfaces.IEvent#getTimePeriod()
	 */
	@Override
	public Interval getTimePeriod() {
		return this.timePeriod;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gsu.dmlab.datatypes.interfaces.IEvent#updateTimePeriod(org.joda.time
	 * .Interval)
	 */
	@Override
	public void updateTimePeriod(Interval period) {
		this.timePeriod = period;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gsu.dmlab.datatypes.interfaces.IEvent#getType()
	 */
	@Override
	public String getType() {
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gsu.dmlab.datatypes.interfaces.IEvent#getPrevious()
	 */
	@Override
	public IEvent getPrevious() {
		return this.previous;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gsu.dmlab.datatypes.interfaces.IEvent#setPrevious(edu.gsu.dmlab.datatypes
	 * .interfaces.IEvent)
	 */
	@Override
	public void setPrevious(IEvent event) {
		if (this.previous == null) {
			this.previous = event;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gsu.dmlab.datatypes.interfaces.IEvent#getNext()
	 */
	@Override
	public IEvent getNext() {
		return this.next;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gsu.dmlab.datatypes.interfaces.IEvent#setNext(edu.gsu.dmlab.datatypes
	 * .interfaces.IEvent)
	 */
	@Override
	public void setNext(IEvent event) {
		if (this.next == null) {
			this.next = event;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gsu.dmlab.datatypes.interfaces.IEvent#getUUID()
	 */
	@Override
	public UUID getUUID() {
		return this.uniqueId;
	}
}
