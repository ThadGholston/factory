/**
 * File StageTwo.java is the second stage in the iterative tracking 
 * algorithm of Kempton et al.,  http://dx.doi.org/10.1016/j.ascom.2015.10.005.
 * 
 * 
 * @author Thaddeus Gholston
 * @version 09/23/15
 * @owner Data Mining Lab, Georgia State University
 */

package edu.gsu.dmlab.stages;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.factory.interfaces.IGraphProblemFactory;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import edu.gsu.dmlab.stages.interfaces.BaseUpperStage;
import edu.gsu.dmlab.tracking.interfaces.IAppearanceModel;
import edu.gsu.dmlab.tracking.interfaces.IFrameSkipModel;
import edu.gsu.dmlab.util.interfaces.ISearchAreaProducer;

public class StageTwo extends BaseUpperStage {

	IAppearanceModel appearanceModel;
	IFrameSkipModel skipModel;

	public StageTwo(ISearchAreaProducer predictor, ITrackIndexer tracksIdxr,
			IAppearanceModel appearanceModel, IFrameSkipModel skipModel,
			IGraphProblemFactory graphFactory, int maxFrameSkip) {

		super(predictor, graphFactory, tracksIdxr, maxFrameSkip);

		if (appearanceModel == null)
			throw new IllegalArgumentException(
					"Appearance Model cannot be null.");
		if (skipModel == null)
			throw new IllegalArgumentException(
					"Frame Skip Model cannot be null.");

		this.appearanceModel = appearanceModel;
		this.skipModel = skipModel;
	}

	@Override
	protected double prob(ITrack leftTrack, ITrack rightTrack) {
		double p = 1;
		p *= this.appearanceModel.calcProbAppearance(leftTrack, rightTrack);
		p *= this.skipModel.getSkipProb(leftTrack, rightTrack);
		return p;
	}

}
