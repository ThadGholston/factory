package edu.gsu.dmlab.indexes;

import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.factory.interfaces.IIndexFactory;
import edu.gsu.dmlab.geometry.GeometryUtilities;
import edu.gsu.dmlab.geometry.Rectangle2D;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.indexes.interfaces.AbsMatIndexer;
import edu.gsu.dmlab.indexes.interfaces.IIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;

import org.apache.commons.configuration.ConfigurationException;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Created by thad on 10/11/15.
 */
public class BasicTrackIndexer extends AbsMatIndexer<ITrack> implements
        ITrackIndexer {
    IIndexFactory factory;

    public BasicTrackIndexer(ArrayList<ITrack> list, int regionDimension,
                             int regionDiv, IIndexFactory factory)
            throws IllegalArgumentException {
        super(list, regionDimension, regionDiv);

        if (factory == null)
            throw new IllegalArgumentException("Factory cannot be null.");
        this.factory = factory;
    }

    protected void buildIndex() {
        objectList.parallelStream().forEach(track -> {
            indexTrack(track);
        });
    }

    private void indexTrack(ITrack track) {
        if (track.getType() == EventType.SIGMOID) {
            pushSGTrackIntoMatrix(track);
        } else {
            pushTrackIntoMatrix(track);
        }
    }

    private void pushSGTrackIntoMatrix(ITrack track) {
        Rectangle2D boxFromFirstEvent = track.getFirst().getBBox();
//		Rectangle2D beginSearchBox = GeometryUtilities.getScaledBoundingBox(
//				boxFromFirstEvent, regionDivisor);

        Rectangle2D boxFromLastEvent = track.getLast().getBBox();
        Rectangle2D endSearchBox = GeometryUtilities.getScaledBoundingBox(
                boxFromLastEvent, regionDivisor);

        Arrays.asList(new Rectangle2D[]{boxFromFirstEvent, endSearchBox})
                .parallelStream().forEach(boundingBox -> {
            if (boxFromFirstEvent.equals(boundingBox)) {
                popStartRecArea(boundingBox, track);
            }
        });
    }

    private void popStartRecArea(Rectangle2D boundingBox, ITrack track) {
        IntStream
                .rangeClosed((int) boundingBox.getMinX(),
                        (int) boundingBox.getMaxX())
                .parallel()
                .forEach(
                        x -> {
                            IntStream
                                    .rangeClosed((int) boundingBox.getMinY(),
                                            (int) boundingBox.getMaxY())
                                    .parallel().forEach(y -> {
                                searchSpace[x][y].add(track);
                            });
                        });
    }

    private void pushTrackIntoMatrix(ITrack track) {
        Rectangle2D boundingBox = GeometryUtilities.getScaledBoundingBox(track
                .getFirst().getShape(), regionDivisor);
        IntStream
                .rangeClosed((int) boundingBox.getMinX(),
                        (int) boundingBox.getMaxX())
                .parallel()
                .forEach(
                        x -> {
                            IntStream
                                    .rangeClosed((int) boundingBox.getMinY(),
                                            (int) boundingBox.getMaxY())
                                    .parallel().forEach(y -> {
                                searchSpace[x][y].add(track);
                            });
                        });
    }

    @Override
    public ArrayList<ITrack> filterOnInterval(Interval timePeriod) {
        // TODO Auto-generated method stub
        return null;
    }

}
