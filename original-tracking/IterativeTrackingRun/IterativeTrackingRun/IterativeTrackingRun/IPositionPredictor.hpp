/*
 * File:   IPosition_Predictor.hpp
 * Author: Dustin
 *
 * Created on October 25, 2013, 1:07 PM
 */

#ifndef IPOSITION_PREDICTOR_HPP
#define	IPOSITION_PREDICTOR_HPP
//#include <opencv2/opencv.hpp>

enum COORDINATES {
	HGS, PIX
};

class IPositionPredictor {
public:
	virtual cv::Point2i getPredictedPos(cv::Point2i point, double span) = 0;
	virtual std::vector<cv::Point2i>* getPredictedPos(std::vector<cv::Point2i>* poly, double span) = 0;
	virtual std::vector<cv::Point2i>* getPredictedPos(std::vector<cv::Point2i>* poly, float* movementVect, double span) = 0;
	virtual std::vector<cv::Point2i>* getSearchRegion(cv::Rect* bBox, double days) = 0;
	virtual std::vector<cv::Point2i>* getSearchRegion(cv::Rect* bBox, float* movementVect, double days) = 0;
};

#endif	/* IPOSITION_PREDICTOR_HPP */

