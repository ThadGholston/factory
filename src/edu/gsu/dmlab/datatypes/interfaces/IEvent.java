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

import org.joda.time.Interval;
import org.opencv.core.Point;
import org.opencv.core.Rect;

public interface IEvent {

	public int getId();

	public Point getLocation();

	public Rect getBBox();

	public Point[] getShape();

	public Interval getTimePeriod();

	public void updateTimePeriod(Interval period);

	public String getType();

	public IEvent getPrevious();

	public void setPrevious(IEvent event);

	public IEvent getNext();

	public void setNext(IEvent event);
	
	public UUID getUUID();
}
