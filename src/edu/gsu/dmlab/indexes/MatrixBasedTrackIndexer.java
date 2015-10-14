package edu.gsu.dmlab.indexes;

import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.geometry.Rectangle2D;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.indexes.interfaces.AbstractMatrixRangeIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Created by thad on 10/11/15.
 */
public class MatrixBasedTrackIndexer extends AbstractMatrixRangeIndexer<ITrack> implements ITrackIndexer {

    private TList[][] matrixEnd;
    private int regionDimension;

    public MatrixBasedTrackIndexer(ArrayList list) {
        super(list);
    }

    @Override
    protected void index() {
        // matrix = new TList[regionDimension][regionDimension];
        // matrixEnd = new TList[regionDimension][regionDimension];
        list.parallelStream().forEach(track -> {
            indexTrack(track);
        });
    }

    private void indexTrack(ITrack track) {
        if (trackIsOfTypeSG(track)) {
            pushSGTrackIntoMatrices(track);
        } else {
            pushTrackIntoMatrices(track);
        }
    }

    private boolean trackIsOfTypeSG(ITrack track) {
        return track.getFirst().getType().equals("SG");
    }
    // IntStream.range(0, this.regionDimension * this.regionDimension).parallel().forEach(index -> {
    // int Y = (int)(index / this.regionDimension);
    // int X = index - (Y * this.regionDimension);
    // });

    private void pushSGTrackIntoMatrices(ITrack track) {
        Rectangle2D boxFromFirstEvent = track.getFirst().getBBox();
        Rectangle2D beginSearchBox = getScaledBoundingBox(boxFromFirstEvent);

        Rectangle2D boxFromLastEvent = track.getLast().getBBox();
        Rectangle2D endSearchBox = getScaledBoundingBox(boxFromLastEvent);

        Arrays.asList(new Rectangle2D[]{boxFromFirstEvent, endSearchBox}).parallelStream().forEach(boundingBox ->{
            if (boxFromFirstEvent.equals(boundingBox)){
                popStartRecArea(boundingBox, track);
            } else {
                popEndRecArea(boundingBox, track);
            }
        });
    }

    private void popStartRecArea(Rectangle2D boundingBox, ITrack track) {
        IntStream.rangeClosed((int) boundingBox.getMinX(), (int) boundingBox.getMaxX()).parallel().forEach(x -> {
            IntStream.rangeClosed((int) boundingBox.getMinY(), (int) boundingBox.getMaxY()).parallel().forEach(y -> {
                matrix[x][y].add(track);
            });
        });
    }

    private void popEndRecArea(Rectangle2D boundingBox, ITrack track) {
        IntStream.rangeClosed((int) boundingBox.getMinX(), (int) boundingBox.getMaxX()).parallel().forEach(x -> {
            IntStream.rangeClosed((int) boundingBox.getMinY(), (int) boundingBox.getMaxY()).parallel().forEach(y -> {
                matrixEnd[x][y].add(track);
            });
        });
    }


    private Rectangle2D getScaledBoundingBox(Rectangle2D boundingBox) {
        double x, y, width, height;
        x = boundingBox.getMinX() / this.regionDivisor;
        y = boundingBox.getMinY() / this.regionDivisor;
        width = boundingBox.getWidth() / this.regionDivisor;
        height = boundingBox.getHeight() / this.regionDivisor;
        Rectangle2D scaledBoundBox = new Rectangle2D();
        scaledBoundBox.setFrame(x, y, width, height);
        return scaledBoundBox;
    }

    private Rectangle2D getScaledBoundingBox(Point2D[] searchArea) {
        Rectangle2D scaledBoundBox = new Rectangle2D();
        Arrays.asList(searchArea).parallelStream().forEach(point2D -> {
            scaledBoundBox.add(point2D.getX() / this.regionDivisor, point2D.getY() / this.regionDivisor);
        });
        return scaledBoundBox;
    }

    private void pushTrackIntoMatrices(ITrack track) {
        ArrayList<IRegionAdder> adder = new ArrayList<>();
        adder.add(new StartRegionAdder());
        adder.add(new EndRegionAdder());
        adder.parallelStream().forEach(iRegionAdder -> {
            iRegionAdder.addToRegion(track);
        });

    }

    private interface IRegionAdder{
        void addToRegion(ITrack track);
    }

    private class StartRegionAdder implements IRegionAdder{

        @Override
        public void addToRegion(ITrack track) {
            Rectangle2D boundingBox = getScaledBoundingBox(track.getFirst().getShape());
            IntStream.rangeClosed((int) boundingBox.getMinX(), (int) boundingBox.getMaxX()).parallel().forEach(x -> {
                IntStream.rangeClosed((int) boundingBox.getMinY(), (int) boundingBox.getMaxY()).parallel().forEach(y -> {
                    matrix[x][y].add(track);
                });
            });
        }
    }

    private class EndRegionAdder implements IRegionAdder{
        @Override
        public void addToRegion(ITrack track) {
            Rectangle2D boundingBox = getScaledBoundingBox(track.getLast().getShape());
            IntStream.rangeClosed((int) boundingBox.getMinX(), (int) boundingBox.getMaxX()).parallel().forEach(x -> {
                IntStream.rangeClosed((int) boundingBox.getMinY(), (int) boundingBox.getMaxY()).parallel().forEach(y -> {
                    matrixEnd[x][y].add(track);
                });
            });
        }
    }

    @Override
    public ArrayList<ITrack> getTracksStartBetween(DateTime begin, DateTime end, Point2D[] searchArea) {
        return null;
    }

    @Override
    public ArrayList<ITrack> getTracksEndBetween(DateTime begin, DateTime end, Point2D[] searchArea) {
        return null;
    }

    private ArrayList<ITrack> getTracksInRange(DateTime begin, DateTime end, Point2D[] searchArea, TList[][] region){
        ArrayList<ITrack> inSearch = new ArrayList<>();
        Point2D[] scaledSearchArea = null;
        Rectangle2D boundingBox = getScaledBoundingBox(searchArea);
        Interval interval = new Interval(begin, end);
        ArrayList<ITrack> searchResults = new ArrayList<>();
        int yMin = (int) ((boundingBox.getMinY() < 0) ? 0 : boundingBox.getMinY());
        int xMin = (int) ((boundingBox.getMinX() < 0) ? 0 : boundingBox.getMinX());
        int xMax = (int) ((boundingBox.getMinY() < 0) ? 0 : boundingBox.getMaxX());
        int yMax = (int) ((boundingBox.getMinY() < 0) ? 0 : boundingBox.getMaxY());
        IntStream.range(xMin, xMax).parallel().forEach(x -> {
            IntStream.range(yMin, yMax).parallel().forEach(y -> {
                Point2D testPoint = new Point2D(x, y);
                if (boundingBox.contains(x, y) &&
                        region[x][y].size() > 0) {
                    ArrayList<ITrack> intermediateResults = region[x][y];
                    for (ITrack object : intermediateResults) {
                        if (interval.contains(object.getTimePeriod())) {
                            synchronized (this) {
                                if (!searchResults.contains(object)) {
                                    searchResults.add(object);
                                }
                            }
                        }
                    }
                }
            });
        });
        return searchResults;
    }

    @Override
    public ArrayList<ITrack> getAll() {
        return list;
    }
}
