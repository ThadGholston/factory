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
	private int sizeX;
	private int sizeY;
	private ArrayList<IBaseDataType>[][] area;
	protected static int sizeThreshold = 5;

	public ForkAreaSort(ArrayList<IBaseDataType>[][] area, int startX,
			int startY, int sizeX, int sizeY) {
		if (area == null)
			throw new IllegalArgumentException("Area cannot be null.");
		if (sizeX < 1)
			throw new IllegalArgumentException("SizeX cannot be less than 1.");
		if (sizeY < 1)
			throw new IllegalArgumentException("SizeY cannot be less than 1.");
		if (startX < 0)
			throw new IllegalArgumentException("Start X cannot be less than 0.");
		if (startY < 0)
			throw new IllegalArgumentException("Start Y cannot be less than 0.");

		this.startX = startX;
		this.startY = startY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
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
		if (this.sizeX < sizeThreshold || this.sizeY < sizeThreshold) {
			this.computeDirectly();
			return;
		}

		int splitX = this.sizeX / 2;
		int splitY = this.sizeY / 2;
		int minusSplitX = this.sizeX - splitX;
		int minusSplitY = this.sizeY - splitY;

		this.invokeAll(
		// Do quadrant 2
				new ForkAreaSort(this.area, this.startX, this.startY, splitX,
						splitY),
				// Do quadrant 1
				new ForkAreaSort(this.area, this.startX + splitX, this.startY,
						minusSplitX, splitY),
				// Do quadrant 3
				new ForkAreaSort(this.area, this.startX, this.startY + splitY,
						splitX, minusSplitY),
				// Do quadrant 4
				new ForkAreaSort(this.area, this.startX + splitX, this.startY
						+ splitY, minusSplitX, minusSplitY));
	}

	protected void computeDirectly() {
		int xStop = startX + sizeX;
		int yStop = startY + sizeY;
		for (int x = startX; x < xStop; x++) {
			for (int y = startY; y < yStop; y++) {
				if ((y < this.area.length) && (x < this.area[y].length))
					Collections.sort(this.area[x][y],
							IBaseDataType.baseComparator);

			}
		}
	}

}
