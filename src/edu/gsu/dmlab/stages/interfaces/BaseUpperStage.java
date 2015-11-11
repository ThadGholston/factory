/**
 * File StageTwo.java is the second stage in the iterative tracking 
 * algorithm of Kempton et al.,  http://dx.doi.org/10.1016/j.ascom.2015.10.005.
 * 
 * 
 * @author Thaddeus Gholston
 * @version 09/23/15
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

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Seconds;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;

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
				DateTime startSearch;
				DateTime endSearch;
				Polygon searchArea;
				ArrayList<ITrack> potentialTracks;

				int positionOfEvent = currentTrack.indexOf(currentEvent);
				if (getIndex(currentTrack, positionOfEvent - 1) == null
						|| getIndex(currentTrack, positionOfEvent - 2) == null) {
					// get the search area to find tracks that may belong linked
					// to the current one being processed
					span = Seconds.secondsIn(currentEvent.getTimePeriod())
							.getSeconds() / Utility.SECONDS_TO_DAYS;
					// set the time span to search in as the end of our current
					// track+the
					// the span of the frame for the last event in our current
					// track being processed.
					startSearch = currentEvent.getTimePeriod().getEnd();
					endSearch = startSearch.plusSeconds(Seconds.secondsIn(
							currentEvent.getTimePeriod()).getSeconds());
					searchArea = this.predictor.getSearchRegion(
							currentEvent.getBBox(), span);

					potentialTracks = this.tracksIdxr
							.filterOnIntervalAndLocation(new Interval(
									startSearch, endSearch), searchArea);

				} else {

					Interval tp = getIndex(currentTrack, positionOfEvent - 1)
							.getTimePeriod();
					startSearch = currentEvent.getTimePeriod().getEnd();
					endSearch = startSearch.plusSeconds(Seconds.secondsBetween(
							currentEvent.getTimePeriod().getStart(),
							tp.getStart()).getSeconds());
					span = Seconds.secondsBetween(endSearch, startSearch)
							.getSeconds() / Utility.SECONDS_TO_DAYS;

					float[] motionVect = Utility.trackMovement(currentTrack);
					searchArea = this.predictor.getSearchRegion(
							currentEvent.getBBox(), motionVect, span);

					potentialTracks = this.tracksIdxr
							.filterOnIntervalAndLocation(new Interval(
									startSearch, endSearch), searchArea);
				}

				// search locations for potential matches up to the maxFrameSkip
				// away
				// using the previously predicted search area as the starting
				// point
				// for the next search area.
				startSearch = currentEvent.getTimePeriod().getEnd();
				for (int j = 0; j < maxFrameSkip; j++) {
					ArrayList<ITrack> potentialTracks2;
					if (getIndex(currentTrack, positionOfEvent - 1) == null
							|| getIndex(currentTrack, positionOfEvent - 2) == null) {
						// get the search area to find tracks that may belong
						// linked to the current one being processed
						span = Seconds.secondsIn(currentEvent.getTimePeriod())
								.getSeconds() / Utility.SECONDS_TO_DAYS;
						// set the time span to search in as the end of our
						// current track+the
						// the span of the frame for the last event in our
						// current track being processed.
						endSearch = startSearch.plusSeconds(Seconds.secondsIn(
								currentEvent.getTimePeriod()).getSeconds());
						Rectangle rect = searchArea.getBounds();
						searchArea = this.predictor.getSearchRegion(rect, span);

						potentialTracks2 = this.tracksIdxr
								.filterOnIntervalAndLocation(new Interval(
										startSearch, endSearch), searchArea);
						startSearch = endSearch;
					} else {
						Interval tp = getIndex(currentTrack,
								positionOfEvent - 1).getTimePeriod();
						span = currentEvent.getTimePeriod().toDuration()
								.toStandardSeconds().getSeconds()
								/ Utility.SECONDS_TO_DAYS;
						// startSearch = currentEvent.getTimePeriod().end();
						endSearch = startSearch
								.plusSeconds(Seconds
										.secondsBetween(
												currentEvent.getTimePeriod()
														.getStart(),
												tp.getStart()).getSeconds());

						float[] motionVect = Utility
								.trackMovement(currentTrack);
						Rectangle rect = searchArea.getBounds();
						searchArea = this.predictor.getSearchRegion(rect,
								motionVect, span);

						potentialTracks2 = this.tracksIdxr
								.filterOnIntervalAndLocation(new Interval(
										startSearch, endSearch), searchArea);
						startSearch = endSearch;
					}

					// put potential matches not in potentialTracks list into
					// the list
					while (!potentialTracks2.isEmpty()) {

						ITrack possibleTrackFromSkippedFrames = potentialTracks2
								.remove(potentialTracks2.size() - 1);
						boolean isInList = false;
						// cout << "Search for potential2 in potential" << endl;
						for (ITrack trackInList : potentialTracks) {

							if (possibleTrackFromSkippedFrames.getFirst()
									.getId() == trackInList.getFirst().getId()) {
								isInList = true;
								break;
							}
						}
						// cout << "done search" << endl;
						if (!isInList) {
							potentialTracks.add(possibleTrackFromSkippedFrames);
						}
					}

				}

				// if the list of potential matches has anything in it,
				// then we will process those tracks.
				if (potentialTracks.size() >= 1) {
					for (ITrack tmpTrk : potentialTracks) {
						this.addEdgeBetweenTracks(currentTrack, tmpTrk, graph);
					}
				}
			}

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

	protected IEvent getIndex(ITrack track, int position) {
		try {
			return track.get(position);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
}