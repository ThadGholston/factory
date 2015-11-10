package tests.indexes;

import static org.mockito.Mockito.mock;
import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.factory.interfaces.IIndexFactory;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.indexes.BasicTrackIndexer;
import edu.gsu.dmlab.indexes.interfaces.AbsMatIndexer;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;

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
		BasicTrackIndexer idxr = new BasicTrackIndexer(null, 1,1, factory);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnFactory() throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<ITrack> lst = (ArrayList<ITrack>) mock(ArrayList.class);
		@SuppressWarnings("unused")
		BasicTrackIndexer idxr = new BasicTrackIndexer(lst, 1,1, null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorCallsSuperWhichThrowsOnDimLessThanOne() throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<ITrack> lst = (ArrayList<ITrack>) mock(ArrayList.class);
		IIndexFactory factory = mock(IIndexFactory.class);
		
		@SuppressWarnings("unused")
		BasicTrackIndexer idxr = new BasicTrackIndexer(lst, 0,1, factory);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorCallsSuperWhichThrowsOnDivLessThanOne() throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<ITrack> lst = (ArrayList<ITrack>) mock(ArrayList.class);
		IIndexFactory factory = mock(IIndexFactory.class);
		
		@SuppressWarnings("unused")
		BasicTrackIndexer idxr = new BasicTrackIndexer(lst, 1,0, factory);
	}
	
	@Test
	public void testFilterOnIntervalAndLocationDoesNotReturnWhenNotIntersectingSpatial()
			throws IllegalArgumentException {
		
	}
}
