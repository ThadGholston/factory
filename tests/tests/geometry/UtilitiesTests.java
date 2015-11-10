package tests.geometry;

import static org.junit.Assert.*;

import java.awt.Polygon;

import org.junit.Test;

import edu.gsu.dmlab.geometry.GeometryUtilities;

public class UtilitiesTests {

	@Test
	public void testScalePolygon() {
		int[] xArr = {};
		int[] yArr = {};
		Polygon pgon = new Polygon();
		Polygon scaledPgon = GeometryUtilities.scalePolygon(pgon, 4);
		
	}

}
