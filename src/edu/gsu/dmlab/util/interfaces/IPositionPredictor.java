/**
 * IPositionPredictor.java is an interface for the classes that will be used to predict
 * the location of polygons or search areas. 
 *
 * @author Dustin Kempton
 * @version 06/03/2015 
 * @Owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab.util.interfaces;


import java.awt.Polygon;
import java.awt.Rectangle;

import edu.gsu.dmlab.geometry.Point2D;

public interface IPositionPredictor {
	Point2D getPredictedPos(Point2D point, double span);

	Polygon getPredictedPos(Polygon poly, double span);

	Polygon getPredictedPos(Polygon poly, float[] movementVect, double span);

	Polygon getSearchRegion(Rectangle bBox, double span);

	Polygon getSearchRegion(Rectangle bBox, float[] movementVect, double span);
}