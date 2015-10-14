//package edu.gsu.dmlab.datatypes;
//
//import edu.gsu.dmlab.geometry.Point2D;
//import edu.gsu.dmlab.geometry.Rectangle2D;
//import edu.gsu.dmlab.datatypes.interfaces.IRegion;
//import edu.gsu.dmlab.datatypes.interfaces.ITrack;
//import edu.gsu.dmlab.util.Utility;
//import org.joda.time.Interval;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.stream.IntStream;
//
///**
// * Created by thad on 10/5/15.
// */
//public class MatRegion implements IRegion {
//    public class TrackLists extends ArrayList<ITrack> {
//
//    }
//
//    private TrackLists[][] region;
//
//    public MatRegion(int x, int y) {
//        region = new TrackLists[x][y];
//    }
//
//    @Override
//    public ArrayList searchForElementsInRegion(Rectangle2D searchBox, Point2D[] searchPolygon) {
//        return null;
//    }
//
//    @Override
//    public ArrayList<ITrack> searchForElementsInSpaceAndTime(Interval interval, Rectangle2D searchBox, Point2D[] searchPolygon) {
//        ArrayList<ITrack> searchResults = new ArrayList<>();
//        int yMin = (int) ((searchBox.getMinY() < 0) ? 0 : searchBox.getMinY());
//        int xMin = (int) ((searchBox.getMinX() < 0) ? 0 : searchBox.getMinX());
//        int xMax = (int) ((searchBox.getMinY() < 0) ? 0 : searchBox.getMaxX());
//        int yMax = (int) ((searchBox.getMinY() < 0) ? 0 : searchBox.getMaxY());
//        ArrayList<ArrayList<ArrayList<ITrack>>> test = new ArrayList<>();
//        IntStream.range(xMin, xMax).parallel().forEach(x -> {
//            IntStream.range(yMin, yMax).parallel().forEach(y -> {
//                Point2D testPoint = new Point2D(x, y);
//                if (Utility.isInsideSearchArea(testPoint, searchPolygon) &&
//                        this.region[x][y].size() > 0) {
//                    TrackLists intermediateResults = this.region[x][y];
//                    for (ITrack object : intermediateResults) {
//                        if (interval.contains(object.getTimePeriod())) {
//                            synchronized (this) {
//                                if (!searchResults.contains(object)) {
//                                    searchResults.add(object);
//                                }
//                            }
//                        }
//                    }
//                }
//            });
//        });
//
//        return searchResults;
//    }
//
//    @Override
//    public void addElementsToRegion(Rectangle2D.Double beginSearchBox, Object track) {
//
//    }
//
//    @Override
//    public void sort3Dimesion(Comparator compartor) {
//
//    }
//
//    @Override
//    public void add(int x, int y, Object obj) {
//
//    }
//
//
//}
