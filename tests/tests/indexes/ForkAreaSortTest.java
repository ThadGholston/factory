package tests.indexes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.Test;

import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.indexes.ForkAreaSort;

public class ForkAreaSortTest {

	@SuppressWarnings("serial")
	protected class FakeForkAreaSort extends ForkAreaSort{

		public FakeForkAreaSort(ArrayList<IBaseDataType>[][] area, int startX,
				int startY, int sizeX, int sizeY) {
			super(area, startX, startY, sizeX, sizeY);
		}
		
		public void compute(){
			super.compute();
			
		}
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsOnNullList()
			throws IllegalArgumentException {
		@SuppressWarnings("unused")
		ForkAreaSort idxr = new ForkAreaSort(null, 1, 1, 1,1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsStartXLessThanZero()
			throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IBaseDataType>[][] area = new ArrayList[0][0];
		@SuppressWarnings("unused")
		ForkAreaSort idxr = new ForkAreaSort(area, -1, 1, 1,1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsStartYLessThanZero()
			throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IBaseDataType>[][] area = new ArrayList[0][0];
		@SuppressWarnings("unused")
		ForkAreaSort idxr = new ForkAreaSort(area, 1, -1, 1,1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsSizeXLessThanOne()
			throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IBaseDataType>[][] area = new ArrayList[0][0];
		@SuppressWarnings("unused")
		ForkAreaSort idxr = new ForkAreaSort(area, 1, 1, 0,1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrowsSizeYLessThanOne()
			throws IllegalArgumentException {
		@SuppressWarnings("unchecked")
		ArrayList<IBaseDataType>[][] area = new ArrayList[0][0];
		@SuppressWarnings("unused")
		ForkAreaSort idxr = new ForkAreaSort(area, 1, 1, 1,0);
	}

	@Test
	public void testSort() throws IllegalArgumentException {
		IBaseDataType d1 = mock(IBaseDataType.class);
		when(d1.compareTime(any(IBaseDataType.class))).thenReturn(1);

		IBaseDataType d2 = mock(IBaseDataType.class);
		when(d2.compareTime(any(IBaseDataType.class))).thenReturn(-1);

		@SuppressWarnings("unchecked")
		ArrayList<IBaseDataType>[][] area = new ArrayList[2][2];
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				area[i][j] = new ArrayList<IBaseDataType>();
				area[i][j].add(d1);
				area[i][j].add(d2);
			}
		}

		int startX = 0;
		int startY = 0;
		int size = 2;
		FakeForkAreaSort idxr = new FakeForkAreaSort(area, startX, startY, size, size);
		idxr.compute();
		
		int correctCount = 0;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				if(area[i][j].get(0)==d2)correctCount++;
			}
		}
		assertTrue(correctCount==4);

	}
	
	@Test
	public void testSortLargerThanInternalThresholdOdd() throws IllegalArgumentException {
		IBaseDataType d1 = mock(IBaseDataType.class);
		IBaseDataType d2 = mock(IBaseDataType.class);
		IBaseDataType d3 = mock(IBaseDataType.class);
		when(d1.compareTime(d2)).thenReturn(1);
		when(d1.compareTime(d3)).thenReturn(1);
		when(d2.compareTime(d1)).thenReturn(-1);
		when(d2.compareTime(d3)).thenReturn(-1);
		when(d3.compareTime(d1)).thenReturn(-1);
		when(d3.compareTime(d2)).thenReturn(1);

		int size = 5;
		@SuppressWarnings("unchecked")
		ArrayList<IBaseDataType>[][] area = new ArrayList[size][size];
		for (int i = 0; i < area.length; i++) {
			for (int j = 0; j < area[0].length; j++) {
				area[i][j] = new ArrayList<IBaseDataType>();
				area[i][j].add(d1);
				area[i][j].add(d2);
				area[i][j].add(d3);
			}
		}

		int startX = 0;
		int startY = 0;
		
		FakeForkAreaSort idxr = new FakeForkAreaSort(area, startX, startY, size,size);
		idxr.compute();
		int correctCount = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if(area[i][j].get(0)==d2)correctCount++;
			}
		}
		assertTrue(correctCount==size*size);
	}
	
	@Test
	public void testSortLargerThanInternalThresholdEven() throws IllegalArgumentException {
		IBaseDataType d1 = mock(IBaseDataType.class);
		IBaseDataType d2 = mock(IBaseDataType.class);
		IBaseDataType d3 = mock(IBaseDataType.class);
		when(d1.compareTime(d2)).thenReturn(1);
		when(d1.compareTime(d3)).thenReturn(1);
		when(d2.compareTime(d1)).thenReturn(-1);
		when(d2.compareTime(d3)).thenReturn(-1);
		when(d3.compareTime(d1)).thenReturn(-1);
		when(d3.compareTime(d2)).thenReturn(1);

		int size = 6;
		@SuppressWarnings("unchecked")
		ArrayList<IBaseDataType>[][] area = new ArrayList[size][size];
		for (int i = 0; i < area.length; i++) {
			for (int j = 0; j < area[0].length; j++) {
				area[i][j] = new ArrayList<IBaseDataType>();
				area[i][j].add(d1);
				area[i][j].add(d2);
				area[i][j].add(d3);
			}
		}

		int startX = 0;
		int startY = 0;
		
		FakeForkAreaSort idxr = new FakeForkAreaSort(area, startX, startY, size,size);
		idxr.compute();
		int correctCount = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if(area[i][j].get(0)==d2)correctCount++;
			}
		}
		assertTrue(correctCount==size*size);
	}
}
