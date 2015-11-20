package edu.gsu.dmlab.tracking;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.tracking.interfaces.IObsModel;

public class PoissonObsModel implements IObsModel {

	IEventIndexer evntsIdxr;
	int timeSpan;
	int numSpan;

	public PoissonObsModel(IEventIndexer evntsIdxr, int timeSpan, int numSpan) {
		if (evntsIdxr == null)
			throw new IllegalArgumentException("Event Indexer cannot be null.");
		this.evntsIdxr = evntsIdxr;
		this.timeSpan = timeSpan;
		this.numSpan = numSpan;
	}

	@Override
	public double getObsProb(IEvent ev) {
		return this.getPoissonProb(ev);
	}

	public double getPoissonProb(IEvent event) {
		DateTime start = this.evntsIdxr.getFirstTime();
		DateTime end = start.plusSeconds(this.timeSpan * this.numSpan);
		Interval timePeriod = new Interval(start, end);
		int lambda = 0;
		int delta = 0;
		if (event.getTimePeriod().overlaps(timePeriod)) {
			end = start.plus(this.timeSpan * this.numSpan);
			Interval range = new Interval(start, end);
			lambda = evntsIdxr.getExpectedChangePerFrame(range);
			range = event.getTimePeriod();
			range = new Interval(range.getStart(), range.getEnd().plusSeconds(
					this.timeSpan * this.numSpan));
			delta = evntsIdxr.getExpectedChangePerFrame(range);
		} else {
			end = this.evntsIdxr.getLastTime();
			start = end.minusSeconds(this.timeSpan * this.numSpan);
			Interval range = new Interval(start, end);
			lambda = evntsIdxr.getExpectedChangePerFrame(range);
			range = event.getTimePeriod();
			range = new Interval(range.getStart().minusSeconds(
					this.timeSpan * this.numSpan), range.getEnd());
			delta = evntsIdxr.getExpectedChangePerFrame(range);
		}
		if (lambda > 0) {
			double lamPow = Math.pow(lambda, delta);
			double lamExp = Math.exp(-lambda);
			double fact = CombinatoricsUtils.factorialDouble(delta);
			return (lamPow * lamExp) / fact;
		} else if (lambda == 0 && delta < 2) {
			return 0.0000001;
		} else {
			return .98;
		}
	}

}
