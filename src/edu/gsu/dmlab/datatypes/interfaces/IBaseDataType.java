/**
 * 
 * File: IBaseDataType.java is the base data type for both Events and Tracks.
 * So that we can index with the same index object.
 * by the Data Mining Lab at Georgia State University.  
 * 
 * @author Dustin Kempton
 * @version 07/23/2015 
 * @Owner Data Mining Lab, Georgia State University
 * 
 */
package edu.gsu.dmlab.datatypes.interfaces;

import org.joda.time.Interval;

import java.util.Comparator;
import java.util.UUID;

public interface IBaseDataType {

	/**
	 * Returns the time period that the object is valid for
	 * @return
	 */
	Interval getTimePeriod();

	int compareTime(IBaseDataType baseDataType);

	boolean intersects(Interval interval);

	boolean isBefore(Interval timePeriod);

	boolean isBefore(IBaseDataType obj);

	boolean isAfter(Interval timePeriod);

	boolean isAfter(IBaseDataType obj);

	UUID getUUID();
	
	public static Comparator<IBaseDataType> baseComparator = new Comparator<IBaseDataType>(){

		@Override
		public int compare(IBaseDataType o1, IBaseDataType o2) {
			return o1.compareTime(o2);
		}
		
	};

}
