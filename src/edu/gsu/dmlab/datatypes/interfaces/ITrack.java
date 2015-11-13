/**
 * File: ITrack.java is the public interface for event tracks processed by tracking algorithms
 * implemented by the Data Mining Lab at Georgia State University
 *
 * @author Dustin Kempton
 * @version 05/12/2015
 * @Owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab.datatypes.interfaces;

import edu.gsu.dmlab.datatypes.EventType;

import java.io.Serializable;
import java.util.ArrayList;

public interface ITrack extends IBaseDataType, Serializable {

	/**
	 * Returns the start time in milliseconds of the first event in the track
	 * 
	 * @return
	 */
	long getStartTimeMillis();

	/**
	 * Returns the end time in milliseconds of the last event in the track
	 * 
	 * @return
	 */
	long getEndTimeMillis();

	/**
	 * Returns an array that contains all of the events currently in the track
	 * 
	 * @return array of all the events currently in the track
	 */
	ArrayList<IEvent> getEvents();
	
	int size();

	/**
	 * Returns the first event in the track
	 * 
	 * @return the first event in the track
	 */
	IEvent getFirst();

	/**
	 * Returns the last event in the track
	 * 
	 * @return the last event in the track
	 */
	IEvent getLast();

	/**
	 * Returns the event type of the track
	 * 
	 * @return the event type of the track
	 */
	public EventType getType();

}