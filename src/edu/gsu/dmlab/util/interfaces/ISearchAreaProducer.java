package edu.gsu.dmlab.util.interfaces;

import java.awt.Polygon;
import java.awt.Rectangle;

public interface ISearchAreaProducer {
	public Polygon getSearchRegion(Rectangle bBox, double span);
	public Polygon getSearchRegion(Rectangle bBox, float[] movementVect, double span);
}
