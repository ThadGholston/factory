package edu.gsu.dmlab.indexes;

import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.factory.interfaces.IIndexFactory;
import edu.gsu.dmlab.geometry.GeometryUtilities;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.indexes.interfaces.AbsMatIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;

import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;
import java.awt.Polygon;
import java.awt.Rectangle;

/**
 * Created by thad on 10/11/15. Modified by Dustin on 11/09/15.
 */
public class BasicTrackIndexer extends AbsMatIndexer<ITrack> implements
		ITrackIndexer {
	IIndexFactory factory;

	public BasicTrackIndexer(ArrayList<ITrack> list, int regionDimension,
			int regionDiv, IIndexFactory factory)
			throws IllegalArgumentException {
		super(list, regionDimension, regionDiv);

		if (factory == null)
			throw new IllegalArgumentException("Factory cannot be null.");
		this.factory = factory;
	}

	protected void buildIndex() {
		objectList.parallelStream().forEach(track -> {
			this.indexTrack(track);
		});

		// Sort all of the array lists in the search area matrix
		RecursiveAction fs = this.factory.getBaseObjectAreaSort(
				this.searchSpace, 0, 0, this.searchSpace.length,
				this.searchSpace.length);
		ForkJoinPool pool = new ForkJoinPool();
		pool.invoke(fs);
	}

	private void indexTrack(ITrack track) {
		if (track.getType() == EventType.SIGMOID) {
			this.pushSGTrackIntoMatrix(track);
		} else {
			this.pushTrackIntoMatrix(track);
		}
	}

	private void pushSGTrackIntoMatrix(ITrack track) {
		Rectangle scaledBoundingBox = GeometryUtilities.scaleBoundingBox(track
				.getFirst().getBBox(), regionDivisor);

		IntStream
				.rangeClosed((int) scaledBoundingBox.getMinX(),
						(int) scaledBoundingBox.getMaxX())
				.parallel()
				.forEach(
						x -> {
							IntStream
									.rangeClosed(
											(int) scaledBoundingBox.getMinY(),
											(int) scaledBoundingBox.getMaxY())
									.parallel().forEach(y -> {
										searchSpace[x][y].add(track);
									});
						});
	}

	private void pushTrackIntoMatrix(ITrack track) {
		Rectangle scaledBoundingBox = GeometryUtilities.scaleBoundingBox(track
				.getFirst().getBBox(), regionDivisor);
		Polygon scaledPoly = GeometryUtilities.scalePolygon(track.getFirst()
				.getShape(), regionDivisor);

		IntStream
				.rangeClosed((int) scaledBoundingBox.getMinX(),
						(int) scaledBoundingBox.getMaxX())
				.parallel()
				.forEach(
						x -> {
							IntStream
									.rangeClosed(
											(int) scaledBoundingBox.getMinY(),
											(int) scaledBoundingBox.getMaxY())
									.parallel().forEach(y -> {
										if (scaledPoly.contains(x, y))
											searchSpace[x][y].add(track);
									});
						});
	}

	@Override
	public ArrayList<ITrack> filterOnInterval(Interval timePeriod) {
		// TODO Auto-generated method stub
		return null;
	}

}
