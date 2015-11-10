package edu.gsu.dmlab.geometry;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by thad on 10/25/15.
 */
public class GeometryUtilities {
	// for use in getting search area, the angle of the growth
	private static final double THETA_RAD = Math.toRadians(10.0);

	// public static Polygon getScaledSearchArea(Polygon poly, int divisor) {
	//
	// int[] xArr = poly.xpoints;
	// int[] yArr = poly.ypoints;
	// int[] scaledXArr = new int[xArr.length];
	// int[] scaledYArr = new int[yArr.length];
	// for (int i = 0; i < xArr.length; i++) {
	// scaledXArr[i] = xArr[i] / divisor;
	// scaledYArr[i] = yArr[i] / divisor;
	// }
	//
	// Polygon scaledPoly = new Polygon(scaledXArr, scaledYArr,
	// scaledXArr.length);
	// Rectangle scaledRect = scaledPoly.getBounds();
	//
	// int[] searchXArr = new int[6];
	// int[] searchYArr = new int[6];
	// int addedSize = (int) (Math.tan(THETA_RAD) * scaledRect.getWidth());
	// // Point 1
	// searchXArr[0] = scaledRect.x;
	// searchYArr[0] = scaledRect.y;
	//
	// // Point 2
	// searchXArr[1] = scaledRect.x;
	// searchYArr[1] = (int) (scaledRect.y + scaledRect.getHeight());
	//
	// // Point 3
	// searchXArr[2] = (int) (scaledRect.x + scaledRect.getWidth());
	// searchYArr[2] = searchYArr[1] + addedSize;
	//
	// // Point 4
	// searchXArr[3] = searchXArr[2];
	// searchYArr[3] = searchYArr[1];
	//
	// // Point 5
	// searchXArr[4] = searchXArr[2];
	// searchYArr[4] = scaledRect.y;
	//
	// // Point 6
	// searchXArr[5] = searchXArr[2];
	// searchYArr[5] = scaledRect.y - addedSize;
	//
	// Polygon returnPoly = new Polygon(searchXArr, searchYArr,
	// searchXArr.length);
	// return returnPoly;
	// }

	// public static edu.gsu.dmlab.geometry.Point2D[] getScaledSearchArea(
	// ITrack track, double divisor) {
	// edu.gsu.dmlab.geometry.Point2D[] startShape = track.getFirst()
	// .getShape();
	// edu.gsu.dmlab.geometry.Point2D[] searchArea = new
	// edu.gsu.dmlab.geometry.Point2D[startShape.length];
	// for (int index = 0; index < startShape.length; index++) {
	// edu.gsu.dmlab.geometry.Point2D point = startShape[index];
	// searchArea[index] = new edu.gsu.dmlab.geometry.Point2D(point.x
	// / divisor, point.y / divisor);
	// }
	// return searchArea;
	// }

	public static Circle2D getMiniumEnclosingCircle(
			edu.gsu.dmlab.geometry.Point2D[] area) {
		// TODO: Implement getMiniumEnclosingCircle
		return null;
	}

	public static Rectangle scaleBoundingBox(Rectangle boundingBox, int divisor) {
		double x, y, width, height;
		x = boundingBox.getMinX() / divisor;
		y = boundingBox.getMinY() / divisor;
		width = boundingBox.getWidth() / divisor;
		height = boundingBox.getHeight() / divisor;
		Rectangle scaledBoundBox = new Rectangle();
		scaledBoundBox.setFrame(x, y, width, height);
		return scaledBoundBox;
	}

	public static Polygon scalePolygon(Polygon poly, int divisor) {
		int[] xArr = poly.xpoints;
		int[] yArr = poly.ypoints;
		int[] scaledXArr = new int[xArr.length];
		int[] scaledYArr = new int[yArr.length];
		for (int i = 0; i < xArr.length; i++) {
			scaledXArr[i] = xArr[i] / divisor;
			scaledYArr[i] = yArr[i] / divisor;
		}

		Polygon scaledPoly = new Polygon(scaledXArr, scaledYArr,
				scaledXArr.length);
		return scaledPoly;
	}

}
