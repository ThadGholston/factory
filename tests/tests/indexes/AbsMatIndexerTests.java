
package tests.indexes;

import static org.junit.Assert.*;

import org.junit.Test;

import static org.mockito.Mockito.*;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.Interval;

import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.geometry.Rectangle2D;
import edu.gsu.dmlab.indexes.interfaces.AbsMatIndexer;

/**
 *
 * @author Dustin Kempton
 * 
 *
 */

public class AbsMatIndexerTests {
	
	protected class FakeAbsMatIndexer extends AbsMatIndexer<IBaseDataType>{

		public FakeAbsMatIndexer(ArrayList<IBaseDataType> objectList, int regionDimension, int regionDiv) throws IllegalArgumentException {
			super(objectList, regionDimension, regionDiv);
			
		}

		public void setSearchSpace(Rectangle2D rect, IBaseDataType data){
			for(int x = (int) rect.x; x<rect.x+rect.width;x++){
				for(int y = (int)rect.y;y<rect.y+rect.height;y++){
					super.searchSpace[x][y].add(data);
				}
			}
		}
		@Override
		protected void buildIndex() {
			// Auto-generated method stub
			
		}

		@Override
		public ArrayList<IBaseDataType> search(Interval timePeriod) {
			return null;
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnNullList() throws IllegalArgumentException {
		@SuppressWarnings("unused")
		AbsMatIndexer<IBaseDataType> idxr = new FakeAbsMatIndexer(null, 1,1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnDimLessThanOne() throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IBaseDataType> lst = (ArrayList<IBaseDataType>)mock(ArrayList.class);
		@SuppressWarnings("unused")
		AbsMatIndexer<IBaseDataType> idxr = new FakeAbsMatIndexer(lst, 0,1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnDivLessThanOne() throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IBaseDataType> lst = (ArrayList<IBaseDataType>)mock(ArrayList.class);
		@SuppressWarnings("unused")
		AbsMatIndexer<IBaseDataType> idxr = new FakeAbsMatIndexer(lst, 1,0);
	}
	
	@Test
	public void testGetFirstTime() throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IBaseDataType> lst = (ArrayList<IBaseDataType>)mock(ArrayList.class);
		
		AbsMatIndexer<IBaseDataType> idxr = new FakeAbsMatIndexer(lst, 1,1);
		
		IBaseDataType obj = mock(IBaseDataType.class);
		when(lst.get(0)).thenReturn(obj);

		Interval itvl = new Interval (0, 500);
		when(obj.getTimePeriod()).thenReturn(itvl);
		
		idxr.getFirstTime();
		verify(lst, times(1)).get(0);
	}
	
	@Test
	public void testGetLastTime() throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IBaseDataType> lst = (ArrayList<IBaseDataType>)mock(ArrayList.class);
		
		AbsMatIndexer<IBaseDataType> idxr = new FakeAbsMatIndexer(lst, 1,1);
		
		IBaseDataType obj = mock(IBaseDataType.class);
		when(lst.get(0)).thenReturn(obj);
		when(lst.size()).thenReturn(1);

		Interval itvl = new Interval (0, 500);
		when(obj.getTimePeriod()).thenReturn(itvl);
		
		idxr.getFirstTime();
		verify(lst, times(1)).get(0);
		
	}

	@Test
	public void testGetAll() throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IBaseDataType> lst = (ArrayList<IBaseDataType>)mock(ArrayList.class);
		
		AbsMatIndexer<IBaseDataType> idxr = new FakeAbsMatIndexer(lst, 1,1);	
		assertEquals(lst, idxr.getAll());
		
	}
	
	@Test
	public void testSearchReturnWhenIntersects() throws IllegalArgumentException {
		ArrayList<IBaseDataType> lst = new ArrayList<IBaseDataType>();
		
		FakeAbsMatIndexer idxr = new FakeAbsMatIndexer(lst, 4,1);
		
		IBaseDataType obj = mock(IBaseDataType.class);
		when(obj.getUUID()).thenReturn(new UUID(4,2));
		
		Interval itvl = new Interval (0, 500);
		when(obj.getTimePeriod()).thenReturn(itvl);
		
		Rectangle2D rect = new Rectangle2D(1,1,2,2);
		int[] xArr2 = {1,3,3,1};
		int[] yArr2 = {1,1,3,3};
		Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
		
		
		//adds to index so we can get it back out
		idxr.setSearchSpace(rect, obj);
		
		
		ArrayList<IBaseDataType> retList = idxr.search(itvl, poly);
		assertTrue(retList.size()==1);
		
	}
	
	@Test
	public void testSearchDoesNotReturnWhenNotIntersectingSpatial() throws IllegalArgumentException {
		ArrayList<IBaseDataType> lst = new ArrayList<IBaseDataType>();
		
		FakeAbsMatIndexer idxr = new FakeAbsMatIndexer(lst, 4,1);
		
		IBaseDataType obj = mock(IBaseDataType.class);
		when(obj.getUUID()).thenReturn(new UUID(4,2));
		
		Interval itvl = new Interval (0, 500);
		when(obj.getTimePeriod()).thenReturn(itvl);
		
		Rectangle2D rect = new Rectangle2D(1,1,2,2);
		
		//adds to index so we can get it back out
		idxr.setSearchSpace(rect, obj);
		
		int[] xArr2 = {3,4,4,3};
		int[] yArr2 = {3,3,4,4};
		Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
		ArrayList<IBaseDataType> retList = idxr.search(itvl, poly);
		assertTrue(retList.size()==0);
		
	}
	
	@Test
	public void testSearchDoesNotReturnWhenNotIntersectingTemporal() throws IllegalArgumentException {
		ArrayList<IBaseDataType> lst = new ArrayList<IBaseDataType>();
		
		FakeAbsMatIndexer idxr = new FakeAbsMatIndexer(lst, 4,1);
		
		IBaseDataType obj = mock(IBaseDataType.class);
		when(obj.getUUID()).thenReturn(new UUID(4,2));
		
		Interval itvl = new Interval (0, 500);
		when(obj.getTimePeriod()).thenReturn(itvl);
		
		Rectangle2D rect = new Rectangle2D(1,1,2,2);
		
		//adds to index so we can get it back out
		idxr.setSearchSpace(rect, obj);
		
		Interval itvl2 = new Interval (600, 700);
		int[] xArr2 = {1,3,3,1};
		int[] yArr2 = {1,1,3,3};
		Polygon poly = new Polygon(xArr2, yArr2, xArr2.length);
		ArrayList<IBaseDataType> retList = idxr.search(itvl2, poly);
		assertTrue(retList.size()==0);
		
	}
}
