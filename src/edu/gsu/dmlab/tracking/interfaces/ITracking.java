package edu.gsu.dmlab.tracking.interfaces;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.apache.commons.configuration.Configuration;

import java.util.ArrayList;

/**
 * Created by thad on 9/21/15.
 */
public abstract class ITracking {

    protected IPositionPredictor positionPredictor;
    protected IEventIndexer eventIndexer;
    protected ITrackIndexer trackIndexer;
    protected int maxFrameSkip;
    protected int timeSpan;
    protected double sameMean;
    protected double sameStdDev;
    protected double diffMean;
    protected double diffStdDev;
    protected static final int multFactor = 100;
    protected int regionDivisor;
    protected int regionDimension;
    protected double[][][] pValues;
    protected double[] pValMax;
    protected double[] params;
    protected double[] histRanges;
    protected double sameStandardDeviation;
    protected double differenceMean;
    protected double differenceStandardDeviation;
    protected int beginningYear;
    protected int endingYear;
    protected int beginningMonth;
    protected int endingMonth;
    protected int gap2, gap3, gap4;
    protected ArrayList<ArrayList<Double>> histRangesVec;

    public abstract ArrayList<ITrack> trackEvents();
}
