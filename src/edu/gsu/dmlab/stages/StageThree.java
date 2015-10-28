package edu.gsu.dmlab.stages;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import edu.gsu.dmlab.stages.interfaces.BaseUpperStage;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import java.awt.geom.Point2D;

/**
 * Created by thad on 9/23/15.
 */
public class StageThree extends BaseUpperStage {

    private int maxFrameSkip;
    public StageThree(ITrackIndexer trackIndexer, IEventIndexer eventIndexer, IPositionPredictor positionPredictor, Configuration configuration, int maxFrameSkip) throws ConfigurationException{
        super(positionPredictor, trackIndexer,  maxFrameSkip);
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
//            #pragma omp section
//            {
                p = this.PMotionModel(leftTrack, rightTrack);
//            }
//        }

        return p;
    }

    double PMotionModel(ITrack leftTrack, ITrack rightTrack) {
        double []leftMotion = trackNormalizedMeanMovement(leftTrack);
        double []rightMotion = trackNormalizedMeanMovement(rightTrack);

        double xdiff = leftMotion[0] - rightMotion[0];
        double ydiff = leftMotion[1] - rightMotion[1];

        double val = (xdiff * xdiff) + (ydiff * ydiff);
        val = Math.sqrt(val);
        double prob = 1.0 - (0.5 * val);
        return prob;
    }

    private double[] trackNormalizedMeanMovement(ITrack track) {
        double xMovement = 0.0;
        double yMovement = 0.0;
        IEvent event = track.getFirst();
        for(IEvent currentEvent: track){
            Point2D.Double currentlocation = currentEvent.getLocation();
            Point2D.Double firstLocation = event.getLocation();
            xMovement += currentlocation.getX() - firstLocation.getX();
            yMovement += currentlocation.getY() - firstLocation.getY();
            event = currentEvent;
        }

        double [] motionNormMean = new double[2];

        if (track.size() > 0) {
            //average the movement
            double xMean = xMovement / track.size();
            double yMean = yMovement / track.size();

            //Now normalize the movement
            double val = (xMean * xMean) + (yMean * yMean);
            val = Math.sqrt(val);

            //Store in array for return
            motionNormMean[0] = xMean / val;
            motionNormMean[1] = yMean / val;
        } else {
            motionNormMean[0] = 0;
            motionNormMean[1] = 0;
        }
        return motionNormMean;
    }
}
