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
