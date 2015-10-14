package edu.gsu.dmlab.indexes.interfaces;

import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * Created by thad on 9/19/15.
 */
public interface ITrackIndexer extends IIndexer{
    ArrayList<ITrack> getTracksStartBetween(DateTime begin, DateTime end, Point2D[] searchArea);
    ArrayList<ITrack> getTracksEndBetween(DateTime begin, DateTime end, Point2D[] searchArea);
    ArrayList<ITrack> getAll();
};
