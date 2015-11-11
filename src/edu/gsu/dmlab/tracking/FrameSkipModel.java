package edu.gsu.dmlab.tracking;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.tracking.interfaces.IFrameSkipModel;
import edu.gsu.dmlab.tracking.interfaces.ILocationProbCal;

public class FrameSkipModel implements IFrameSkipModel {

	ILocationProbCal exitProbCalculator;

	public FrameSkipModel(ILocationProbCal exitProbCalculator) {
		if (exitProbCalculator == null)
			throw new IllegalArgumentException(
					"Exit Prob Calculator cannot be null.");

		this.exitProbCalculator = exitProbCalculator;
	}

	@Override
	public double getSkipProb(ITrack leftTrack, ITrack rightTrack) {

		DateTime leftTime = leftTrack.getLast().getTimePeriod().getEnd();
		DateTime rightTime = rightTrack.getFirst().getTimePeriod().getStart();
		Interval timePeriod = new Interval(rightTime, leftTime);
		int span = (int) (leftTrack.getLast().getTimePeriod()
				.toDurationMillis() / 1000);
		int frameSkip = (int) (timePeriod.toDurationMillis() / 1000) / span;

		if (frameSkip > 0) {
			double pExitVal = exitProbCalculator.calcProb(leftTrack.getLast());
			double pFalseNeg = 1 - (pExitVal);
			for (int i = 0; i < frameSkip; i++) {
				pFalseNeg *= pFalseNeg;
			}
			return pFalseNeg;
		} else {

			return 1.0;
		}
	}

}
