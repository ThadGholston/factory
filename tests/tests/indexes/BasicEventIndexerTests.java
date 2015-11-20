package tests.indexes;

import static org.junit.Assert.*;

import org.junit.Test;

import static org.mockito.Mockito.*;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.RecursiveAction;

import org.joda.time.Duration;
import org.joda.time.Interval;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.factory.interfaces.IIndexFactory;
import edu.gsu.dmlab.indexes.BasicEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.AbsMatIndexer;

/**
 * Created by Dustin on 10/28/15.
 */

public class BasicEventIndexerTests {

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorCallsSuperWhichThrowsOnNullList()
			throws IllegalArgumentException {
		Duration dur = new Duration(0, 2000);
		IIndexFactory factory = mock(IIndexFactory.class);
		@SuppressWarnings("unused")
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(null, 1, 1, dur,
				factory);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorCallsSuperWhichThrowsOnDimLessThanOne()
			throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IEvent> lst = (ArrayList<IEvent>) mock(ArrayList.class);
		Duration dur = new Duration(0, 2000);
		IIndexFactory factory = mock(IIndexFactory.class);

		@SuppressWarnings("unused")
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, 0, 1, dur,
				factory);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorCallsSuperWhichThrowsOnDivLessThanOne()
			throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IEvent> lst = (ArrayList<IEvent>) mock(ArrayList.class);
		Duration dur = new Duration(0, 2000);
		IIndexFactory factory = mock(IIndexFactory.class);

		@SuppressWarnings("unused")
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, 1, 0, dur,
				factory);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnNullFrameSpan()
			throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IEvent> lst = (ArrayList<IEvent>) mock(ArrayList.class);

		IIndexFactory factory = mock(IIndexFactory.class);

		@SuppressWarnings("unused")
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, 1, 1, null,
				factory);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnShortFrameSpan()
			throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IEvent> lst = (ArrayList<IEvent>) mock(ArrayList.class);
		Duration dur = new Duration(0, 0);
		IIndexFactory factory = mock(IIndexFactory.class);

		@SuppressWarnings("unused")
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, 1, 1, dur,
				factory);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnNullFactory()
			throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IEvent> lst = (ArrayList<IEvent>) mock(ArrayList.class);

		Duration dur = new Duration(0, 2000);

		@SuppressWarnings("unused")
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, 1, 1, dur, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBasicEventSearchDoesNotReturnWhenNotIntersectingSpatial()
			throws IllegalArgumentException {
		ArrayList<IEvent> lst = new ArrayList<IEvent>();

		Duration dur = new Duration(0, 2000);

		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(
				factory.getBaseObjectAreaSort(any(ArrayList[][].class),
						anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(rs);

		// add event to index
		IEvent obj = mock(IEvent.class);
		lst.add(obj);

		// add interval to the object
		Interval itvl = new Interval(0, 5000);
		when(obj.getTimePeriod()).thenReturn(itvl);
		when(obj.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle rect = new Rectangle(1, 1, 2, 2);
		when(obj.getBBox()).thenReturn(rect);
		int[] xArr = { 1, 2, 2, 1 };
		int[] yArr = { 1, 1, 3, 3 };
		Polygon geom = new Polygon(xArr, yArr, xArr.length);
		when(obj.getShape()).thenReturn(geom);
		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, regionDim,
				regionDiv, dur, factory);

		int[] xArr2 = { 3, 4, 4, 3 };
		int[] yArr2 = { 3, 3, 4, 4 };
		Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
		ArrayList<IEvent> retList = idxr
				.search(itvl, poly);
		assertTrue(retList.size() == 0);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBasicEventSearchDoesReturnWhenIntersectingSpatialTemporal()
			throws IllegalArgumentException {
		ArrayList<IEvent> lst = new ArrayList<IEvent>();

		Duration dur = new Duration(2000);

		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(
				factory.getBaseObjectAreaSort(any(ArrayList[][].class),
						anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(rs);

		// add event to index
		IEvent obj = mock(IEvent.class);
		lst.add(obj);

		// add interval to the object
		Interval itvl = new Interval(0, 5000);
		when(obj.getTimePeriod()).thenReturn(itvl);
		when(obj.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle rect = new Rectangle(1, 1, 2, 2);
		when(obj.getBBox()).thenReturn(rect);
		int[] xArr = { 1, 3, 3, 1 };
		int[] yArr = { 1, 1, 3, 3 };
		Polygon geom = new Polygon(xArr, yArr, xArr.length);
		when(obj.getShape()).thenReturn(geom);

		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, regionDim,
				regionDiv, dur, factory);


		int[] xArr2 = {1,2,2,1};
		int[] yArr2 = {1,1,2,2};
		Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
		ArrayList<IEvent> retList = idxr
				.search(itvl, poly);
		assertTrue(retList.size() == 1);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBasicEventSearchDoesNotReturnWhenIntersectingSpatialButNotTemporal()
			throws IllegalArgumentException {
		ArrayList<IEvent> lst = new ArrayList<IEvent>();

		Duration dur = new Duration(2000);

		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(
				factory.getBaseObjectAreaSort(any(ArrayList[][].class),
						anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(rs);

		// add event to index
		IEvent obj = mock(IEvent.class);
		lst.add(obj);

		// add interval to the object
		Interval itvl = new Interval(0, 5000);
		when(obj.getTimePeriod()).thenReturn(itvl);
		when(obj.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle rect = new Rectangle(1, 1, 2, 2);
		when(obj.getBBox()).thenReturn(rect);
		int[] xArr = { 1, 3, 3, 1 };
		int[] yArr = { 1, 1, 3, 3 };
		Polygon geom = new Polygon(xArr, yArr, xArr.length);
		when(obj.getShape()).thenReturn(geom);

		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, regionDim,
				regionDiv, dur, factory);

		Interval itvl2 = new Interval(6000, 7000);
		int[] xArr2 = {1,2,2,1};
		int[] yArr2 = {1,1,2,2};
		Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
		ArrayList<IEvent> retList = idxr
				.search(itvl2, poly);
		assertTrue(retList.size() == 0);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBasicEventSearchDoesNotReturnWhenNotIntersectingTemporal()
			throws IllegalArgumentException {
		ArrayList<IEvent> lst = new ArrayList<IEvent>();

		Duration dur = new Duration(2000);

		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(
				factory.getBaseObjectAreaSort(any(ArrayList[][].class),
						anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(rs);

		// add event to index
		IEvent obj = mock(IEvent.class);
		lst.add(obj);

		// add interval to the object
		Interval itvl = new Interval(0, 5000);
		when(obj.getTimePeriod()).thenReturn(itvl);
		when(obj.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle rect = new Rectangle(1, 1, 2, 2);
		when(obj.getBBox()).thenReturn(rect);
		int[] xArr = { 1, 3, 3, 1 };
		int[] yArr = { 1, 1, 3, 3 };
		Polygon geom = new Polygon(xArr, yArr, xArr.length);
		when(obj.getShape()).thenReturn(geom);

		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, regionDim,
				regionDiv, dur, factory);

		Interval itvl2 = new Interval(6000, 7000);
		ArrayList<IEvent> retList = idxr.search(itvl2);
		assertTrue(retList.size() == 0);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBasicEventSearchDoesReturnWhenIntersectingTemporal()
			throws IllegalArgumentException {
		ArrayList<IEvent> lst = new ArrayList<IEvent>();

		Duration dur = new Duration(2000);

		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(
				factory.getBaseObjectAreaSort(any(ArrayList[][].class),
						anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(rs);

		// add event to index
		IEvent obj = mock(IEvent.class);
		lst.add(obj);

		// add interval to the object
		Interval itvl = new Interval(0, 5000);
		when(obj.getTimePeriod()).thenReturn(itvl);
		when(obj.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle rect = new Rectangle(1, 1, 2, 2);
		when(obj.getBBox()).thenReturn(rect);
		int[] xArr = { 1, 3, 3, 1 };
		int[] yArr = { 1, 1, 3, 3 };
		Polygon geom = new Polygon(xArr, yArr, xArr.length);
		when(obj.getShape()).thenReturn(geom);

		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, regionDim,
				regionDiv, dur, factory);

		ArrayList<IEvent> retList = idxr.search(itvl);
		assertTrue(retList.size() == 1);

	}

}
