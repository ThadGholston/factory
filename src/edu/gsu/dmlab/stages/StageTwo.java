package edu.gsu.dmlab.stages;

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

    public StageTwo(ITrackIndexer trackIndexer, IEventIndexer eventIndexer, IPositionPredictor positionPredictor, Configuration configuration, int maxFrameSkip) {
        super(trackIndexer, positionPredictor, configuration, maxFrameSkip);
    }


    double prob(ITrack leftTrack, ITrack rightTrack) {
        double p = 1;
//        #pragma omp parallel sections reduction(*:p)
//        {
//            #pragma omp section
//            {
        p = this.PAppearance(leftTrack, rightTrack);
//            }
//            #pragma omp section
//            {
        p = this.PFrameGap(leftTrack, rightTrack);
//            }
//        }

        return p;
    }


    public int getMaxFrameSkip() {
        return maxFrameSkip;
    }
}
