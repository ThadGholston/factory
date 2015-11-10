package tests.geometry;

import static org.junit.Assert.*;

import java.awt.Polygon;
import java.awt.Rectangle;

import org.junit.Test;

import edu.gsu.dmlab.geometry.GeometryUtilities;

public class UtilitiesTests {

	@Test
	public void testScalePolygonOnX() {
		int[] xArr = { 4, 4, 8, 8 };
		int[] yArr = { 4, 8, 8, 4 };
		int divisor = 4;
		Polygon pgon = new Polygon(xArr, yArr, xArr.length);
		Polygon scaledPgon = GeometryUtilities.scalePolygon(pgon, divisor);

		assertArrayEquals(new int[] { xArr[0] / divisor, xArr[1] / divisor, xArr[2] / divisor, xArr[3] / divisor },
				scaledPgon.xpoints);
	}

	@Test
	public void testScalePolygonOnY() {
		int[] xArr = { 4, 4, 8, 8 };
		int[] yArr = { 4, 8, 8, 4 };
		int divisor = 4;
		Polygon pgon = new Polygon(xArr, yArr, xArr.length);
		Polygon scaledPgon = GeometryUtilities.scalePolygon(pgon, divisor);

		assertArrayEquals(new int[] { yArr[0] / divisor, yArr[1] / divisor, yArr[2] / divisor, yArr[3] / divisor },
				scaledPgon.ypoints);
	}

	@Test
	public void testScaleBoundingBoxX() {
		int x = 4;
		int y = 16;
		int height = 32;
		int width = 8;
		Rectangle rect = new Rectangle(x, y, width, height);
		int divisor = 4;
		Rectangle scaledRect = GeometryUtilities.scaleBoundingBox(rect, divisor);

		assertTrue(scaledRect.x == x / divisor);

	}

	@Test
	public void testScaleBoundingBoxY() {
		int x = 4;
		int y = 16;
		int height = 32;
		int width = 8;
		Rectangle rect = new Rectangle(x, y, width, height);
		int divisor = 4;
		Rectangle scaledRect = GeometryUtilities.scaleBoundingBox(rect, divisor);

		assertTrue(scaledRect.y == y / divisor);

	}
	
	@Test
	public void testScaleBoundingBoxHeight() {
		int x = 4;
		int y = 16;
		int height = 32;
		int width = 8;
		Rectangle rect = new Rectangle(x, y, width, height);
		int divisor = 4;
		Rectangle scaledRect = GeometryUtilities.scaleBoundingBox(rect, divisor);

		assertTrue(scaledRect.getHeight() == height / divisor);

	}
	
	@Test
	public void testScaleBoundingBoxWidth() {
		int x = 4;
		int y = 16;
		int height = 32;
		int width = 8;
		Rectangle rect = new Rectangle(x, y, width, height);
		int divisor = 4;
		Rectangle scaledRect = GeometryUtilities.scaleBoundingBox(rect, divisor);

		assertTrue(scaledRect.getWidth() == width / divisor);

	}
}
