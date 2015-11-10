package edu.gsu.dmlab.tracking;

import java.awt.Polygon;
import java.awt.Rectangle;


import org.la4j.matrix.SparseMatrix;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.geometry.GeometryUtilities;

import edu.gsu.dmlab.tracking.interfaces.ILocationProbCal;

public class LocationProbCalc implements ILocationProbCal {

	String filLoc;
	SparseMatrix mat;
	double max;
	int scalFactor;

	LocationProbCalc(String fileLoc, int regionDiv) {
		if (fileLoc == null)
			throw new IllegalArgumentException("FileLocation cannot be null");
		this.mat = SparseMatrix.fromCSV(fileLoc);
		this.scalFactor = regionDiv;
		this.max = mat.max();
	}

	@Override
	public double calcProb(IEvent ev) {
		Polygon scaledPoly = GeometryUtilities.scalePolygon(ev.getShape(), this.scalFactor);
		Rectangle scaledBoundingBox = scaledPoly.getBounds();

		double returnValue = 0;
		double minVal = 0;
		boolean isSet = false;
		int count = 0;

		for (int x = (int) scaledBoundingBox.getMinX(); x <= scaledBoundingBox.getMaxX(); x++) {
			for (int y = (int) scaledBoundingBox.getMinY(); y <= scaledBoundingBox.getMaxY(); y++) {
				double tmp = this.mat.getOrElse(x, y, 0.0);
				if (!isSet) {
					minVal = tmp;
					isSet = true;
				} else {
					if (tmp < minVal) {
						minVal = tmp;
					}
				}
				returnValue += tmp;
				count++;
			}
		}

		if (minVal > 0) {
			returnValue = minVal / this.max;
		} else {
			returnValue = returnValue / count;
			returnValue = returnValue / this.max;
		}

		// if we can't calculate a value, who knows what the probability is.
		// We'll just cal it 50/50.
		if (returnValue <= 0) {
			returnValue = 0.5;
		}

		return returnValue;
	}

}
