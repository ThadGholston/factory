/*
 * Copyright (c) 2011. Philipp Wagner <bytefish[at]gmx[dot]de>.
 * Released to public domain under terms of the BSD Simplified license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the organization nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *   See <http://www.opensource.org/licenses/bsd-license>
 *   
 *   Converted to Java class by Dustin Kempton <dkempton1[at]cs[dot]gsu[dot]edu>.
 */

package edu.gsu.dmlab.imageproc;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import edu.gsu.dmlab.exceptions.InvalidConfigException;
import edu.gsu.dmlab.imageproc.colormaps.Autumn;
import edu.gsu.dmlab.imageproc.colormaps.Bone;
import edu.gsu.dmlab.imageproc.colormaps.Jet;
import edu.gsu.dmlab.imageproc.colormaps.Winter;

public abstract class ColorMap {

    public enum COLORMAP {
        AUTUMN, BONE, JET, WINTER
    }

    protected Mat _lut;

    protected static Mat linspace(float x0, float x1, int n) {
        float step = (x1 - x0) / (n - 1);
        float[] vals = new float[n];
        for (int i = 0; i < n; i++) {
            vals[i] = x0 + i * step;
        }
        return new MatOfFloat(vals);
    }

    static void sortMatrixRowsByIndices(Mat src, Mat indices, Mat dst)
            throws InvalidConfigException {
        if (indices.type() != CvType.CV_32SC1)
            throw new InvalidConfigException(
                    "cv::sortRowsByIndices only works on integer indices!");

        ArrayList<Integer> idxList = new ArrayList<>();
        Converters.Mat_to_vector_int(indices.t(), idxList);

        dst.create(src.rows(), src.cols(), src.type());

        for (int idx = 0; idx < idxList.size(); idx++) {
            Mat originalRow = src.row(idxList.get(idx));
            Mat sortedRow = dst.row(idx);
            originalRow.copyTo(sortedRow);
        }
    }

    static Mat sortMatrixRowsByIndices(Mat src, Mat indices)
            throws InvalidConfigException {
        Mat dst = new Mat();
        sortMatrixRowsByIndices(src, indices, dst);
        return dst;
    }

    static Mat argsort(Mat _src) throws InvalidConfigException {
        return argsort(_src, true);
    }

    static Mat argsort(Mat src, boolean ascending)
            throws InvalidConfigException {

        if (src.rows() != 1 && src.cols() != 1)
            throw new InvalidConfigException(
                    "cv::argsort only sorts 1D matrices.");
        int flags = Core.SORT_EVERY_ROW
                + (ascending ? Core.SORT_ASCENDING : Core.SORT_DESCENDING);
        Mat sorted_indices = new Mat();
        Core.sortIdx(src.reshape(1, 1), sorted_indices, flags);
        return sorted_indices;
    }

    static Mat interp1_(Mat X_, Mat Y_, Mat XI) throws InvalidConfigException {
        int n = XI.rows();
        // sort input table
        Mat sort_indices = argsort(X_);

        Mat X = sortMatrixRowsByIndices(X_, sort_indices);
        // ArrayList<Float> xList = new ArrayList<Float>();
        // Converters.Mat_to_vector_float(X, xList);

        Mat Y = sortMatrixRowsByIndices(Y_, sort_indices);
        // ArrayList<Float> yList = new ArrayList<Float>();
        // Converters.Mat_to_vector_float(Y, yList);

        Mat yi = Mat.zeros(XI.size(), XI.type());
        // interpolated values
        for (int i = 0; i < n; i++) {
            int c = 0;
            int low = 0;
            int high = X.rows() - 1;
            // set bounds
            if (XI.get(i, 0)[0] < X.get(low, 0)[0])
                high = 1;
            if (XI.get(i, 0)[0] > X.get(high, 0)[0])
                low = high - 1;
            // binary search
            while ((high - low) > 1) {
                c = low + ((high - low) >> 1);
                if (XI.get(i, 0)[0] > X.get(c, 0)[0]) {
                    low = c;
                } else {
                    high = c;
                }
            }

            // linear interpolation
            double[] tmp = yi.get(i, 0);
            tmp[0] += Y.get(low, 0)[0] + (XI.get(i, 0)[0] - X.get(low, 0)[0])
                    * (Y.get(high, 0)[0] - Y.get(low, 0)[0])
                    / (X.get(high, 0)[0] - X.get(low, 0)[0]);
            yi.put(i, 0, tmp);
        }
        return yi;
    }

    // Interpolates from a base colormap.
    static Mat linear_colormap(Mat X, Mat r, Mat g, Mat b, Mat xi)
            throws InvalidConfigException {
        Mat lut = new Mat();
        Mat lut8 = new Mat();
        ArrayList<Mat> planes = new ArrayList<>();
        planes.add(interp1_(X, b, xi));
        planes.add(interp1_(X, g, xi));
        planes.add(interp1_(X, r, xi));
        Core.merge(planes, lut);
        lut.convertTo(lut8, CvType.CV_8U, 255.);
        return lut8;
    }

    // Interpolates from a base colormap.
    static Mat linear_colormap(Mat X, Mat r, Mat g, Mat b, float begin,
                               float end, float n) throws InvalidConfigException {
        return linear_colormap(X, r, g, b, linspace(begin, end, Math.round(n)));
    }

    // Interpolates from a base colormap.
    protected static Mat linear_colormap(Mat X, Mat r, Mat g, Mat b, int n)
            throws InvalidConfigException {
        return linear_colormap(X, r, g, b, linspace(0, 1, n));
    }

    public static void applyColorMap(Mat src, Mat dst, COLORMAP colormap)
            throws InvalidConfigException {
        ColorMap cm = colormap == COLORMAP.AUTUMN ? new Autumn()
                : colormap == COLORMAP.BONE ? new Bone()
                : colormap == COLORMAP.JET ? new Jet()
                : colormap == COLORMAP.WINTER ? (ColorMap) (new Winter())
                : null;

        if (cm != null) {
            cm.apply(src, dst);
        } else {
            throw new InvalidConfigException(
                    "Unknown colormap id; use one of COLORMAP.*");
        }

    }

    // Applies the colormap on a given image.
    //
    // This function expects BGR-aligned data of type CV_8UC1 or
    // CV_8UC3. If the wrong image type is given, the original image
    // will be returned.
    //
    // Throws an error for wrong-aligned lookup table, which must be
    // of size 256 in the latest OpenCV release (2.3.1).
    void apply(Mat src, Mat dst) throws InvalidConfigException {
        if (_lut.total() != 256)
            throw new InvalidConfigException(
                    "cv::LUT only supports tables of size 256.");
        // Return original searchSpace if wrong type is given (is fail loud better
        // here?)
        if (src.type() != CvType.CV_8UC1 && src.type() != CvType.CV_8UC3) {
            src.copyTo(dst);
            return;
        }
        // Turn into a BGR searchSpace into its grayscale representation.
        if (src.type() == CvType.CV_8UC3)
            Imgproc.cvtColor(src.clone(), src, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(src.clone(), src, Imgproc.COLOR_GRAY2BGR);
        // Apply the ColorMap.
        Core.LUT(src, _lut, dst);
        // _lut.copyTo(_dst);
    }

    // Setup base map to interpolate from.
    protected abstract void init(int n) throws InvalidConfigException;

}