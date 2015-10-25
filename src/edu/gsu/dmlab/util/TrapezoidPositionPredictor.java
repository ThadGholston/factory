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

import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.geometry.Rectangle2D;
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
	public Point2D getPredictedPos(Point2D point, double span) {
		// TODO Auto-generated method stub
		Point2D HGSCoord = CoordinateSystemConverter.convertPixXYToHGS(point);
		HGSCoord = this.calcNewLoc(HGSCoord, span);
		return CoordinateSystemConverter.convertHGSToPixXY(HGSCoord);
	}

	/**
	 * getPredictedPos :returns the predicted position of all the points in the
	 * objectList, for the change in time,based upon the latitude of the point and the
	 * solar rotation at that latitude.
	 *
	 * @param poly
	 *            :the objectList of point to calculate the new position of.
	 * @param span
	 *            :the time span used to determine how far the sun has rotated
	 * @return :a new objectList of points with the new coordinates
	 */
	@Override
	public Point2D[] getPredictedPos(Point2D[] poly, double span) {
		Point2D[] outArr = new Point2D[poly.length];
		for (int i = 0; i < poly.length; i++) {
			outArr[i] = this.getPredictedPos(poly[i], span);
		}
		return outArr;
	}

	@Override
	public Point2D[] getPredictedPos(Point2D[] poly, float[] movementVect,
								   double span) {

		Point2D[] outArr = new Point2D[poly.length];
		double xMove = movementVect[0] * span;
		double yMove = movementVect[1] * span;

		for (int i = 0; i < poly.length; i++) {
			outArr[i] = new Point2D(poly[i].x + xMove, poly[i].y + yMove);
		}
		return outArr;
	}

	@Override
	public Point2D[] getSearchRegion(Rectangle2D bBox, double span) {

		Point2D oldCorner = new Point2D(bBox.x, bBox.y);
		Point2D rotatedCorner = this.getPredictedPos(oldCorner, span);

		Point2D rotatedLowerLeft = new Point2D(rotatedCorner.x, rotatedCorner.y
				+ bBox.height);

		Point2D upperLeft = rotatedCorner;
		Point2D lowerLeft = rotatedLowerLeft;

		Point2D upperCenterRight = new Point2D((rotatedCorner.x + bBox.width),
				rotatedCorner.y);
		Point2D lowerCenterRight = new Point2D((rotatedLowerLeft.x + bBox.width),
				rotatedLowerLeft.y);

		double length = upperCenterRight.x - upperLeft.x;
		double addedHeight = Math.tan(Math.toRadians(THETA)) * length;

		Point2D lowerRight = new Point2D(lowerCenterRight.x, lowerCenterRight.y
				- addedHeight);
		Point2D upperRight = new Point2D(upperCenterRight.x, upperCenterRight.y
				+ addedHeight);

		Point2D[] out = new Point2D[7];
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
	public Point2D[] getSearchRegion(Rectangle2D bBox, float[] movementVect, double span) {

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
		Point2D lowerCenterRight = new Point2D((rotatedLowerLeft.x + bBox.width),
				rotatedLowerLeft.y);

		double length = upperCenterRight.x - upperLeft.x;
		double addedHeight = Math.tan(Math.toRadians(THETA)) * length;

		Point2D lowerRight = new Point2D(lowerCenterRight.x, lowerCenterRight.y
				- addedHeight);
		Point2D upperRight = new Point2D(upperCenterRight.x, upperCenterRight.y
				+ addedHeight);

		Point2D[] out = new Point2D[7];
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
	Point2D calcNewLoc(Point2D pointIn, double days) {
		double x = pointIn.x
				+ days
				* (14.44 - 3.0 * Math.pow(Math.sin(Math.toDegrees(pointIn.y)),
				2.0));
		pointIn.x = x;
		return pointIn;
	}

}