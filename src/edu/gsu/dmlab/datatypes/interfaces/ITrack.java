/**
 * File: ITrack.java is the public interface for event tracks processed by tracking algorithms
 * implemented by the Data Mining Lab at Georgia State University
 * 
 * @author Dustin Kempton
 * @version 05/12/2015 
 * @Owner Data Mining Lab, Georgia State University
 *
 */
package edu.gsu.dmlab.datatypes.interfaces;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ListIterator;
import java.util.UUID;

public interface ITrack extends IBaseDataType{

	/**
	 * Returns the start time in milliseconds of the first event in the track
	 * @return
	 */
	public long getStartTimeMillis();

	/**
	 * Returns the end time in milliseconds of the last event in the track
	 * @return
	 */
	public long getEndTimeMillis();

	/**
	 * Returns an array that contains all of the events currently in the track
	 * @return	array of all the events currently in the track
	 */
	public IEvent[] getEvents();

	/**
	 * Returns the first event in the track
	 * @return	the first event in the track
	 */
	public IEvent getFirst();

	/**
	 * Returns the last event in the track
	 * @return	the last event in the track
	 */
	public IEvent getLast();
}