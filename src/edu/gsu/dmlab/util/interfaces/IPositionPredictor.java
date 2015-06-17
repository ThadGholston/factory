/**
 * IPositionPredictor.java is an interface for the classes that will be used to predict
 * the location of polygons or search areas. 
 *   
 * @author Dustin Kempton
 * @version 06/03/2015 
 * @Owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab.util.interfaces;

import org.opencv.core.Point;
import org.opencv.core.Rect;

public interface IPositionPredictor {
	Point getPredictedPos(Point point, double span);

	Point[] getPredictedPos(Point[] poly, double span);

	Point[] getPredictedPos(Point[] poly, float[] movementVect, double span);

	Point[] getSearchRegion(Rect bBox, double span);

	Point[] getSearchRegion(Rect bBox, float[] movementVect, double span);
}
