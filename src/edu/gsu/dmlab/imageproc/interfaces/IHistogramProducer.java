package edu.gsu.dmlab.imageproc.interfaces;

import org.opencv.core.Mat;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;

public interface IHistogramProducer {

	void getHist(Mat retMat, IEvent event, int[][] dims, boolean left);
}