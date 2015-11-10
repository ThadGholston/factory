package tests.indexes;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.factory.interfaces.IIndexFactory;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.indexes.BasicEventIndexer;
import edu.gsu.dmlab.indexes.BasicTrackIndexer;
import edu.gsu.dmlab.indexes.interfaces.AbsMatIndexer;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.RecursiveAction;

/**
 * @author Dustin Kempton
 * @version 11/09/2015
 * @Owner Data Mining Lab, Georgia State University
 *
 */

public class TrackIndexerTests {

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorCallsSuperWhichThrowsOnNullList() throws IllegalArgumentException {
		IIndexFactory factory = mock(IIndexFactory.class);
		@SuppressWarnings("unused")
		BasicTrackIndexer idxr = new BasicTrackIndexer(null, 1, 1, factory);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnFactory() throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<ITrack> lst = (ArrayList<ITrack>) mock(ArrayList.class);
		@SuppressWarnings("unused")
		BasicTrackIndexer idxr = new BasicTrackIndexer(lst, 1, 1, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorCallsSuperWhichThrowsOnDimLessThanOne() throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<ITrack> lst = (ArrayList<ITrack>) mock(ArrayList.class);
		IIndexFactory factory = mock(IIndexFactory.class);

		@SuppressWarnings("unused")
		BasicTrackIndexer idxr = new BasicTrackIndexer(lst, 0, 1, factory);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorCallsSuperWhichThrowsOnDivLessThanOne() throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<ITrack> lst = (ArrayList<ITrack>) mock(ArrayList.class);
		IIndexFactory factory = mock(IIndexFactory.class);

		@SuppressWarnings("unused")
		BasicTrackIndexer idxr = new BasicTrackIndexer(lst, 1, 0, factory);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFilterOnIntervalAndLocationDoesNotReturnWhenNotIntersectingSpatialAR()
			throws IllegalArgumentException {

		// list for input to constructor of track indexer
		ArrayList<ITrack> lst = new ArrayList<ITrack>();

		Duration dur = new Duration(0, 2000);

		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(factory.getBaseObjectAreaSort(any(ArrayList[][].class), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(rs);

		// create track to index
		ITrack trk = mock(ITrack.class);
		IEvent evnt = mock(IEvent.class);
		lst.add(trk);
		when(trk.getType()).thenReturn(EventType.ACTIVE_REGION);
		when(trk.getFirst()).thenReturn(evnt);
		// event for the track

		// add interval to the event
		Interval itvl = new Interval(0, 5000);
		when(evnt.getTimePeriod()).thenReturn(itvl);
		when(trk.getTimePeriod()).thenReturn(itvl);
		when(evnt.getUUID()).thenReturn(new UUID(4, 2));
		when(trk.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle rect = new Rectangle(1, 1, 2, 2);
		when(evnt.getBBox()).thenReturn(rect);
		int[] xArr = { 1, 2, 2, 1 };
		int[] yArr = { 1, 1, 3, 3 };
		Polygon geom = new Polygon(xArr, yArr, xArr.length);
		when(evnt.getShape()).thenReturn(geom);
		int regionDim = 5;
		int regionDiv = 1;
		AbsMatIndexer<ITrack> idxr = new BasicTrackIndexer(lst, regionDim, regionDiv, factory);

		int[] xArr2 = { 4, 5, 5, 4 };
		int[] yArr2 = { 4, 4, 5, 5 };
		Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
		ArrayList<ITrack> retList = idxr.filterOnIntervalAndLocation(itvl, poly);
		assertTrue(retList.size() == 0);

	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testFilterOnIntervalAndLocationDoesNotReturnWhenNotIntersectingSpatialSG()
			throws IllegalArgumentException {

		// list for input to constructor of track indexer
		ArrayList<ITrack> lst = new ArrayList<ITrack>();

		Duration dur = new Duration(0, 2000);

		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(factory.getBaseObjectAreaSort(any(ArrayList[][].class), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(rs);

		// create track to index
		ITrack trk = mock(ITrack.class);
		IEvent evnt = mock(IEvent.class);
		lst.add(trk);
		when(trk.getType()).thenReturn(EventType.SIGMOID);
		when(trk.getFirst()).thenReturn(evnt);
		// event for the track

		// add interval to the event
		Interval itvl = new Interval(0, 5000);
		when(evnt.getTimePeriod()).thenReturn(itvl);
		when(trk.getTimePeriod()).thenReturn(itvl);
		when(evnt.getUUID()).thenReturn(new UUID(4, 2));
		when(trk.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle rect = new Rectangle(1, 1, 2, 2);
		when(evnt.getBBox()).thenReturn(rect);
		int[] xArr = { 1, 2, 2, 1 };
		int[] yArr = { 1, 1, 3, 3 };
		Polygon geom = new Polygon(xArr, yArr, xArr.length);
		when(evnt.getShape()).thenReturn(geom);
		int regionDim = 5;
		int regionDiv = 1;
		AbsMatIndexer<ITrack> idxr = new BasicTrackIndexer(lst, regionDim, regionDiv, factory);

		int[] xArr2 = { 4, 5, 5, 4 };
		int[] yArr2 = { 4, 4, 5, 5 };
		Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
		ArrayList<ITrack> retList = idxr.filterOnIntervalAndLocation(itvl, poly);
		assertTrue(retList.size() == 0);

	}
	
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testFilterOnIntervalAndLocationDoesReturnWhenIntersectingSpatialTemporalAR()
			throws IllegalArgumentException {
		
		// list for input to constructor of track indexer
		ArrayList<ITrack> lst = new ArrayList<ITrack>();
		
		
		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(factory.getBaseObjectAreaSort(any(ArrayList[][].class), anyInt(), anyInt(), anyInt(), anyInt()))
		.thenReturn(rs);
		
		// create track to index
		ITrack trk = mock(ITrack.class);
		IEvent evnt = mock(IEvent.class);
		lst.add(trk);
		when(trk.getType()).thenReturn(EventType.ACTIVE_REGION);
		when(trk.getFirst()).thenReturn(evnt);
		// event for the track
		
		// add interval to the event
		Interval itvl = new Interval(0, 5000);
		when(evnt.getTimePeriod()).thenReturn(itvl);
		when(trk.getTimePeriod()).thenReturn(itvl);
		when(evnt.getUUID()).thenReturn(new UUID(4, 2));
		when(trk.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle rect = new Rectangle(1, 1, 2, 2);
		when(evnt.getBBox()).thenReturn(rect);
		int[] xArr = { 1, 2, 2, 1 };
		int[] yArr = { 1, 1, 3, 3 };
		Polygon geom = new Polygon(xArr, yArr, xArr.length);
		when(evnt.getShape()).thenReturn(geom);
		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<ITrack> idxr = new BasicTrackIndexer(lst, regionDim, regionDiv, factory);
		
		int[] xArr2 = {1,2,2,1};
		int[] yArr2 = {1,1,2,2};
		Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
		ArrayList<ITrack> retList = idxr.filterOnIntervalAndLocation(itvl, poly);
		assertTrue(retList.size() == 1);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testFilterOnIntervalAndLocationDoesReturnWhenIntersectingSpatialTemporalSG()
			throws IllegalArgumentException {

		// list for input to constructor of track indexer
		ArrayList<ITrack> lst = new ArrayList<ITrack>();


		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(factory.getBaseObjectAreaSort(any(ArrayList[][].class), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(rs);

		// create track to index
		ITrack trk = mock(ITrack.class);
		IEvent evnt = mock(IEvent.class);
		lst.add(trk);
		when(trk.getType()).thenReturn(EventType.SIGMOID);
		when(trk.getFirst()).thenReturn(evnt);
		// event for the track

		// add interval to the event
		Interval itvl = new Interval(0, 5000);
		when(evnt.getTimePeriod()).thenReturn(itvl);
		when(trk.getTimePeriod()).thenReturn(itvl);
		when(evnt.getUUID()).thenReturn(new UUID(4, 2));
		when(trk.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle rect = new Rectangle(1, 1, 2, 2);
		when(evnt.getBBox()).thenReturn(rect);
		int[] xArr = { 1, 2, 2, 1 };
		int[] yArr = { 1, 1, 3, 3 };
		Polygon geom = new Polygon(xArr, yArr, xArr.length);
		when(evnt.getShape()).thenReturn(geom);
		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<ITrack> idxr = new BasicTrackIndexer(lst, regionDim, regionDiv, factory);

		
		int[] xArr2 = {1,2,2,1};
		int[] yArr2 = {1,1,2,2};
		Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
		ArrayList<ITrack> retList = idxr.filterOnIntervalAndLocation(itvl, poly);
		assertTrue(retList.size() == 1);

	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testFilterOnIntervalAndLocationDoesNotReturnWhenIntersectingSpatialButNotTemporalAR()
			throws IllegalArgumentException {

		// list for input to constructor of track indexer
		ArrayList<ITrack> lst = new ArrayList<ITrack>();


		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(factory.getBaseObjectAreaSort(any(ArrayList[][].class), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(rs);

		// create track to index
		ITrack trk = mock(ITrack.class);
		IEvent evnt = mock(IEvent.class);
		lst.add(trk);
		when(trk.getType()).thenReturn(EventType.ACTIVE_REGION);
		when(trk.getFirst()).thenReturn(evnt);
		// event for the track

		// add interval to the event
		Interval itvl = new Interval(0, 5000);
		when(evnt.getTimePeriod()).thenReturn(itvl);
		when(trk.getTimePeriod()).thenReturn(itvl);
		when(evnt.getUUID()).thenReturn(new UUID(4, 2));
		when(trk.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle rect = new Rectangle(1, 1, 2, 2);
		when(evnt.getBBox()).thenReturn(rect);
		int[] xArr = { 1, 2, 2, 1 };
		int[] yArr = { 1, 1, 3, 3 };
		Polygon geom = new Polygon(xArr, yArr, xArr.length);
		when(evnt.getShape()).thenReturn(geom);
		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<ITrack> idxr = new BasicTrackIndexer(lst, regionDim, regionDiv, factory);

		Interval itvl2 = new Interval(6000, 7000);
		int[] xArr2 = {1,2,2,1};
		int[] yArr2 = {1,1,2,2};
		Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
		ArrayList<ITrack> retList = idxr.filterOnIntervalAndLocation(itvl2, poly);
		assertTrue(retList.size() == 0);

	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void testFilterOnIntervalAndLocationDoesNotReturnWhenIntersectingSpatialButNotTemporalSG()
			throws IllegalArgumentException {

		// list for input to constructor of track indexer
		ArrayList<ITrack> lst = new ArrayList<ITrack>();


		// make factory for constructor input and its return values as well
		IIndexFactory factory = mock(IIndexFactory.class);
		RecursiveAction rs = mock(RecursiveAction.class);
		when(factory.getBaseObjectAreaSort(any(ArrayList[][].class), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(rs);

		// create track to index
		ITrack trk = mock(ITrack.class);
		IEvent evnt = mock(IEvent.class);
		lst.add(trk);
		when(trk.getType()).thenReturn(EventType.SIGMOID);
		when(trk.getFirst()).thenReturn(evnt);
		// event for the track

		// add interval to the event
		Interval itvl = new Interval(0, 5000);
		when(evnt.getTimePeriod()).thenReturn(itvl);
		when(trk.getTimePeriod()).thenReturn(itvl);
		when(evnt.getUUID()).thenReturn(new UUID(4, 2));
		when(trk.getUUID()).thenReturn(new UUID(4, 2));
		Rectangle rect = new Rectangle(1, 1, 2, 2);
		when(evnt.getBBox()).thenReturn(rect);
		int[] xArr = { 1, 2, 2, 1 };
		int[] yArr = { 1, 1, 3, 3 };
		Polygon geom = new Polygon(xArr, yArr, xArr.length);
		when(evnt.getShape()).thenReturn(geom);
		int regionDim = 4;
		int regionDiv = 1;
		AbsMatIndexer<ITrack> idxr = new BasicTrackIndexer(lst, regionDim, regionDiv, factory);

		Interval itvl2 = new Interval(6000, 7000);
		int[] xArr2 = {1,2,2,1};
		int[] yArr2 = {1,1,2,2};
		Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
		ArrayList<ITrack> retList = idxr.filterOnIntervalAndLocation(itvl2, poly);
		assertTrue(retList.size() == 0);

	}
}
