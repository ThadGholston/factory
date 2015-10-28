/*
 * 
 * File: IImageDBConnection.java is the public interface for image database connectons for any 
 * project that depends on the image/image parameter database created for the Data Mining Lab at
 * Georgia State University
 * 
 * @author Dustin Kempton
 * @version 05/09/2015 
 * @Owner Data Mining Lab, Georgia State University
 * 
 */

package edu.gsu.dmlab.databases.interfaces;

import org.joda.time.Interval;
import org.opencv.core.Mat;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;

import java.sql.SQLException;

public interface IImageDBConnection {
	float[][][] getImageParam(IEvent event, int wavelength,
							  boolean leftSide);

	Mat getFirstImage(Interval period, int wavelength)
			throws SQLException;
}