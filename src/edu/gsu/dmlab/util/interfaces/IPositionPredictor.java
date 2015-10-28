/**
 * IPositionPredictor.java is an interface for the classes that will be used to predict
 * the location of polygons or search areas. 
 *
 * @author Dustin Kempton
 * @version 06/03/2015 
 * @Owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab.util.interfaces;


import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.geometry.Rectangle2D;

public interface IPositionPredictor {
	Point2D getPredictedPos(Point2D point, double span);

	Point2D[] getPredictedPos(Point2D[] poly, double span);

	Point2D[] getPredictedPos(Point2D[] poly, float[] movementVect, double span);

	Point2D[] getSearchRegion(Rectangle2D bBox, double span);

	Point2D[] getSearchRegion(Rectangle2D bBox, float[] movementVect, double span);
}