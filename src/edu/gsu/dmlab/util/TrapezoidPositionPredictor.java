/**
 * File:TrapezoidPositionPredictor.java, predicts the position of points and polygons
 * based upon the differential rotation of the sun, or a given normalized movement vector.
 * Similarly, this file also produces a search based upon the polygon representation or the mbr of
 * an object. The search are is a trapezoid that starts as the size of the mbr at the left and opens
 * up to the right.
 *
 * @author Dustin Kempton
 * @version 10/10/2015 
 * @Owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab.util;

import java.awt.Polygon;
import java.awt.Rectangle;

import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.util.interfaces.ISearchAreaProducer;

public class TrapezoidPositionPredictor implements ISearchAreaProducer {
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

	Point2D getPredictedPos(Point2D point, double span) {
		Point2D HGSCoord = CoordinateSystemConverter.convertPixXYToHGS(point);
		HGSCoord = this.calcNewLoc(HGSCoord, span);
		return CoordinateSystemConverter.convertHGSToPixXY(HGSCoord);
	}

	/**
	 * getPredictedPos :returns the predicted position of all the points in the
	 * objectList, for the change in time,based upon the latitude of the point
	 * and the solar rotation at that latitude.
	 *
	 * @param poly
	 *            :the objectList of point to calculate the new position of.
	 * @param span
	 *            :the time span used to determine how far the sun has rotated
	 * @return :a new objectList of points with the new coordinates
	 */

	Polygon getPredictedPos(Polygon poly, double span) {

		int[] xArr = new int[poly.xpoints.length];
		int[] yArr = new int[poly.xpoints.length];
		for (int i = 0; i < poly.xpoints.length; i++) {
			Point2D origPoint = new Point2D(poly.xpoints[i], poly.ypoints[i]);
			Point2D shiftedPoint = this.getPredictedPos(origPoint, span);
			xArr[i] = (int) shiftedPoint.x;
			yArr[i] = (int) shiftedPoint.y;
		}

		Polygon outPoly = new Polygon(xArr, yArr, xArr.length);
		return outPoly;
	}

	Polygon getPredictedPos(Polygon poly, float[] movementVect, double span) {

		double xMove = movementVect[0] * span;
		double yMove = movementVect[1] * span;

		int[] xArr = new int[poly.xpoints.length];
		int[] yArr = new int[poly.xpoints.length];
		for (int i = 0; i < poly.xpoints.length; i++) {
			xArr[i] = (int) (poly.xpoints[i] + xMove);
			yArr[i] = (int) (poly.ypoints[i] + yMove);
		}

		Polygon outPoly = new Polygon(xArr, yArr, xArr.length);
		return outPoly;
	}

	@Override
	public Polygon getSearchRegion(Rectangle bBox, double span) {

		Point2D oldCorner = new Point2D(bBox.x, bBox.y);
		Point2D rotatedCorner = this.getPredictedPos(oldCorner, span);

		Point2D rotatedLowerLeft = new Point2D(rotatedCorner.x, rotatedCorner.y
				+ bBox.height);

		Point2D upperLeft = rotatedCorner;
		Point2D lowerLeft = rotatedLowerLeft;

		Point2D upperCenterRight = new Point2D((rotatedCorner.x + bBox.width),
				rotatedCorner.y);
		Point2D lowerCenterRight = new Point2D(
				(rotatedLowerLeft.x + bBox.width), rotatedLowerLeft.y);

		double length = upperCenterRight.x - upperLeft.x;
		double addedHeight = Math.tan(Math.toRadians(THETA)) * length;

		Point2D lowerRight = new Point2D(lowerCenterRight.x, lowerCenterRight.y
				- addedHeight);
		Point2D upperRight = new Point2D(upperCenterRight.x, upperCenterRight.y
				+ addedHeight);

		int[] searchXArr = new int[6];
		int[] searchYArr = new int[6];

		// out.add(upperLeft);
		searchXArr[0] = (int) upperLeft.x;
		searchYArr[0] = (int) upperLeft.y;

		// out.add(lowerLeft);
		searchXArr[1] = (int) lowerLeft.x;
		searchYArr[1] = (int) lowerLeft.y;

		// out.add(lowerRight);
		searchXArr[2] = (int) lowerRight.x;
		searchYArr[2] = (int) lowerRight.y;

		// out.add(lowerCenterRight);
		searchXArr[3] = (int) lowerCenterRight.x;
		searchYArr[3] = (int) lowerCenterRight.y;

		// out.add(upperCenterRight);
		searchXArr[4] = (int) upperCenterRight.x;
		searchYArr[4] = (int) upperCenterRight.y;

		// out.add(upperRight);
		searchXArr[5] = (int) upperRight.x;
		searchYArr[5] = (int) upperRight.y;

		Polygon out = new Polygon(searchXArr, searchYArr, searchXArr.length);
		return out;
	}

	@Override
	public Polygon getSearchRegion(Rectangle bBox, float[] movementVect,
			double span) {

		double xMove = movementVect[0] * span;
		double yMove = movementVect[1] * span;

		Point2D oldCorner = new Point2D(bBox.x, bBox.y);
		Point2D rotatedCorner = new Point2D(oldCorner.x + xMove, oldCorner.y
				+ yMove);

		Point2D rotatedLowerLeft = new Point2D(rotatedCorner.x, rotatedCorner.y
				+ bBox.height);

		Point2D upperLeft = rotatedCorner;
		Point2D lowerLeft = rotatedLowerLeft;

		Point2D upperCenterRight = new Point2D((rotatedCorner.x + bBox.width),
				rotatedCorner.y);
		Point2D lowerCenterRight = new Point2D(
				(rotatedLowerLeft.x + bBox.width), rotatedLowerLeft.y);

		double length = upperCenterRight.x - upperLeft.x;
		double addedHeight = Math.tan(Math.toRadians(THETA)) * length;

		Point2D lowerRight = new Point2D(lowerCenterRight.x, lowerCenterRight.y
				- addedHeight);
		Point2D upperRight = new Point2D(upperCenterRight.x, upperCenterRight.y
				+ addedHeight);

		int[] searchXArr = new int[6];
		int[] searchYArr = new int[6];

		// out.add(upperLeft);
		searchXArr[0] = (int) upperLeft.x;
		searchYArr[0] = (int) upperLeft.y;

		// out.add(lowerLeft);
		searchXArr[1] = (int) lowerLeft.x;
		searchYArr[1] = (int) lowerLeft.y;

		// out.add(lowerRight);
		searchXArr[2] = (int) lowerRight.x;
		searchYArr[2] = (int) lowerRight.y;

		// out.add(lowerCenterRight);
		searchXArr[3] = (int) lowerCenterRight.x;
		searchYArr[3] = (int) lowerCenterRight.y;

		// out.add(upperCenterRight);
		searchXArr[4] = (int) upperCenterRight.x;
		searchYArr[4] = (int) upperCenterRight.y;

		// out.add(upperRight);
		searchXArr[5] = (int) upperRight.x;
		searchYArr[5] = (int) upperRight.y;

		Polygon out = new Polygon(searchXArr, searchYArr, searchXArr.length);
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
	Point2D calcNewLoc(Point2D pointIn, double days) {
		double x = pointIn.x
				+ days
				* (14.44 - 3.0 * Math.pow(Math.sin(Math.toDegrees(pointIn.y)),
						2.0));
		pointIn.x = x;
		return pointIn;
	}

}