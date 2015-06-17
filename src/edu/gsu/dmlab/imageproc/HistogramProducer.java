package edu.gsu.dmlab.imageproc;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import edu.gsu.dmlab.databases.interfaces.IImageDBConnection;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.imageproc.interfaces.IHistogramProducer;

public class HistogramProducer implements IHistogramProducer {

	int[] wavelenghts;
	IImageDBConnection imageDB;

	public HistogramProducer(IImageDBConnection imageDB, int[] wavelengths) {
		if (imageDB == null)
			throw new IllegalArgumentException(
					"IImageDBConnection cannot be null in HistogramProducer constructor.");
		if (wavelengths == null)
			throw new IllegalArgumentException(
					"wavelengths cannot be null in HistogramProducer constructor.");

		this.imageDB = imageDB;
		this.wavelenghts = wavelengths;
	}

	// make sure to pass dims in as wavelength/parameter pairs
	public Mat getHist(IEvent event, int[][] dims, boolean left) {

		// Get the image parameters for each wavelength in the set of dimensions
		ArrayList<float[][][]> paramsList = new ArrayList<float[][][]>();
		for (int i = 0; i < dims.length; i++) {
			// waveIdx is 1 to 9 but array idx is 0 to 8 so make sure to
			// subtract 1
			int waveIdx = dims[i][0];

			float[][][] params = this.imageDB.getImageParam(event,
					this.wavelenghts[waveIdx - 1], left);
			paramsList.add(params);
		}

		// Place the values into an Matrix for processing by the OpenCV
		// functions
		int depth = dims.length;
		Mat m = new Mat(paramsList.get(0)[0].length, paramsList.get(0).length,
				org.opencv.core.CvType.CV_32FC(depth));
		for (int y = 0; y < m.rows(); y++) {
			for (int x = 0; x < m.cols(); x++) {
				float[] vals = new float[depth];
				for (int i = 0; i < depth; i++) {
					// get the param indicated by the dims array at depth i
					// the param value is from 1 to 10 whare array index is 0-9
					// hence the -1
					vals[i] = paramsList.get(i)[x][y][dims[i][1] - 1];
				}
				m.put(y, x, vals);
			}
		}

		// for some reason the calcHist function wants a list of mat for the
		// input
		List<Mat> matsForHistFunction = new ArrayList<Mat>();
		matsForHistFunction.add(m);

		// create the channel matrix, histSizes matrix, and histogram ranges
		// matrix for input
		int[] channelsArr = new int[depth];
		int[] histSizeArr = new int[depth];
		MatOfFloat rangesMat = new MatOfFloat();
		for (int i = 0; i < depth; i++) {
			channelsArr[i] = i;
			histSizeArr[i] = histSize;
			// the params in dims go from 1 to 10 but the array index
			// of ranges goes from 0 to 9 so subtract 1.
			rangesMat.push_back(new MatOfFloat(ranges[dims[i][1] - 1]));
		}
		MatOfInt channels = new MatOfInt(channelsArr);
		MatOfInt histSizes = new MatOfInt(histSizeArr);

		// create mask and histogram mat and calculate the histogram
		Mat mask = new Mat();
		Mat currHist = new Mat();
		Imgproc.calcHist(matsForHistFunction, channels, mask, currHist,
				histSizes, rangesMat);
		return currHist;
	}
}
