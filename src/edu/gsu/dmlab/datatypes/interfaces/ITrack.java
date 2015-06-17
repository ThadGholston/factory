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

public interface ITrack {
	public long getStartTimeMillis();

	public long getEndTimeMillis();

	IEvent[] getEvents();

	IEvent getFirst();

	IEvent getLast();
}
