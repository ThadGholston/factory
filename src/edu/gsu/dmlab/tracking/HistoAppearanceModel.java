package edu.gsu.dmlab.tracking;

import org.apache.commons.math3.special.Erf;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.imageproc.interfaces.IHistogramProducer;
import edu.gsu.dmlab.tracking.interfaces.IAppearanceModel;

public class HistoAppearanceModel implements IAppearanceModel {

	double sameMean, sameStdDev, diffMean, diffStdDev;
	IHistogramProducer histoProducer;
	int[][] dims;
	int compMethod;

	public HistoAppearanceModel(double sameMean, double sameStdDev, double diffMean, double diffStdDev,
			IHistogramProducer histoProducer, int[][] dims, int compMethod) {

		if (histoProducer == null)
			throw new IllegalArgumentException("Histogram Producer cannot be null.");
		this.sameMean = sameMean;
		this.sameStdDev = sameStdDev;
		this.diffMean = diffMean;
		this.diffStdDev = diffStdDev;
		this.histoProducer = histoProducer;
		this.dims = dims;
		this.compMethod = compMethod;
	}

	@Override
	public double calcProbAppearance(ITrack leftTrack, ITrack rightTrack) {
		Mat leftFrameHist = new Mat();
		Mat rightFrameHist = new Mat();
		this.histoProducer.getHist(leftFrameHist, leftTrack.getLast(), this.dims, true);
		this.histoProducer.getHist(rightFrameHist, rightTrack.getFirst(), this.dims, false);
		double compVal = this.compareHistogram(leftFrameHist, rightFrameHist);
		double sameProb, diffProb;
		sameProb = calcNormProb(compVal, sameMean, sameStdDev);
		diffProb = calcNormProb(compVal, diffMean, diffStdDev);
		return sameProb / (sameProb + diffProb);
	}

	private double compareHistogram(Mat hist1, Mat hist2) {
		double value = 0;
		switch (this.compMethod) {
		case 1:
			value = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CORREL);
			break;
		case 2:
			value = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);
			break;
		case 3:
			value = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_INTERSECT);
			break;
		default:
			value = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_BHATTACHARYYA);
			break;
		}

		if (Double.isNaN(value)) {
			return 0.0;
		}
		return value;
	}

	private double calcNormProb(double x, double mean, double stdDev) {
		double val1 = normCDF(x - stdDev, mean, stdDev);
		double val2 = normCDF(x + stdDev, mean, stdDev);
		double val = val2 - val1;

		return val;
	}

	private double normCDF(double x, double mean, double stdDev) {
		double val = (x - mean) / (stdDev * Math.sqrt(2.0));
		val = 1.0 + Erf.erf(val);
		val = val * 0.5;
		return val;
	}
}
