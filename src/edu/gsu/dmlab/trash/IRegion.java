//package edu.gsu.dmlab.datatypes.interfaces;
//
//import edu.gsu.dmlab.geometry.Point2D;
//import edu.gsu.dmlab.geometry.Rectangle2D;
//import org.joda.time.Interval;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//
///**
// * Created by thad on 10/4/15.
// */
//public interface IRegion<T> {
//
//    IRegion newInstance();
//
//    ArrayList<T> searchForElementsInRegion(Rectangle2D searchBox, Point2D[] searchPolygon);
////        ArrayList<T> searchResults = new ArrayList<>();
////        int yMin = (int) ((searchBox.getMinY() < 0) ? 0 : searchBox.getMinY());
////        int xMin = (int) ((searchBox.getMinX() < 0) ? 0 : searchBox.getMinX());
////        int xMax = (int) ((searchBox.getMinY() < 0) ? 0 : searchBox.getMaxX());
////        int yMax = (int) ((searchBox.getMinY() < 0) ? 0 : searchBox.getMaxY());
////        IntStream.range(xMin, xMax).parallel().forEach(x -> {
////            IntStream.range(yMin, yMax).parallel().forEach(y -> {
////                Point2D testPoint = new Point2D(x, y);
////                if (Utility.isInsideSearchArea(testPoint, searchPolygon) &&
////                        this.get(x, y).size() > 0) {
////                    ArrayList<T> intermediateResults = this.get(x, y);
////                    for (T object : intermediateResults) {
////                        synchronized (this) {
////                            if (!searchResults.contains(object)) {
////                                searchResults.add(object);
////                            }
////                        }
////                    }
////                }
////            });
////        });
////        return searchResults;
////    }
//
//    ArrayList<T> searchForElementsInSpaceAndTime(Interval interval, Rectangle2D searchBox, Point2D[] searchPolygon);
////        ArrayList<T> searchResults = new ArrayList<>();
////        int yMin = (int) ((searchBox.getMinY() < 0) ? 0 : searchBox.getMinY());
////        int xMin = (int) ((searchBox.getMinX() < 0) ? 0 : searchBox.getMinX());
////        int xMax = (int) ((searchBox.getMinY() < 0) ? 0 : searchBox.getMaxX());
////        int yMax = (int) ((searchBox.getMinY() < 0) ? 0 : searchBox.getMaxY());
////        IntStream.range(xMin, xMax).parallel().forEach(x -> {
////            IntStream.range(yMin, yMax).parallel().forEach(y -> {
////                Point2D testPoint = new Point2D(x, y);
////                if (Utility.isInsideSearchArea(testPoint, searchPolygon) &&
////                        this.get(x, y).size() > 0) {
////                    ArrayList<T> intermediateResults = this.get(x, y);
////                    for (T object : intermediateResults) {
////                        if (interval.contains(object.getTimePeriod())) {
////                            synchronized (this) {
////                                if (!searchResults.contains(object)) {
////                                    searchResults.add(object);
////                                }
////                            }
////                        }
////                    }
////                }
////            });
////        });
////        return searchResults;
////    }
//
//    void addElementsToRegion(Rectangle2D.Double beginSearchBox, T track);
//
//    void sort3Dimesion(Comparator<T> compartor);
//
//    void add(int x, int y, T obj);
//}
