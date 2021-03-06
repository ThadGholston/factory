package edu.gsu.dmlab.imageproc.colormaps;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;


import edu.gsu.dmlab.exceptions.InvalidConfigException;
import edu.gsu.dmlab.imageproc.ColorMap;

//Equals the GNU Octave colormap "autumn".
public class Autumn extends ColorMap {

	public Autumn() throws InvalidConfigException {
		super();
		this.init(256);
	}

	public Autumn(int n) throws InvalidConfigException {
		super();
		this.init(n);
	}

	protected void init(int n) throws InvalidConfigException {
		float r[] = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1 };
		double g[] = { 0, 0.01587301587301587, 0.03174603174603174,
				0.04761904761904762, 0.06349206349206349, 0.07936507936507936,
				0.09523809523809523, 0.1111111111111111, 0.126984126984127,
				0.1428571428571428, 0.1587301587301587, 0.1746031746031746,
				0.1904761904761905, 0.2063492063492063, 0.2222222222222222,
				0.2380952380952381, 0.253968253968254, 0.2698412698412698,
				0.2857142857142857, 0.3015873015873016, 0.3174603174603174,
				0.3333333333333333, 0.3492063492063492, 0.3650793650793651,
				0.3809523809523809, 0.3968253968253968, 0.4126984126984127,
				0.4285714285714285, 0.4444444444444444, 0.4603174603174603,
				0.4761904761904762, 0.492063492063492, 0.5079365079365079,
				0.5238095238095238, 0.5396825396825397, 0.5555555555555556,
				0.5714285714285714, 0.5873015873015873, 0.6031746031746031,
				0.6190476190476191, 0.6349206349206349, 0.6507936507936508,
				0.6666666666666666, 0.6825396825396826, 0.6984126984126984,
				0.7142857142857143, 0.7301587301587301, 0.746031746031746,
				0.7619047619047619, 0.7777777777777778, 0.7936507936507936,
				0.8095238095238095, 0.8253968253968254, 0.8412698412698413,
				0.8571428571428571, 0.873015873015873, 0.8888888888888888,
				0.9047619047619048, 0.9206349206349206, 0.9365079365079365,
				0.9523809523809523, 0.9682539682539683, 0.9841269841269841, 1 };
		float b[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		Mat X = ColorMap.linspace(0, 1, 64);
		Mat rMatF = new MatOfFloat(r);
		Mat gMat = new MatOfDouble(g);
		Mat gMatF = new Mat();
		gMat.convertTo(gMatF, CvType.CV_32FC1);
		Mat bMatF = new MatOfFloat(b);
		this._lut = ColorMap.linear_colormap(X, rMatF.clone(), // red
				gMatF.clone(), // green
				bMatF.clone(), // blue
				n); // number of sample points
	}
}