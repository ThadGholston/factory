package edu.gsu.dmlab.util;

import edu.gsu.dmlab.geometry.Circle2D;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.geometry.Rectangle2D;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;

import java.util.ArrayList;

/**
 * Created by thad on 9/22/15.
 */
public class Utility {
    public static Point2D[] getScaledSearchArea(ArrayList<Point2D> area, int divisor){
        Point2D[] scaledSearchArea = new Point2D[area.size()];
        for(int index = 0; index < area.size(); index++){
            Point2D point = area.get(index);
            Point2D p = new Point2D(point.getX() / divisor, point.getY() / divisor);
            scaledSearchArea[index] = p;
        }
        return scaledSearchArea;
    }

    public static Point2D[] getScaledSearchArea(ITrack track, int divisor){
        Point2D[] startShape = track.getFirst().getShape();
        Point2D[] searchArea = new Point2D[startShape.length];
        for (int index = 0; index < startShape.length; index++){
            Point2D point = startShape[index];
            searchArea[index] = new Point2D(point.x / divisor, point.y / divisor);
        }
        return searchArea;
    }



    public static Rectangle2D getBoundingBox(Point2D[] searchArea) {
        double xMin = Double.MIN_VALUE, xMax = Double.MIN_VALUE, yMin = Double.MIN_VALUE, yMax = Double.MIN_VALUE;
        Rectangle2D rectangle = new Rectangle2D();
        for (Point2D point: searchArea){
            rectangle.add(point);
        }
        return (Rectangle2D) rectangle.getBounds2D();
    }

    public static boolean isInsideSearchArea(Point2D queryPoint, Point2D[] searchArea){
        Rectangle2D rectangle = Utility.getBoundingBox(searchArea);
        return rectangle.contains(queryPoint);
    }

    public static Circle2D getMiniumEnclosingCircle(Point2D[] area){
        //TODO: Implement getMiniumEnclosingCircle
        return null;
    }

}
