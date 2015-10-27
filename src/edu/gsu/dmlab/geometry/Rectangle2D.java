package edu.gsu.dmlab.geometry;

/**
 * Created by thad on 10/3/15.
 * Edited by Dustin Kempton on 10/27/15
 */

@SuppressWarnings("serial")
public class Rectangle2D extends java.awt.geom.Rectangle2D.Double {
	
	public Rectangle2D() {
		super();
	}
	
	public Rectangle2D(int x, int y, int width, int height) {
		super();
		super.x = x;
		super.y = y;
		super.width = width;
		super.height = height;
	}
}
