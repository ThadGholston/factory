/**
 * File StageTwo.java is the second stage in the iterative tracking 
 * algorithm of Kempton et al.,  http://dx.doi.org/10.1016/j.ascom.2015.10.005.
 * 
 * 
 * @author Thaddeus Gholston : Major Revisions by Dustin Kempton
 * @version 11/12/15
 * @owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab.stages.interfaces;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.factory.interfaces.IGraphProblemFactory;
import edu.gsu.dmlab.graph.Graph;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import edu.gsu.dmlab.util.Utility;
import edu.gsu.dmlab.util.interfaces.ISearchAreaProducer;

import org.joda.time.Interval;
import org.joda.time.Seconds;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public abstract class BaseUpperStage implements IProcessingStage {

	ITrackIndexer tracksIdxr;
	ISearchAreaProducer predictor;

	IGraphProblemFactory graphFactory;

	protected int maxFrameSkip;
	protected static final int multFactor = 100;

	protected int countX;

	public BaseUpperStage(ISearchAreaProducer predictor,
			IGraphProblemFactory graphFactory, ITrackIndexer tracksIdxr,
			int maxFrameSkip) {

		if (predictor == null)
			throw new IllegalArgumentException(
					"Search Area Producer cannot be null.");
		if (graphFactory == null)
			throw new IllegalArgumentException(
					"Graph Problem Factory cannot be null.");

		this.predictor = predictor;
		this.tracksIdxr = tracksIdxr;
		this.maxFrameSkip = maxFrameSkip;

		this.countX = 0;
	}

	@Override
	public ArrayList<ITrack> process() {

		ArrayList<ITrack> vectOfTracks = this.tracksIdxr.getAll();

		if (vectOfTracks.size() > 0) {
			Graph graph = this.graphFactory.getGraph(vectOfTracks);

			// for each track that we got back in the list we will find the
			// potential
			// matches for that track and link it to the one with the highest
			// probability of
			// being a match.
			for (int i = 0; i < vectOfTracks.size(); i++) {

				// we get the event associated with the end of our current track
				// as that is
				// the last frame of the track and has position information and
				// image
				// information associated with it.
				ITrack currentTrack = vectOfTracks.get(i);
				IEvent currentEvent = currentTrack.getLast();

				double span;
				Polygon searchArea;
				HashMap<UUID, ITrack> potentialMap = new HashMap<UUID, ITrack>();

				// get the search area to find tracks that may belong linked
				// to the current one being processed
				span = Seconds.secondsIn(currentEvent.getTimePeriod())
						.getSeconds() / Utility.SECONDS_TO_DAYS;
				// set the time span to search in as the end of our current
				// track+the
				// the span of the frame for the last event in our current
				// track being processed.
				Interval currentSearchTime = new Interval(currentEvent
						.getTimePeriod().getEnd(), currentEvent.getTimePeriod()
						.toDuration());

				float[] motionVect = null;
				if (currentTrack.size() >= 2) {
					searchArea = this.predictor.getSearchRegion(
							currentEvent.getBBox(), span);
<<<<<<< Updated upstream
=======

					potentialTracks = this.tracksIdxr
							.search(new Interval(
									startSearch, endSearch), searchArea);

>>>>>>> Stashed changes
				} else {
					motionVect = Utility.trackMovement(currentTrack);
					searchArea = this.predictor.getSearchRegion(
							currentEvent.getBBox(), motionVect, span);
<<<<<<< Updated upstream
=======

					potentialTracks = this.tracksIdxr
							.search(new Interval(
									startSearch, endSearch), searchArea);
>>>>>>> Stashed changes
				}
				ArrayList<ITrack> potentialTracks = this.tracksIdxr
						.filterOnIntervalAndLocation(currentSearchTime,
								searchArea);

				for (ITrack trk : potentialTracks) {
					potentialMap.put(trk.getUUID(), trk);
				}
				// search locations for potential matches up to the maxFrameSkip
				// away
				// using the previously predicted search area as the starting
				// point
				// for the next search area.

				for (int j = 0; j < maxFrameSkip; j++) {

					// update the currentSearchTime to the next frame
					currentSearchTime = new Interval(
							currentSearchTime.getEnd(),
							currentSearchTime.toDuration());

					// if we don't have a motion vector then use diff rotation

					if (motionVect == null) {
						// update search area for next frame
						Rectangle rect = searchArea.getBounds();
						searchArea = this.predictor.getSearchRegion(rect, span);

<<<<<<< Updated upstream
						// search next frame
						potentialTracks = this.tracksIdxr
								.filterOnIntervalAndLocation(currentSearchTime,
										searchArea);

=======
						potentialTracks2 = this.tracksIdxr
								.search(new Interval(
										startSearch, endSearch), searchArea);
						startSearch = endSearch;
>>>>>>> Stashed changes
					} else {
						// update search area for next frame using motion vector
						Rectangle rect = searchArea.getBounds();
						searchArea = this.predictor.getSearchRegion(rect,
								motionVect, span);

<<<<<<< Updated upstream
						// search next frame
						potentialTracks = this.tracksIdxr
								.filterOnIntervalAndLocation(currentSearchTime,
										searchArea);
=======
						potentialTracks2 = this.tracksIdxr
								.search(new Interval(
										startSearch, endSearch), searchArea);
						startSearch = endSearch;
>>>>>>> Stashed changes
					}

					// put potential matches not in potentialTracks list into
					// the list
					for (ITrack trk : potentialTracks) {
						if (!potentialMap.containsKey(trk.getUUID())) {
							potentialMap.put(trk.getUUID(), trk);
						}
					}

				}

				// if the map of potential matches has anything in it,
				// then we will process those tracks by adding them to the graph
				// problem
				if (potentialMap.size() >= 1) {
					for (ITrack tmpTrk : potentialMap.values()) {
						this.addEdgeBetweenTracks(currentTrack, tmpTrk, graph);
					}
				}
			}

			// if the graph problem was solved then return the results
			if (graph.solve()) {
				return graph.getTrackLinked();
			}
		}
		// if solve didn't work then just return the original list.
		return this.tracksIdxr.getAll();
	}

	void addEdgeBetweenTracks(ITrack track1, ITrack track2, Graph graph) {
		double prob = this.prob(track1, track2);
		int weightVal = -(int) (Math.log(prob) * multFactor);
		graph.addEdge(track1, track2, weightVal);
	}

	protected abstract double prob(ITrack leftTrack, ITrack rightTrack);

}