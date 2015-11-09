package edu.gsu.dmlab.stages;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import edu.gsu.dmlab.stages.interfaces.BaseUpperStage;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.apache.commons.configuration.Configuration;

/**
 * Created by thad on 9/23/15.
 */
public class StageTwo extends BaseUpperStage {

    private int maxFrameSkip;

    ITrackIndexer trackIndexer;

    public StageTwo(IPositionPredictor predictor, IEventIndexer evntsIdxr, ITrackIndexer tracksIdxr,
                    int timeSpan, int numSpan, int maxFrameSkip, double sameMean, double sameStdDev, double diffMean,
                    double diffStdDev, double[] histRanges, double[] params, double[][][] pValues) {
        super(predictor, evntsIdxr, tracksIdxr,
                timeSpan, numSpan, maxFrameSkip, sameMean, sameStdDev, diffMean,
                diffStdDev, histRanges, params, pValues);
    }


    protected double prob(ITrack leftTrack, ITrack rightTrack) {
        double p = 1;
        p *= this.PAppearance(leftTrack, rightTrack);
        p *= this.PFrameGap(leftTrack, rightTrack);
        return p;
    }

}
