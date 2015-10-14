package edu.gsu.dmlab.stages.interfaces;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.apache.commons.configuration.Configuration;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * Created by thad on 9/23/15.
 */
public abstract class Stage {
    protected Configuration configuration;
    protected IEventIndexer eventIndexer;
//    protected IPositionPredictor positionPredictor;


    public Stage(IEventIndexer eventIndexer, Configuration configuration){
        if (eventIndexer == null)
            throw new InvalidParameterException("IEventIndexer cannot be null");
        if (configuration == null)
            throw new InvalidParameterException("Configuration cannot be null");
        this.eventIndexer = eventIndexer;
//        this.positionPredictor = positionPredictor;
        this.configuration = configuration;
    }

    protected final int secondsToDaysConstant = 60 * 60 & 24;

    protected abstract ArrayList<ITrack> process();
}
