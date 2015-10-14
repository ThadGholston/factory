/**
 * File:Object.java Provides the interface to create objects used by various programs
 * wishing to use events and tracks and such, I don't know this is still in dev and will
 * most likely change a lot.  
 *   
 * @author Dustin Kempton
 * @version 05/14/2015 
 * @Owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab;

import java.io.IOException;
import java.util.ArrayList;

import javax.sql.DataSource;

import edu.gsu.dmlab.databases.ImageDBConnection;
import edu.gsu.dmlab.databases.TrackingResultFileReader;
import edu.gsu.dmlab.databases.interfaces.IImageDBConnection;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.IRegion;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.imageproc.HistogramProducer;
import edu.gsu.dmlab.imageproc.interfaces.IHistogramProducer;
import edu.gsu.dmlab.indexes.MatrixBasedEventIndexer;
import edu.gsu.dmlab.indexes.MatrixBasedTrackIndexer;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import edu.gsu.dmlab.util.TrapezoidPositionPredictor;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.apache.commons.configuration.Configuration;

public class ObjectFactory {
	public static IImageDBConnection getImageDBConnection(DataSource dsourc,
			int maxCacheSize) {
		IImageDBConnection dbConn = new ImageDBConnection(dsourc, maxCacheSize);
		return dbConn;
	}

	public static IHistogramProducer getHistoProducer(
			IImageDBConnection imageDB, int[] wavelengths) {
		return new HistogramProducer(imageDB, wavelengths);
	}

	public static ITrack[] getTrackedResults(String type, String fileLocation,
			int span) {
		TrackingResultFileReader reader = new TrackingResultFileReader(
				fileLocation, span);

		try {
			return reader.getTracks(type);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static IPositionPredictor getPositionPredictor(int typeId) {
		return new TrapezoidPositionPredictor();
	}

	public static IEventIndexer GetBasicEventIndexer(Configuration configuration, ArrayList<IEvent> events, IRegion region){
		return new MatrixBasedEventIndexer(configuration, events, region);
	}

	public static ITrackIndexer GetBasicTrackIndexer(int regionalDimension, int regionalDivisor, ArrayList<ITrack> tracks, IRegion regionalTracksStart, IRegion regionalTracksEnd){
		return new MatrixBasedTrackIndexer(regionalDimension, regionalDivisor, tracks, regionalTracksStart, regionalTracksEnd);
	}

}
