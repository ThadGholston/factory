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

import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.geometry.Rectangle2D;
import org.joda.time.Interval;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;

public class GenaricEvent implements IEvent {

	private int id;
	private Point2D[] poly = null;
	private Rectangle2D bBox = null;
	private Point2D location = null;
	private Interval timePeriod = null;
	private String type = null;

	IEvent next = null;
	IEvent previous = null;
	UUID uniqueId = null;

	public GenaricEvent(int id, Interval timePeriod, Point2D location, Rectangle2D bbox,
						Point2D[] poly, String type) {
		if (timePeriod == null)
			throw new IllegalArgumentException(
					"Interval cannot be null in GenaricEvet constructor.");
		if (location == null)
			throw new IllegalArgumentException(
					"Point2D cannot be null in GenaricEvet constructor.");
		if (bbox == null)
			throw new IllegalArgumentException(
					"Rectangle2D cannot be null in GenaricEvet constructor.");
		if (poly == null)
			throw new IllegalArgumentException(
					"Point2D[] cannot be null in GenaricEvet constructor.");
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
	public Point2D getLocation() {
		return this.location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gsu.dmlab.datatypes.interfaces.IEvent#getBBox()
	 */
	@Override
	public Rectangle2D getBBox() {
		return this.bBox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gsu.dmlab.datatypes.interfaces.IEvent#getShape()
	 */
	@Override
	public Point2D[] getShape() {
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

	@Override
	public int compareTime(IBaseDataType baseDataType) {
		return this.getTimePeriod().getStart().compareTo(baseDataType.getTimePeriod().getStart());
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