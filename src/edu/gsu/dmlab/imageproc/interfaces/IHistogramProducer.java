package edu.gsu.dmlab.imageproc.interfaces;

import org.opencv.core.Mat;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;

public interface IHistogramProducer {
	static final float ranges[][] = { { (float) 0.75, (float) 8.3 },
			{ (float) 0, (float) 256 }, { (float) 0, (float) 52 },
			{ (float) 0.75, (float) 2 }, { (float) 0, (float) 14 },
			{ (float) 0, (float) 150 }, { (float) -0.05, (float) 0.175 },
			{ (float) -0.0001, (float) 0.005 }, { (float) 0, (float) 10 },
			{ (float) -0.0001, (float) 0.05 } };
	static final int histSize = 15;

	public Mat getHist(IEvent event, int[][] dims, boolean left);
}
