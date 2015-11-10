package edu.gsu.dmlab.geometry;


import java.awt.Polygon;
import java.awt.Rectangle;


/**
 * Created by thad on 10/25/15.
 */
public class GeometryUtilities {
	

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
