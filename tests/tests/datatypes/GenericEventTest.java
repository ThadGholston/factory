package tests.datatypes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.awt.Polygon;
import java.awt.Rectangle;

import org.joda.time.Interval;
import org.junit.Test;

import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.datatypes.GenericEvent;
import edu.gsu.dmlab.geometry.Point2D;

/**
 * Implemented by Dustin Kempton 10/10/15
 */
public class GenericEventTest {

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnNullInterval() throws IllegalArgumentException {
		Point2D point = mock(Point2D.class);
		Rectangle rect = mock(Rectangle.class);
		Polygon poly = mock(Polygon.class);
		@SuppressWarnings("unused")
		GenericEvent event = new GenericEvent(0, null, point, rect, poly, EventType.ACTIVE_REGION);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnNullPoint() throws IllegalArgumentException {
		Interval intvl = new Interval(6000, 8000);
		Rectangle rect = mock(Rectangle.class);
		Polygon poly = mock(Polygon.class);
		@SuppressWarnings("unused")
		GenericEvent event = new GenericEvent(0, intvl, null, rect, poly, EventType.ACTIVE_REGION);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnNullRectangle() throws IllegalArgumentException {
		Interval intvl = new Interval(6000, 8000);
		Point2D point = mock(Point2D.class);
		Polygon poly = mock(Polygon.class);
		@SuppressWarnings("unused")
		GenericEvent event = new GenericEvent(0, intvl, point, null, poly, EventType.ACTIVE_REGION);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnNullPolygon() throws IllegalArgumentException {
		Interval intvl = new Interval(6000, 8000);
		Point2D point = mock(Point2D.class);
		Rectangle rect = mock(Rectangle.class);
		@SuppressWarnings("unused")
		GenericEvent event = new GenericEvent(0, intvl, point, rect, null, EventType.ACTIVE_REGION);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnNullType() throws IllegalArgumentException {
		Interval intvl = new Interval(6000, 8000);
		Point2D point = mock(Point2D.class);
		Rectangle rect = mock(Rectangle.class);
		Polygon poly = mock(Polygon.class);
		@SuppressWarnings("unused")
		GenericEvent event = new GenericEvent(0, intvl, point, rect, poly, null);
	}

	@Test
	public void testIdIsSet() throws Exception {
		Interval intvl = new Interval(6000, 8000);
		Point2D point = mock(Point2D.class);
		Rectangle rect = mock(Rectangle.class);
		Polygon poly = mock(Polygon.class);
		int id = 1;
		GenericEvent event = new GenericEvent(id, intvl, point, rect, poly, EventType.ACTIVE_REGION);
		assertTrue(event.getId() == id);
	}

	@Test
	public void testLocationIsSet() throws Exception {
		Interval intvl = new Interval(6000, 8000);
		Point2D point = mock(Point2D.class);
		Rectangle rect = mock(Rectangle.class);
		Polygon poly = mock(Polygon.class);
		int id = 1;
		GenericEvent event = new GenericEvent(id, intvl, point, rect, poly, EventType.ACTIVE_REGION);
		assertEquals(point, event.getLocation());
	}

	@Test
	public void testBBoxSet() throws Exception {
		Interval intvl = new Interval(6000, 8000);
		Point2D point = mock(Point2D.class);
		Rectangle rect = mock(Rectangle.class);
		Polygon poly = mock(Polygon.class);
		int id = 1;
		GenericEvent event = new GenericEvent(id, intvl, point, rect, poly, EventType.ACTIVE_REGION);
		assertEquals(rect, event.getBBox());
	}

	@Test
	public void testShapeSet() throws Exception {
		Interval intvl = new Interval(6000, 8000);
		Point2D point = mock(Point2D.class);
		Rectangle rect = mock(Rectangle.class);
		Polygon poly = mock(Polygon.class);
		int id = 1;
		GenericEvent event = new GenericEvent(id, intvl, point, rect, poly, EventType.ACTIVE_REGION);
		assertEquals(poly, event.getShape());
	}

	@Test
	public void testIntervalSet() throws Exception {
		Interval intvl = new Interval(6000, 8000);
		Point2D point = mock(Point2D.class);
		Rectangle rect = mock(Rectangle.class);
		Polygon poly = mock(Polygon.class);
		int id = 1;
		GenericEvent event = new GenericEvent(id, intvl, point, rect, poly, EventType.ACTIVE_REGION);
		assertEquals(intvl, event.getTimePeriod());
	}

	@Test
	public void testUpdateTimePeriod() throws Exception {
		Interval intvl = new Interval(6000, 8000);
		Point2D point = mock(Point2D.class);
		Rectangle rect = mock(Rectangle.class);
		Polygon poly = mock(Polygon.class);
		int id = 1;
		GenericEvent event = new GenericEvent(id, intvl, point, rect, poly, EventType.ACTIVE_REGION);
		Interval intvl2 = new Interval(8000, 9000);
		event.updateTimePeriod(intvl2);
		assertEquals(intvl2, event.getTimePeriod());
	}

	@Test
	public void testTypeSet() throws Exception {
		Interval intvl = new Interval(6000, 8000);
		Point2D point = mock(Point2D.class);
		Rectangle rect = mock(Rectangle.class);
		Polygon poly = mock(Polygon.class);
		int id = 1;
		GenericEvent event = new GenericEvent(id, intvl, point, rect, poly, EventType.ACTIVE_REGION);
		assertEquals(EventType.ACTIVE_REGION, event.getType());
	}
	
	@Test
	public void testTypeSet2() throws Exception {
		Interval intvl = new Interval(6000, 8000);
		Point2D point = mock(Point2D.class);
		Rectangle rect = mock(Rectangle.class);
		Polygon poly = mock(Polygon.class);
		int id = 1;
		GenericEvent event = new GenericEvent(id, intvl, point, rect, poly, EventType.CORONAL_HOLE);
		assertEquals(EventType.CORONAL_HOLE, event.getType());
	}

	

	@Test
	public void testGetUUID() throws Exception {
		Interval intvl = new Interval(6000, 8000);
		Point2D point = mock(Point2D.class);
		Rectangle rect = mock(Rectangle.class);
		Polygon poly = mock(Polygon.class);
		int id = 1;
		GenericEvent event = new GenericEvent(id, intvl, point, rect, poly, EventType.CORONAL_HOLE);
		assertNotNull("UUID is null",event.getUUID());
	}

	
}