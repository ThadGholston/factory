package edu.gsu.dmlab.util;

import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;

import org.joda.time.DateTime;

/**
 * Implemented by Dustin 11/11/2015.
 */
public class Utility {

	public static final double SECONDS_TO_DAYS = 60.0 * 60.0 * 24.0;

	public static float[] trackMovement(ITrack track) {
		double xMovement = 0.0;
		double yMovement = 0.0;
		double totalTime = 0.0;
		int count = 0;

		IEvent event = track.getFirst();

		float[] motionNormMean = new float[2];

		for (IEvent currentEvent : track) {
			Point2D locationTwo = currentEvent.getLocation();
			Point2D locationOne = event.getLocation();
			xMovement += locationOne.getX() - locationTwo.getX();
			yMovement += locationOne.getY() - locationTwo.getY();

			double span;
			DateTime startSearch;
			DateTime endSearch;

			// Interval timePeriod = event.getTimePeriod();
			startSearch = currentEvent.getTimePeriod().getEnd();
			endSearch = startSearch.plus(currentEvent.getTimePeriod()
					.getStartMillis() - event.getTimePeriod().getStartMillis());
			span = ((endSearch.minus(startSearch.getMillis())).getMillis() / 1000)
					/ SECONDS_TO_DAYS;

			totalTime += span;
			event = currentEvent;
		}

		if (track.size() > 0) {
			double xMean = xMovement / count;
			double yMean = yMovement / count;
			double tMean = totalTime / count;
			float xMeanPerTime = (float) (xMean / tMean);
			float yMeanPerTime = (float) (yMean / tMean);

			motionNormMean[0] = xMeanPerTime;
			motionNormMean[1] = yMeanPerTime;
		} else {
			motionNormMean[0] = 0;
			motionNormMean[1] = 0;
		}
		return motionNormMean;
	}

}
