package tests.indexes;

import static org.junit.Assert.*;

import org.junit.Test;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.RecursiveAction;

import org.joda.time.Duration;
import org.joda.time.Interval;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.factory.interfaces.IIndexFactory;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.geometry.Rectangle2D;
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
	public void testFilterOnIntervalAndLocationDoesNotReturnWhenNotIntersectingSpatial()
			throws IllegalArgumentException {
		ArrayList<IEvent> lst = new ArrayList<IEvent>();

		Duration dur = new Duration(0, 2000);

		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(
				factory.getBaseObjectAreaSort(any(ArrayList[][].class),
						anyInt(), anyInt(), anyInt())).thenReturn(rs);

		// add event to index
		IEvent obj = mock(IEvent.class);
		lst.add(obj);

		// add interval to the object
		Interval itvl = new Interval(0, 5000);
		when(obj.getTimePeriod()).thenReturn(itvl);
		when(obj.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle2D rect = new Rectangle2D(1, 1, 2, 2);
		when(obj.getBBox()).thenReturn(rect);
		Point2D[] geom = { new Point2D(1, 1), new Point2D(3, 1),
				new Point2D(3, 3), new Point2D(1, 3) };
		when(obj.getShape()).thenReturn(geom);
		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, regionDim,
				regionDiv, dur, factory);

		Rectangle2D rect2 = new Rectangle2D(3, 3, 1, 1);
		ArrayList<IEvent> retList = idxr.filterOnIntervalAndLocation(itvl,
				rect2);
		assertTrue(retList.size() == 0);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFilterOnIntervalAndLocationDoesReturnWhenIntersectingSpatialTemporal()
			throws IllegalArgumentException {
		ArrayList<IEvent> lst = new ArrayList<IEvent>();

		Duration dur = new Duration(2000);

		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(
				factory.getBaseObjectAreaSort(any(ArrayList[][].class),
						anyInt(), anyInt(), anyInt())).thenReturn(rs);

		// add event to index
		IEvent obj = mock(IEvent.class);
		lst.add(obj);

		// add interval to the object
		Interval itvl = new Interval(0, 5000);
		when(obj.getTimePeriod()).thenReturn(itvl);
		when(obj.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle2D rect = new Rectangle2D(1, 1, 2, 2);
		when(obj.getBBox()).thenReturn(rect);
		Point2D[] geom = { new Point2D(1, 1), new Point2D(3, 1),
				new Point2D(3, 3), new Point2D(1, 3) };
		when(obj.getShape()).thenReturn(geom);

		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, regionDim,
				regionDiv, dur, factory);

		Rectangle2D rect2 = new Rectangle2D(1, 1, 1, 1);
		ArrayList<IEvent> retList = idxr.filterOnIntervalAndLocation(itvl,
				rect2);
		assertTrue(retList.size() == 1);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFilterOnIntervalAndLocationDoesNotReturnWhenIntersectingSpatialButNotTemporal()
			throws IllegalArgumentException {
		ArrayList<IEvent> lst = new ArrayList<IEvent>();

		Duration dur = new Duration(2000);

		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(
				factory.getBaseObjectAreaSort(any(ArrayList[][].class),
						anyInt(), anyInt(), anyInt())).thenReturn(rs);

		// add event to index
		IEvent obj = mock(IEvent.class);
		lst.add(obj);

		// add interval to the object
		Interval itvl = new Interval(0, 5000);
		when(obj.getTimePeriod()).thenReturn(itvl);
		when(obj.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle2D rect = new Rectangle2D(1, 1, 2, 2);
		when(obj.getBBox()).thenReturn(rect);
		Point2D[] geom = { new Point2D(1, 1), new Point2D(3, 1),
				new Point2D(3, 3), new Point2D(1, 3) };
		when(obj.getShape()).thenReturn(geom);

		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, regionDim,
				regionDiv, dur, factory);

		Rectangle2D rect2 = new Rectangle2D(1, 1, 1, 1);
		Interval itvl2 = new Interval(6000, 7000);
		ArrayList<IEvent> retList = idxr.filterOnIntervalAndLocation(itvl2,
				rect2);
		assertTrue(retList.size() == 0);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFilterOnIntervalDoesNotReturnWhenNotIntersectingTemporal()
			throws IllegalArgumentException {
		ArrayList<IEvent> lst = new ArrayList<IEvent>();

		Duration dur = new Duration(2000);

		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(
				factory.getBaseObjectAreaSort(any(ArrayList[][].class),
						anyInt(), anyInt(), anyInt())).thenReturn(rs);

		// add event to index
		IEvent obj = mock(IEvent.class);
		lst.add(obj);

		// add interval to the object
		Interval itvl = new Interval(0, 5000);
		when(obj.getTimePeriod()).thenReturn(itvl);
		when(obj.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle2D rect = new Rectangle2D(1, 1, 2, 2);
		when(obj.getBBox()).thenReturn(rect);
		Point2D[] geom = { new Point2D(1, 1), new Point2D(3, 1),
				new Point2D(3, 3), new Point2D(1, 3) };
		when(obj.getShape()).thenReturn(geom);

		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, regionDim,
				regionDiv, dur, factory);

		Interval itvl2 = new Interval(6000, 7000);
		ArrayList<IEvent> retList = idxr.filterOnInterval(itvl2);
		assertTrue(retList.size() == 0);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFilterOnIntervalDoesReturnWhenIntersectingTemporal()
			throws IllegalArgumentException {
		ArrayList<IEvent> lst = new ArrayList<IEvent>();

		Duration dur = new Duration(2000);

		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(
				factory.getBaseObjectAreaSort(any(ArrayList[][].class),
						anyInt(), anyInt(), anyInt())).thenReturn(rs);

		// add event to index
		IEvent obj = mock(IEvent.class);
		lst.add(obj);

		// add interval to the object
		Interval itvl = new Interval(0, 5000);
		when(obj.getTimePeriod()).thenReturn(itvl);
		when(obj.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle2D rect = new Rectangle2D(1, 1, 2, 2);
		when(obj.getBBox()).thenReturn(rect);
		Point2D[] geom = { new Point2D(1, 1), new Point2D(3, 1),
				new Point2D(3, 3), new Point2D(1, 3) };
		when(obj.getShape()).thenReturn(geom);

		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<IEvent> idxr = new BasicEventIndexer(lst, regionDim,
				regionDiv, dur, factory);

		ArrayList<IEvent> retList = idxr.filterOnInterval(itvl);
		assertTrue(retList.size() == 1);

	}

}
