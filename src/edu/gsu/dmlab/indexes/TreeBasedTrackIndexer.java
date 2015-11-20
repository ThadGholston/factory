package edu.gsu.dmlab.indexes;

import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.geometry.GeometryUtilities;
import edu.gsu.dmlab.indexes.datastructures.GITree;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thad on 11/14/15.
 */
public class TreeBasedTrackIndexer implements ITrackIndexer {

    private DateTime firstTime;
    private DateTime lastTime;
    private final int MAX = 4096;
    private final int INCREMENT = 16;
    private int regionDivisor;
    private int regionDimension;
    private GITree<ITrack> tree;
    private ArrayList<ITrack> regionalList;

    public TreeBasedTrackIndexer(ArrayList<ITrack> regionalList, int regionDimension, int regionDivisor) throws IllegalArgumentException {
        if (regionalList == null) {
            throw new IllegalArgumentException();
        }
        if (regionDimension < 1) {
            throw new IllegalArgumentException("region dimension cannot be less than one.");
        }
        if (regionDivisor < 1) {
            throw new IllegalArgumentException("region divisor cannot be less than one.");
        }

        this.regionDimension = regionDimension;
        this.regionDivisor = regionDivisor;
        this.regionalList = regionalList;
        this.tree = new GITree<>(4096, 4096);

        for (ITrack track : regionalList) {
            if (track.getType().equals(EventType.SIGMOID)) {
                pushSGTrackIntoMatrix(track);
            } else {
                pushTrackIntoMatrix(track);
            }
        }
    }

    private void pushSGTrackIntoMatrix(ITrack track) {
        Rectangle scaledBoundingBox = GeometryUtilities.scaleBoundingBox(track.getFirst().getBBox(),
                this.regionDivisor);
        tree.insert(track, track.getTimePeriod().getStartMillis(), track.getTimePeriod().getEndMillis(), scaledBoundingBox);
    }

    private void pushTrackIntoMatrix(ITrack track) {
        Rectangle scaledBoundingBox = GeometryUtilities.scaleBoundingBox(track.getFirst().getBBox(),
                this.regionDivisor);
        tree.insert(track, track.getTimePeriod().getStartMillis(), track.getTimePeriod().getEndMillis(), scaledBoundingBox);
    }

    @Override
    public DateTime getFirstTime() {
        return firstTime;
    }

    @Override
    public DateTime getLastTime() {
        return lastTime;
    }

    @Override
    public ArrayList<ITrack> search(Interval timePeriod) {
        ArrayList<ITrack> tracks = tree.search(timePeriod.getStart(), timePeriod.getEnd());
        Map<UUID, ITrack> map = new ConcurrentHashMap<>();
        tracks.parallelStream().forEach(trk -> {
            map.put(trk.getUUID(), trk);
        });
        ArrayList<ITrack> retList =new ArrayList<>();
        retList.addAll(map.values());
        return retList;
    }

    @Override
    public ArrayList<ITrack> search(Interval timePeriod, Polygon searchArea) {
        Polygon scaledSearchArea = GeometryUtilities.scalePolygon(searchArea,
                this.regionDivisor);
        Rectangle searchBoundingBox = scaledSearchArea.getBounds();
        ArrayList<ITrack> tracks = tree.search(timePeriod.getStart(), timePeriod.getEnd(), searchBoundingBox);
        Map<UUID, ITrack> map = new HashMap<>();
        for (ITrack trk : tracks) {
            map.put(trk.getUUID(), trk);
        }
        ArrayList<ITrack> retList =new ArrayList<>();
        retList.addAll(map.values());
        return retList;
    }

    @Override
    public ArrayList<ITrack> getAll() {
        return regionalList;
    }
}
