package edu.gsu.dmlab.indexes;

import edu.gsu.dmlab.factory.interfaces.IIndexFactory;
import edu.gsu.dmlab.geometry.GeometryUtilities;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.indexes.interfaces.AbsMatIndexer;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import java.awt.Polygon;
import java.awt.Rectangle;

/**
 * Created by thad on 10/11/15. Edited by Dustin Kempton on 10/28/15
 */
public class BasicEventIndexer extends AbsMatIndexer<IEvent> implements
		IEventIndexer {
	Duration frameSpan;
	Interval globalTimePeriod;
	IIndexFactory factory;

	HashMap<Integer, ArrayList<IEvent>> frames = new HashMap<Integer, ArrayList<IEvent>>();

	public BasicEventIndexer(ArrayList<IEvent> regionalList,
							 int regionDimension, int regionDiv, Duration frameSpan,
							 IIndexFactory factory) throws IllegalArgumentException {

		super(regionalList, regionDimension, regionDiv);

		if (frameSpan == null)
			throw new IllegalArgumentException("FrameSpan cannot be null");
		if (factory == null)
			throw new IllegalArgumentException("IIndexFactory cannot be null");

		if (frameSpan.getStandardSeconds() < 1)
			throw new IllegalArgumentException(
					"FrameSpan cannot be a duration less than 1");

		this.frameSpan = frameSpan;
		this.globalTimePeriod = null; // the build index should
		// expand this as events are
		// indexed.
		this.factory = factory;
		this.buildIndex();
	}

	@Override
	protected void buildIndex() {

		// add all the objects to the index
		objectList.parallelStream()
				.forEach(event -> indexEvent((IEvent) event));

		// Sort all of the array lists in the search area matrix
		RecursiveAction fs = this.factory.getBaseObjectAreaSort(
				this.searchSpace, 0, 0, this.searchSpace.length,
				this.searchSpace.length);
		ForkJoinPool pool = new ForkJoinPool();
		pool.invoke(fs);
	}

	@Override
	public ArrayList<IEvent> search(Interval timePeriod) {
		HashMap<UUID, IEvent> results = new HashMap<>();

		// if the query time actually overlaps then do it
		if (this.globalTimePeriod.overlaps(timePeriod)) {

			Interval intersection = this.globalTimePeriod.overlap(timePeriod);
			ArrayList<Integer> periodIndexes = getFrameIndex(intersection);
			for (Integer index : periodIndexes) {
				for (IEvent event : this.frames.getOrDefault(index,
						new ArrayList<>())) {
					if (event.getTimePeriod().overlaps(intersection)) {
						results.put(event.getUUID(), event);
					}
				}
			}
		}

		// return the values
		Collection<IEvent> coll = results.values();
		ArrayList<IEvent> list = new ArrayList<IEvent>();
		list.addAll(0, coll);
		return (ArrayList<IEvent>) list;
	}

	private void indexEvent(IEvent event) {
		insertEventIntoSearchSpace(event);
		resizeGlobalPeriod(event.getTimePeriod());
		buildFrameIndex(event);
	}

	private void insertEventIntoSearchSpace(IEvent event) {
		Polygon shape = event.getShape();
		Polygon scaledSahpe = GeometryUtilities.scalePolygon(shape,
				regionDivisor);
		Rectangle boundingBox = scaledSahpe.getBounds();
		for (int x = (int) boundingBox.getMinX(); x < boundingBox.getMaxX(); x++) {
			for (int y = (int) boundingBox.getMinY(); y < boundingBox.getMaxY(); y++) {
				if (scaledSahpe.contains(x, y)) {
					searchSpace[x][y].add(event);
				}
			}
		}
	}

	private void buildFrameIndex(IEvent event) {
		Interval timePeriod = event.getTimePeriod();
		this.resizeGlobalPeriod(timePeriod);
		ArrayList<Integer> indexes = getFrameIndex(timePeriod);
		for (int index : indexes) {
			ArrayList<IEvent> frame = frames.getOrDefault(index, null);
			if (frame != null) {
				frame.add(event);
			} else {
				frame = new ArrayList<IEvent>();
				frame.add(event);
				this.frames.put(index, frame);
			}
		}
	}

	private void resizeGlobalPeriod(Interval timePeriod) {

		if (this.globalTimePeriod == null) {
			long length = timePeriod.toDurationMillis()
					/ this.frameSpan.getMillis() + 1;
			DateTime end = timePeriod.getStart().plus(
					this.frameSpan.getMillis() * length);
			this.globalTimePeriod = new Interval(timePeriod.getStart(), end);
		} else {
			this.globalTimePeriod = union(this.globalTimePeriod, timePeriod);
		}
	}

	/***
	 * This method assumes that the input interval is within the global time
	 * period. Make sure to expand the global interval if this is not the case,
	 * or trim the input timePeriod prior to calling.
	 *
	 * @param timePeriod
	 *            the time period we wish to get valid index locations for
	 * @return the set of index locations the input period intersects
	 */
	private ArrayList<Integer> getFrameIndex(Interval timePeriod) {

		// find the beginning index location
		long beginningIndex = (this.globalTimePeriod.getStartMillis() - timePeriod
				.getStartMillis()) / this.frameSpan.getMillis();

		long numIdPositions = (timePeriod.toDurationMillis() / this.frameSpan
				.getMillis());

		if (timePeriod.toDurationMillis() % this.frameSpan.getMillis() != 0) {
			numIdPositions += 1;
		}

		// find the ending index location
		long endingIndex = beginningIndex + numIdPositions;

		// construct the array of indexes to return
		ArrayList<Integer> indexes = new ArrayList<>();
		for (long index = beginningIndex; index <= endingIndex; index++) {
			indexes.add((int) index);
		}
		return indexes;
	}

	private Interval union(Interval firstInterval, Interval secondInterval) {
		// Purpose: Produce a new Interval instance from the outer limits of any
		// pair of Intervals.

		// Take the earliest of both starting date-times.
		DateTime start = firstInterval.getStart().isBefore(
				secondInterval.getStart()) ? firstInterval.getStart()
				: secondInterval.getStart();
		// Take the latest of both ending date-times.
		DateTime end = firstInterval.getEnd().isAfter(secondInterval.getEnd()) ? firstInterval
				.getEnd() : secondInterval.getEnd();
		// Instantiate a new Interval from the pair of DateTime instances.
		Interval unionInterval = new Interval(start, end);

		return unionInterval;
	}

	@Override
	public int getExpectedChangePerFrame(Interval timePeriod) {
		if (timePeriod.getEnd().isBefore(this.globalTimePeriod.getStart())) {
			return 0;
		} else if (timePeriod.getStart()
				.isAfter(this.globalTimePeriod.getEnd())) {
			return 0;
		}
		Interval intersection = this.globalTimePeriod.overlap(timePeriod);
		ArrayList<Integer> periodIndexes = getFrameIndex(intersection);
		double sum = 0.0;
		if (periodIndexes.size() > 1) {
			ArrayList<IEvent> events = this.frames.getOrDefault(0,
					new ArrayList<>());
			double lastValue = events.size();
			for (int i = 0; i < periodIndexes.size(); i++) {
				events = this.frames.getOrDefault(i, new ArrayList<>());
				double currentValue = events.size();
				sum += Math.abs(currentValue - lastValue);
				lastValue = currentValue;
			}
		}
		return (int) sum / periodIndexes.size();
	}

}