package edu.gsu.dmlab.tracking.interfaces;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.apache.commons.configuration.Configuration;

import java.util.ArrayList;

/**
 * Created by thad on 9/21/15.
 */
public abstract class ITracking {

    protected Configuration configuration;
    protected double sameMean;
    protected double sameStandardDeviation;
    protected double differenceMean;
    protected double differenceStandardDeviation;
    protected int beginningYear;
    protected int endingYear;
    protected int beginningMonth;
    protected int endingMonth;
    protected int timeSpan;
    protected int gap2, gap3, gap4;
    protected ArrayList<ArrayList<Double>> histRangesVec;
    protected ArrayList<Integer> params;

    public ITracking (Configuration config){
        this.configuration = config;
    }

    public abstract ArrayList<ITrack> trackEvents();
}
