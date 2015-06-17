/**
 * File:TrapezoidPositionPredictor.java, predicts the position of points and polygons
 * based upon the differential rotation of the sun, or a given normalized movement vector.
 * Similarly, this file also produces a search based upon the polygon representation or the mbr of
 * an object. The search are is a trapezoid that starts as the size of the mbr at the left and opens
 * up to the right.
 *   
 * @author Dustin Kempton
 * @version 06/03/2015 
 * @Owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab.util;


import org.opencv.core.Point;
import org.opencv.core.Rect;

import edu.gsu.dmlab.util.interfaces.IPositionPredictor;

public class TrapezoidPositionPredictor implements IPositionPredictor {
	static final double THETA = -1.5;

	/**
	 * getPredictedPos :returns the predicted position of a point, for the
	 * change in time, based upon the latitude of the point and the solar
	 * rotation at that latitude.
	 * 
	 * @param point
	 *            :the point to calculate the new position of.
	 * @param span
	 *            :the time span (in days) used to determine how far the sun has
	 *            rotated
	 * @return :a new point with the new coordinates
	 */
	@Override
	public Point getPredictedPos(Point point, double span) {
		// TODO Auto-generated method stub
		Point HGSCoord = CoordinateSystemConverter.convertPixXYToHGS(point);
		HGSCoord = this.calcNewLoc(HGSCoord, span);
		return CoordinateSystemConverter.convertHGSToPixXY(HGSCoord);
	}

	/**
	 * getPredictedPos :returns the predicted position of all the points in the
	 * list, for the change in time,based upon the latitude of the point and the
	 * solar rotation at that latitude.
	 * 
	 * @param poly
	 *            :the list of point to calculate the new position of.
	 * @param spanDays
	 *            :the time span used to determine how far the sun has rotated
	 * @return :a new list of points with the new coordinates
	 */
	@Override
	public Point[] getPredictedPos(Point[] poly, double span) {
		Point[] outArr = new Point[poly.length];
		for (int i = 0; i < poly.length; i++) {
			outArr[i] = this.getPredictedPos(poly[i], span);
		}
		return outArr;
	}

	@Override
	public Point[] getPredictedPos(Point[] poly, float[] movementVect,
			double span) {

		Point[] outArr = new Point[poly.length];
		double xMove = movementVect[0] * span;
		double yMove = movementVect[1] * span;

		for (int i = 0; i < poly.length; i++) {
			outArr[i] = new Point(poly[i].x + xMove, poly[i].y + yMove);
		}
		return outArr;
	}

	@Override
	public Point[] getSearchRegion(Rect bBox, double span) {

		Point oldCorner = new Point(bBox.x, bBox.y);
		Point rotatedCorner = this.getPredictedPos(oldCorner, span);

		Point rotatedLowerLeft = new Point(rotatedCorner.x, rotatedCorner.y
				+ bBox.height);

		Point upperLeft = rotatedCorner;
		Point lowerLeft = rotatedLowerLeft;

		Point upperCenterRight = new Point((rotatedCorner.x + bBox.width),
				rotatedCorner.y);
		Point lowerCenterRight = new Point((rotatedLowerLeft.x + bBox.width),
				rotatedLowerLeft.y);

		double length = upperCenterRight.x - upperLeft.x;
		double addedHeight = Math.tan(Math.toRadians(THETA)) * length;

		Point lowerRight = new Point(lowerCenterRight.x, lowerCenterRight.y
				- addedHeight);
		Point upperRight = new Point(upperCenterRight.x, upperCenterRight.y
				+ addedHeight);

		Point[] out = new Point[7];
		out[0] = upperLeft;
		out[1] = lowerLeft;
		out[2] = lowerRight;
		out[3] = lowerCenterRight;
		out[4] = upperCenterRight;
		out[5] = upperRight;
		out[6] = upperLeft;

		return out;
	}

	@Override
	public Point[] getSearchRegion(Rect bBox, float[] movementVect, double span) {

		double xMove = movementVect[0] * span;
		double yMove = movementVect[1] * span;

		Point oldCorner = new Point(bBox.x, bBox.y);
		Point rotatedCorner = new Point(oldCorner.x + xMove, oldCorner.y
				+ yMove);

		Point rotatedLowerLeft = new Point(rotatedCorner.x, rotatedCorner.y
				+ bBox.height);

		Point upperLeft = rotatedCorner;
		Point lowerLeft = rotatedLowerLeft;

		Point upperCenterRight = new Point((rotatedCorner.x + bBox.width),
				rotatedCorner.y);
		Point lowerCenterRight = new Point((rotatedLowerLeft.x + bBox.width),
				rotatedLowerLeft.y);

		double length = upperCenterRight.x - upperLeft.x;
		double addedHeight = Math.tan(Math.toRadians(THETA)) * length;

		Point lowerRight = new Point(lowerCenterRight.x, lowerCenterRight.y
				- addedHeight);
		Point upperRight = new Point(upperCenterRight.x, upperCenterRight.y
				+ addedHeight);

		Point[] out = new Point[7];
		out[0] = upperLeft;
		out[1] = lowerLeft;
		out[2] = lowerRight;
		out[3] = lowerCenterRight;
		out[4] = upperCenterRight;
		out[5] = upperRight;
		out[6] = upperLeft;

		return out;
	}

	/**
	 * calcNewLoc :calculates the new location in HGS based on time passed and
	 * latitude
	 * 
	 * @param pointIn
	 *            :point in HGS to calculate the new position of
	 * @param days
	 *            :how far in the future, in days, for which to calculate the
	 *            position of the point
	 * @return : point with new HGS coordinates
	 */
	Point calcNewLoc(Point pointIn, double days) {
		double x = pointIn.x
				+ days
				* (14.44 - 3.0 * Math.pow(Math.sin(Math.toDegrees(pointIn.y)),
						2.0));
		pointIn.x = x;
		return pointIn;
	}

}
