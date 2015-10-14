package edu.gsu.dmlab.tracking;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import edu.gsu.dmlab.stages.StageOne;
import edu.gsu.dmlab.stages.StageThree;
import edu.gsu.dmlab.stages.StageTwo;
import edu.gsu.dmlab.tracking.interfaces.ITracking;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.apache.commons.configuration.Configuration;

import java.util.ArrayList;

/**
 * Created by thad on 9/21/15.
 */
public class IterativeTracking extends ITracking {

    IEventIndexer eventIndexer;
    ITrackIndexer trackIndexer;
    IPositionPredictor positionPredictor;
    int maxFrameSkip;

    public IterativeTracking(Configuration configuration, ITrackIndexer trackIndexer, IEventIndexer eventIndexer, IPositionPredictor positionPredictor, int maxFrameSkip) {
        super(configuration);
        this.eventIndexer = eventIndexer;
        this.trackIndexer = trackIndexer;
        this.positionPredictor = positionPredictor;
        this.maxFrameSkip = maxFrameSkip;
    }

    @Override
    public ArrayList<ITrack> trackEvents() {

        //Process stage one
        StageOne stageOne = new StageOne(eventIndexer, configuration);
        ArrayList<ITrack> tracks = stageOne.process();

        //Process stage two
        StageTwo stageTwo = new StageTwo(trackIndexer, eventIndexer, positionPredictor, configuration, maxFrameSkip);
        tracks = stageTwo.process();

        //Process stage three
        StageThree twoPartStage = new StageThree(trackIndexer, eventIndexer, positionPredictor, configuration, maxFrameSkip);
        tracks = twoPartStage.process();

        //Process stage four
        twoPartStage = new StageThree(trackIndexer, eventIndexer, positionPredictor, configuration, maxFrameSkip);
        tracks = twoPartStage.process();

        return tracks;
    }
}
