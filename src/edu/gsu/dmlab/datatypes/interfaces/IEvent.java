/**
 *
 * File: IEvent.java is the public interface for events processed by the tracking algorithms implemented
 * by the Data Mining Lab at Georgia State University.  
 *
 * @author Dustin Kempton
 * @version 05/09/2015 
 * @Owner Data Mining Lab, Georgia State University
 *
 */
package edu.gsu.dmlab.datatypes.interfaces;

import java.util.UUID;

import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.geometry.Point2D;
import org.joda.time.Interval;

import java.awt.Polygon;
import java.awt.Rectangle;

public interface IEvent extends IBaseDataType {

	/**
	 * Returns the Id of the event that this object represents
	 * @return	id of the event that this object represents
	 */
	int getId();

	/**
	 * Returns the center point of the event that this object represents
	 * @return	center point of the event
	 */
	Point2D getLocation();

	/**
	 * Returns the minimum bounding rectangle of the event that this object represents
	 * @return	minimum bounding rectangle of the event
	 */
	Rectangle getBBox();

	/**
	 * Returns the polygon representation of the event that this object represents
	 * @return	polygon representation of the event
	 */
	Polygon getShape();

	/**
	 * Updates the time period that this object is valid
	 * @param period	the new period for this object to be considered valid over
	 */
	void updateTimePeriod(Interval period);

	/**
	 * Returns the type of event that this object represents. It is usually a two letter
	 * designation such as AR for Active IRegion, SS for Sun Spot etc.
	 * @return	the type of event that this object represents.
	 */
	EventType getType();

	/**
	 * Returns a pointer to the event detection that is considered the previous detection of the
	 * same object.
	 * @return	pointer to the previous detection in the chain.
	 */
	IEvent getPrevious();

	/**
	 * Sets a pointer to the event detection that is considered the previous detection of the
	 * same object.  This method only changes the pointer from null to a non-null value.
	 * Once it is set, it does not change with the next call.
	 * @param event	the previous detection in the chain
	 */
	void setPrevious(IEvent event);

	/**
	 * Returns a pointer to the event detection that is considered the next detection
	 * of the same object.
	 * @return	a pointer to the next detection in the chain.
	 */
	IEvent getNext();

	/**
	 * Sets a pointer to the event detection that is considered the next detection of the
	 * same object. This method only changes the pointer from null to a non-null value.
	 * Once it is set, it does not change with the next call.
	 * @param event	the next detection in the chain
	 */
	void setNext(IEvent event);

	/**
	 * Returns a unique identifier for this object inside this program.  This was
	 * added for use in caching because different event types may contain identifiers
	 * in the ID that overlap from one event type to the next.  This is so we can uniquely 
	 * identify this particular object when caching such things as image parameters.
	 * @return	A unique identifier for this object inside this program.
	 */
	UUID getUUID();
}