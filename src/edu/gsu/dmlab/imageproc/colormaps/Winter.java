package edu.gsu.dmlab.imageproc.colormaps;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import edu.gsu.dmlab.exceptions.InvalidConfigException;
import edu.gsu.dmlab.imageproc.ColorMap;

public class Winter extends ColorMap {
    public Winter() throws InvalidConfigException {
        super();
        this.init(256);
    }

    public Winter(int n) throws InvalidConfigException {
        super();
        this.init(n);
    }

    protected void init(int n) throws InvalidConfigException {
        double r[] = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
        double g[] = { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 };
        double b[] = { 1.0, 0.95, 0.9, 0.85, 0.8, 0.75, 0.7, 0.65, 0.6, 0.55,
                0.5 };

        Mat X = ColorMap.linspace(0, 1, 11);
        Mat rMat = new MatOfDouble(r);
        Mat rMatF = new Mat();
        rMat.convertTo(rMatF, CvType.CV_32FC1);

        Mat gMat = new MatOfDouble(g);
        Mat gMatF = new Mat();
        gMat.convertTo(gMatF, CvType.CV_32FC1);

        Mat bMat = new MatOfDouble(b);
        Mat bMatF = new Mat();
        bMat.convertTo(bMatF, CvType.CV_32FC1);

        this._lut = ColorMap.linear_colormap(X, rMatF.clone(), // red
                gMatF.clone(), // green
                bMatF.clone(), // blue
                n); // number of sample points
    }
}
