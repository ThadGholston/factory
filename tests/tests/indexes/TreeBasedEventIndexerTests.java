package tests.indexes;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.factory.interfaces.IIndexFactory;
import edu.gsu.dmlab.indexes.BasicEventIndexer;
import edu.gsu.dmlab.indexes.TreeBasedEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.AbsMatIndexer;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by thad on 11/20/15.
 */
public class TreeBasedEventIndexerTests {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorCallsThrowsOnNullList()
            throws IllegalArgumentException {
        Duration dur = new Duration(0, 2000);
        IIndexFactory factory = mock(IIndexFactory.class);
        @SuppressWarnings("unused")
        TreeBasedEventIndexer idxr = new TreeBasedEventIndexer(null, 1, 1, dur);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorThrowsOnDimLessThanOne()
            throws IllegalArgumentException {
        @SuppressWarnings("unchecked")
        ArrayList<IEvent> lst = (ArrayList<IEvent>) mock(ArrayList.class);
        Duration duration = new Duration(0, 2000);

        @SuppressWarnings("unused")
        TreeBasedEventIndexer idxr = new TreeBasedEventIndexer(lst, 0, 1, duration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorThrowsOnDivLessThanOne()
            throws IllegalArgumentException {
        @SuppressWarnings("unchecked")
        ArrayList<IEvent> lst = (ArrayList<IEvent>) mock(ArrayList.class);
        Duration duration = new Duration(0, 2000);

        @SuppressWarnings("unused")
        TreeBasedEventIndexer idxr = new TreeBasedEventIndexer(lst, 1, 0, duration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorThrowsOnNullFrameSpan()
            throws IllegalArgumentException {
        @SuppressWarnings("unchecked")
        ArrayList<IEvent> lst = (ArrayList<IEvent>) mock(ArrayList.class);

        @SuppressWarnings("unused")
        TreeBasedEventIndexer idxr = new TreeBasedEventIndexer(lst, 1, 01, null);
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

    @SuppressWarnings("unchecked")
    @Test
    public void testTreeBasedEventSearchDoesNotReturnWhenNotIntersectingSpatial()
            throws IllegalArgumentException {
        ArrayList<IEvent> lst = new ArrayList<IEvent>();

        Duration dur = new Duration(0, 2000);
        // add event to index
        IEvent obj = mock(IEvent.class);
        lst.add(obj);

        // add interval to the object
        Interval itvl = new Interval(0, 5000);
        when(obj.getTimePeriod()).thenReturn(itvl);
        when(obj.getUUID()).thenReturn(new UUID(4, 2));
        Rectangle rect = new Rectangle(1, 1, 2, 2);
        when(obj.getBBox()).thenReturn(rect);
        int[] xArr = {1, 2, 2, 1};
        int[] yArr = {1, 1, 3, 3};
        Polygon geom = new Polygon(xArr, yArr, xArr.length);
        when(obj.getShape()).thenReturn(geom);
        int regionDim = 4;
        int regionDiv = 1;
        TreeBasedEventIndexer idxr = new TreeBasedEventIndexer(lst, regionDim, regionDiv, dur);
        int[] xArr2 = {13, 14, 14, 13};
        int[] yArr2 = {13, 13, 14, 14};
        Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
        ArrayList<IEvent> retList = idxr
                .search(itvl, poly);
        System.out.println("testTreeBasedEventSearchDoesNotReturnWhenNotIntersectingSpatial: " + retList.size());
        assertTrue(retList.size() == 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTreeBasedSearchDoesReturnWhenIntersectingSpatialTemporal()
            throws IllegalArgumentException {
        ArrayList<IEvent> lst = new ArrayList<IEvent>();

        Duration dur = new Duration(2000);

        // add event to index
        IEvent obj = mock(IEvent.class);
        lst.add(obj);

        // add interval to the object
        Interval itvl = new Interval(0, 5000);
        when(obj.getTimePeriod()).thenReturn(itvl);
        when(obj.getUUID()).thenReturn(new UUID(4, 2));
        Rectangle rect = new Rectangle(1, 1, 2, 2);
        when(obj.getBBox()).thenReturn(rect);
        int[] xArr = {1, 3, 3, 1};
        int[] yArr = {1, 1, 3, 3};
        Polygon geom = new Polygon(xArr, yArr, xArr.length);
        when(obj.getShape()).thenReturn(geom);

        int regionDim = 4;
        int regionDiv = 1;

        TreeBasedEventIndexer idxr = new TreeBasedEventIndexer(lst, regionDim, regionDiv, dur);
        int[] xArr2 = {1, 2, 2, 1};
        int[] yArr2 = {1, 1, 2, 2};
        Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
        ArrayList<IEvent> retList = idxr
                .search(itvl, poly);
//        System.out.println("testTreeBasedSearchDoesReturnWhenIntersectingSpatialTemporal: " + retList.size());
        assertTrue(retList.size() == 1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTreeBasedEventSearchDoesNotReturnWhenIntersectingSpatialButNotTemporal()
            throws IllegalArgumentException {
        ArrayList<IEvent> lst = new ArrayList<IEvent>();

        Duration dur = new Duration(2000);
        // add event to index
        IEvent obj = mock(IEvent.class);
        lst.add(obj);

        // add interval to the object
        Interval itvl = new Interval(0, 5000);
        when(obj.getTimePeriod()).thenReturn(itvl);
        when(obj.getUUID()).thenReturn(new UUID(4, 2));
        Rectangle rect = new Rectangle(1, 1, 2, 2);
        when(obj.getBBox()).thenReturn(rect);
        int[] xArr = {1, 3, 3, 1};
        int[] yArr = {1, 1, 3, 3};
        Polygon geom = new Polygon(xArr, yArr, xArr.length);
        when(obj.getShape()).thenReturn(geom);

        int regionDim = 4;
        int regionDiv = 1;
        TreeBasedEventIndexer idxr = new TreeBasedEventIndexer(lst, regionDim, regionDiv, dur);
        Interval itvl2 = new Interval(6000, 7000);
        int[] xArr2 = {1, 2, 2, 1};
        int[] yArr2 = {1, 1, 2, 2};
        Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
        ArrayList<IEvent> retList = idxr
                .search(itvl2, poly);
//        System.out.println("testTreeBasedEventSearchDoesNotReturnWhenIntersectingSpatialButNotTemporal: " +  retList.size());
        assertTrue(retList.size() == 0);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTreeBasedEventSearchDoesNotReturnWhenNotIntersectingTemporal()
            throws IllegalArgumentException {
        ArrayList<IEvent> lst = new ArrayList<IEvent>();

        Duration dur = new Duration(2000);
        // add event to index
        IEvent obj = mock(IEvent.class);
        lst.add(obj);

        // add interval to the object
        Interval itvl = new Interval(0, 5000);
        when(obj.getTimePeriod()).thenReturn(itvl);
        when(obj.getUUID()).thenReturn(new UUID(4, 2));
        Rectangle rect = new Rectangle(1, 1, 2, 2);
        when(obj.getBBox()).thenReturn(rect);
        int[] xArr = {1, 3, 3, 1};
        int[] yArr = {1, 1, 3, 3};
        Polygon geom = new Polygon(xArr, yArr, xArr.length);
        when(obj.getShape()).thenReturn(geom);

        int regionDim = 4;
        int regionDiv = 1;
        TreeBasedEventIndexer idxr = new TreeBasedEventIndexer(lst, regionDim, regionDiv, dur);

        Interval itvl2 = new Interval(6000, 7000);
        ArrayList<IEvent> retList = idxr.search(itvl2);
//        System.out.println("testTreeBasedEventSearchDoesNoetReturnWhenNotIntersectingTemporal: " + retList.size());
        assertTrue(retList.size() == 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTreeBasedEventSearchDoesReturnWhenIntersectingTemporal()
            throws IllegalArgumentException {
        ArrayList<IEvent> lst = new ArrayList<IEvent>();

        Duration dur = new Duration(2000);

        // add event to index
        IEvent obj = mock(IEvent.class);
        lst.add(obj);

        // add interval to the object
        Interval itvl = new Interval(0, 5000);
        when(obj.getTimePeriod()).thenReturn(itvl);
        when(obj.getUUID()).thenReturn(new UUID(4, 2));
        Rectangle rect = new Rectangle(1, 1, 2, 2);
        when(obj.getBBox()).thenReturn(rect);
        int[] xArr = {1, 3, 3, 1};
        int[] yArr = {1, 1, 3, 3};
        Polygon geom = new Polygon(xArr, yArr, xArr.length);
        when(obj.getShape()).thenReturn(geom);

        int regionDim = 4;
        int regionDiv = 1;
        TreeBasedEventIndexer idxr = new TreeBasedEventIndexer(lst, regionDim, regionDiv, dur);

        ArrayList<IEvent> retList = idxr.search(itvl);
//        System.out.println("testTreeBasedEventSearchDoesReturnWhenIntersectingTemporal: " + retList.size());
        assertTrue(retList.size() == 1);

    }

}
