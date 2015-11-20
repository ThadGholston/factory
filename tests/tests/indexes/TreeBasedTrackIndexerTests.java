package tests.indexes;

import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.factory.interfaces.IIndexFactory;
import edu.gsu.dmlab.indexes.BasicTrackIndexer;
import edu.gsu.dmlab.indexes.TreeBasedTrackIndexer;
import edu.gsu.dmlab.indexes.interfaces.AbsMatIndexer;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;
import org.w3c.dom.css.Rect;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.RecursiveAction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

/**
 * Created by thad on 11/19/15.
 */
public class TreeBasedTrackIndexerTests {

    @Test(expected = IllegalArgumentException.class)
    public void testTreeBasedConstructorThrowsOnNullList() throws IllegalArgumentException {
        TreeBasedTrackIndexer idxr = new TreeBasedTrackIndexer(null, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTreeBasedConstructorCallsSuperWhichThrowsOnDimLessThanOne() throws IllegalArgumentException {
        @SuppressWarnings("unchecked")
        ArrayList<ITrack> lst = (ArrayList<ITrack>) mock(ArrayList.class);

        @SuppressWarnings("unused")
        TreeBasedTrackIndexer idxr = new TreeBasedTrackIndexer(lst, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTreeBasedConstructorCallsSuperWhichThrowsOnDivLessThanOne() throws IllegalArgumentException {
        @SuppressWarnings("unchecked")
        ArrayList<ITrack> lst = (ArrayList<ITrack>) mock(ArrayList.class);

        @SuppressWarnings("unused")
        TreeBasedTrackIndexer idxr = new TreeBasedTrackIndexer(lst, 1, 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTreeBasedTrackSearchDoesNotReturnWhenNotIntersectingSpatialAR()
            throws IllegalArgumentException {
        // list for input to contructor of track indexer
        ArrayList<ITrack> lst = new ArrayList<>();

        Duration dur = new Duration(0, 2000);

        // create track to index
        ITrack trk = mock(ITrack.class);
        IEvent evnt = mock(IEvent.class);
        lst.add(trk);
        ;
        when(trk.getType()).thenReturn(EventType.ACTIVE_REGION);
        when(trk.getFirst()).thenReturn(evnt);

        Interval interval = new Interval(0, 5000);
        when(evnt.getTimePeriod()).thenReturn(interval);
        when(trk.getTimePeriod()).thenReturn(interval);
        when(evnt.getUUID()).thenReturn(new UUID(4, 2));
        when(trk.getUUID()).thenReturn(new UUID(4, 2));
        Rectangle rectangle = new Rectangle(1, 1, 2, 2);
        when(evnt.getBBox()).thenReturn(rectangle);
        int[] xArr = {1, 2, 2, 1};
        int[] yArr = {1, 1, 3, 3};
        Polygon geom = new Polygon(xArr, yArr, xArr.length);
        when(evnt.getShape()).thenReturn(geom);
        int regionDimension = 5;
        int regionDivisor = 1;
        TreeBasedTrackIndexer idxr = new TreeBasedTrackIndexer(lst, regionDimension, regionDivisor);
        int[] xArr2 = {4, 5, 5, 4};
        int[] yArr2 = {4, 4, 5, 5};
        Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
        ArrayList<ITrack> retList = idxr.search(interval, poly);
//        System.out.println("testTreeBasedTrackSearchDoesNotReturnWhenNotIntersectingSpatialAR: " + retList.size());
        assertTrue(retList.size() == 0);
    }

    @SuppressWarnings("unchecked")
    public void testTreeBasedTrackSearchDoesReturnWhenIntersectingSpatialTemporalAR()
            throws IllegalArgumentException {
        //list for input to constructor
        ArrayList<ITrack> lst = new ArrayList<>();

        // create track to index
        ITrack trk = mock(ITrack.class);
        IEvent evnt = mock(IEvent.class);
        lst.add(trk);

        Interval itvl = new Interval(0, 5000);
        when(evnt.getTimePeriod()).thenReturn(itvl);
        when(trk.getTimePeriod()).thenReturn(itvl);
        when(evnt.getUUID()).thenReturn(new UUID(4, 2));
        when(trk.getUUID()).thenReturn(new UUID(4, 2));
        Rectangle rect = new Rectangle(1, 1, 2, 2);
        int[] xArr = {1, 2, 2, 1};
        int[] yArr = {1, 1, 3, 3};
        Polygon geom = new Polygon(xArr, yArr, xArr.length);
        when(evnt.getShape()).thenReturn(geom);
        int regionDim = 4;
        int regionDiv = 1;
        TreeBasedTrackIndexer idxr = new TreeBasedTrackIndexer(lst, regionDim, regionDiv);

        int[] xArr2 = {1, 2, 2, 1};
        int[] yArr2 = {1, 1, 2, 2};
        Polygon polygon = new Polygon(xArr2, yArr2, xArr.length);
        ArrayList<ITrack> retList = idxr.search(itvl, polygon);
//        System.out.println("testTreeBasedTrackSearchDoesReturnWhenIntersectingSpatialTemporalAR: " + retList.size());
        assertTrue(retList.size() == 1);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTreeBasedTrackSearchDoesReturnWhenIntersectingSpatialTemporalSG()
            throws IllegalArgumentException {

        // list for input to constructor of track indexer
        ArrayList<ITrack> lst = new ArrayList<ITrack>();

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
        int[] xArr = {1, 2, 2, 1};
        int[] yArr = {1, 1, 3, 3};
        Polygon geom = new Polygon(xArr, yArr, xArr.length);
        when(evnt.getShape()).thenReturn(geom);
        int regionDim = 4;
        int regionDiv = 1;
        TreeBasedTrackIndexer idxr = new TreeBasedTrackIndexer(lst, regionDim, regionDiv);


        int[] xArr2 = {1, 2, 2, 1};
        int[] yArr2 = {1, 1, 2, 2};
        Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
        ArrayList<ITrack> retList = idxr.search(itvl, poly);
//        System.out.println("testTreeBasedTrackSearchDoesReturnWhenIntersectingSpatialTemporalSG: " + retList.size());
        assertTrue(retList.size() == 1);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTreeBasedTrackSearchDoesNotReturnWhenIntersectingSpatialButNotTemporalAR()
            throws IllegalArgumentException {

        // list for input to constructor of track indexer
        ArrayList<ITrack> lst = new ArrayList<ITrack>();

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
        int[] xArr = {1, 2, 2, 1};
        int[] yArr = {1, 1, 3, 3};
        Polygon geom = new Polygon(xArr, yArr, xArr.length);
        when(evnt.getShape()).thenReturn(geom);
        int regionDim = 4;
        int regionDiv = 1;
        TreeBasedTrackIndexer idxr = new TreeBasedTrackIndexer(lst, regionDim, regionDiv);

        Interval itvl2 = new Interval(6000, 7000);
        int[] xArr2 = {1, 2, 2, 1};
        int[] yArr2 = {1, 1, 2, 2};
        Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
        ArrayList<ITrack> retList = idxr.search(itvl2, poly);
//        System.out.println("testTreeBasedTrackSearchDoesNotReturnWhenIntersectingSpatialButNotTemporalAR: " + retList.size());
        assertTrue(retList.size() == 0);

    }


    @SuppressWarnings("unchecked")
    @Test
    public void testTreeBasedTrackSearchDoesNotReturnWhenIntersectingSpatialButNotTemporalSG()
            throws IllegalArgumentException {

        // list for input to constructor of track indexer
        ArrayList<ITrack> lst = new ArrayList<ITrack>();

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
        int[] xArr = {1, 2, 2, 1};
        int[] yArr = {1, 1, 3, 3};
        Polygon geom = new Polygon(xArr, yArr, xArr.length);
        when(evnt.getShape()).thenReturn(geom);
        int regionDim = 4;
        int regionDiv = 1;
        TreeBasedTrackIndexer idxr = new TreeBasedTrackIndexer(lst, regionDim, regionDiv);

        Interval itvl2 = new Interval(6000, 7000);
        int[] xArr2 = {1, 2, 2, 1};
        int[] yArr2 = {1, 1, 2, 2};
        Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
        ArrayList<ITrack> retList = idxr.search(itvl2, poly);
//        System.out.println("testTreeBasedTrackSearchDoesNotReturnWhenIntersectingSpatialButNotTemporalSG: " + retList.size());
        assertTrue(retList.size() == 0);
    }
}
