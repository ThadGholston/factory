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

	float ranges[][] = { { (float) 0.75, (float) 8.3 },
			{ (float) 0, (float) 256 }, { (float) 0, (float) 52 },
			{ (float) 0.75, (float) 2 }, { (float) 0, (float) 14 },
			{ (float) 0, (float) 150 }, { (float) -0.05, (float) 0.175 },
			{ (float) -0.0001, (float) 0.005 }, { (float) 0, (float) 10 },
			{ (float) -0.0001, (float) 0.05 } };
	int histSize = 15;

	int[] wavelenghts;
	IImageDBConnection imageDB;
	int count = 0;

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
	public void getHist(Mat retMat, IEvent event, int[][] dims, boolean left) {

		// Get the image parameters for each wavelength in the set of dimensions
		ArrayList<float[][][]> paramsList = new ArrayList<>();
		for (int[] kDimension: dims){
			// waveIdx is 1 to 9 but array idx is 0 to 8 so make sure to
			// subtract 1
			int waveIdx = kDimension[0];

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
				vals = null;
			}
		}
		paramsList.clear();
		// for some reason the calcHist function wants a objectList of mat for the
		// input
		List<Mat> matsForHistFunction = new ArrayList<>();
		matsForHistFunction.add(m);

		// create the channel searchSpace, histSizes searchSpace, and histogram ranges
		// searchSpace for input
		int[] channelsArr = new int[depth];
		int[] histSizeArr = new int[depth];
		MatOfFloat rangesMat = new MatOfFloat();
		for (int i = 0; i < depth; i++) {
			channelsArr[i] = i;
			histSizeArr[i] = this.histSize;
			// the params in dims go from 1 to 10 but the array index
			// of ranges goes from 0 to 9 so subtract 1.
			MatOfFloat rng = new MatOfFloat(this.ranges[dims[i][1] - 1]);
			rangesMat.push_back(rng);
			rng.release();
		}
		MatOfInt channels = new MatOfInt(channelsArr);
		MatOfInt histSizes = new MatOfInt(histSizeArr);

		// create mask and histogram mat and calculate the histogram

		Mat currHist = new Mat();
		Mat mask = new Mat();
		Imgproc.calcHist(matsForHistFunction, channels, mask, currHist,
				histSizes, rangesMat);

		// cleanup hopefully invoked a little faster with the null pointers.
		matsForHistFunction.clear();

		m.release();
		mask.release();
		rangesMat.release();
		channels.release();
		histSizes.release();
		currHist.assignTo(retMat);
		currHist.release();
	}
}