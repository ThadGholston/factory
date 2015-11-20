package edu.gsu.dmlab.tracking;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import edu.gsu.dmlab.stages.StageOne;
import edu.gsu.dmlab.stages.StageThree;
import edu.gsu.dmlab.stages.StageTwo;
import edu.gsu.dmlab.tracking.interfaces.ITracking;
import org.apache.commons.configuration.Configuration;

import java.util.ArrayList;

/**
 * Created by thad on 9/21/15.
 */
public class IterativeTracking extends ITracking {
    @Override
    public ArrayList<ITrack> trackEvents() {
        return null;
    }


//    public IterativeTracking(IPositionPredictor positionPredictor, IEventIndexer eventIndexer, ITrackIndexer trackIndexer,
//                             int timeSpan, double sameMean, double sameStdDev, double diffMean, double diffStdDev,
//                             double[] histRanges, double[] params) {
//    }
//
//    @Override
//    public ArrayList<ITrack> trackEvents() {
//
//        //Process stage one
//        StageOne stageOne = new StageOne(positionPredictor, eventIndexer);
//        ArrayList<ITrack> tracks = stageOne.process();
//
//        //Process stage two
//        StageTwo stageTwo = new StageTwo(positionPredictor, eventIndexer, trackIndexer, timeSpan, sameMean, sameStdDev, diffMean, diffStdDev, histRanges, params);
//        tracks = stageTwo.process();
//
//        //Process stage three
//        StageThree twoPartStage = new StageThree(positionPredictor, eventIndexer, trackIndexer, timeSpan, sameMean, sameStdDev, diffMean, diffStdDev, histRanges, params);
//        tracks = twoPartStage.process();
//
//        //Process stage four
//        twoPartStage = new StageThree(positionPredictor, eventIndexer, trackIndexer, timeSpan, sameMean, sameStdDev, diffMean, diffStdDev, histRanges, params);
//        tracks = twoPartStage.process();
//
//        return tracks;
//    }
}
