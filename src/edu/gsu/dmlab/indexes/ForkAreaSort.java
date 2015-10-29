/**
 * 
 */
package edu.gsu.dmlab.indexes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.RecursiveAction;

import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;

/**
 * @author Dustin Kempton
 * @version10/28/2015
 * @Owner Data Mining Lab, Georgia State University
 *
 */
@SuppressWarnings("serial")
public class ForkAreaSort extends RecursiveAction {

	private int startX;
	private int startY;
	private int size;
	private ArrayList<IBaseDataType>[][] area;
	protected static int sizeThreshold = 5;

	public ForkAreaSort(ArrayList<IBaseDataType>[][] area, int startX,
			int startY, int size) {
		if (area == null)
			throw new IllegalArgumentException("Area cannot be null.");
		if (size < 1)
			throw new IllegalArgumentException("Size cannot be less than 1.");
		if (startX < 0)
			throw new IllegalArgumentException("Start X cannot be less than 0.");
		if (startY < 0)
			throw new IllegalArgumentException("Start Y cannot be less than 0.");

		this.startX = startX;
		this.startY = startY;
		this.size = size;
		this.area = area;	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.RecursiveAction#compute()
	 */
	@SuppressWarnings("static-access")
	@Override
	protected void compute() {
		if (this.size < sizeThreshold) {
			this.computeDirectly();
			return;
		}

		int split = this.size / 2;

		this.invokeAll(new ForkAreaSort(this.area, this.startX, this.startY,
				split), new ForkAreaSort(this.area, this.startX + split,
				this.startY + split, this.size - split));
	}

	protected void computeDirectly() {
		for (int x = startX; x < startX + size; x++) {
			for (int y = startY; y < startX + size; y++) {
				if ((x < this.area.length) && (y < this.area[x].length))
					Collections.sort(this.area[x][y],
							IBaseDataType.baseComparator);
				
			}
		}
	}

}
