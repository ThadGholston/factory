/*
 * File:   PositionPredictor.cpp
 * Author: Dustin Kempton
 *
 * Created on October 25, 2013, 2:45 PM
 */
#ifndef POSITIONPREDICTOR_CPP
#define POSITIONPREDICTOR_CPP

#ifndef M_PI
#define M_PI 3.14159265358979323846264338327
#endif

#include <math.h>
#include <stdexcept>
#include <opencv2/core/core.hpp>
#include "../include/IPositionPredictor.hpp"
#include "../include/IPointConverter.hpp"

using namespace cv;
using namespace std;

class PositionPredictor : public IPositionPredictor {
private:
	IPointConverter* convtr;
	COORDINATES coor;
	static constexpr double theta = -1.5;
	static constexpr double RADTODEG = 180 / M_PI;


public:

	PositionPredictor(IPointConverter* convtr,COORDINATES coor = PIX) : coor(coor) {
		if (convtr == NULL) throw invalid_argument("PositionConverter cannot be null");
		this->convtr = convtr;
	}

	/**
	 * getPredictedPos :returns the predicted position of a point, for the change in time,
	 * based upon the latitude of the point and the solar rotation at that latitude.
	 * @param point     :the point to calculate the new position of.
	 * @param spanDays  :the time span used to determine how far the sun has rotated
	 * @return          :a new point with the new coordinates
	 */
	Point2i getPredictedPos(Point2i point, double spanDays) {
		if (coor == PIX) {
			Point2d HGSCoord = this->convtr->convertPixXYToHGS(point);
			HGSCoord = this->calcNewLoc(HGSCoord, spanDays);
			return this->convtr->convertHGSToPixXY(HGSCoord);
		}
		else {
			return this->calcNewLoc(point, spanDays);
		}
	}

	/**
	 * getPredictedPos :returns the predicted position of all the points in the list,
	 * for the change in time,based upon the latitude of the point and the solar
	 * rotation at that latitude.
	 * @param poly     :the list of point to calculate the new position of.
	 * @param spanDays  :the time span used to determine how far the sun has rotated
	 * @return          :a new list of points with the new coordinates
	 */
	vector<Point2i>* getPredictedPos(vector<Point2i>* poly, double spanDays) {
		vector<Point2i>* outList = new vector<Point2i >();
		for (vector<Point2i>::const_iterator iterator = poly->begin(); iterator != poly->end(); ++iterator) {
			outList->push_back(this->getPredictedPos(iterator.operator *(), spanDays));
		}
		return outList;
	}

	vector<cv::Point2i>* getPredictedPos(std::vector<cv::Point2i>* poly, float* movementVect, double spanDays){
		vector<Point2i>* outList = new vector<Point2i >();
		double xMove = movementVect[0] * spanDays;
		double yMove = movementVect[1] * spanDays;
		//printf("xMove: %E \n", xMove);
		//printf("yMove: %E \n", yMove);
		for (vector<Point2i>::const_iterator iterator = poly->begin(); iterator != poly->end(); ++iterator) {
			Point2i tmp = iterator.operator*();
			outList->push_back(Point2i(tmp.x + xMove, tmp.y + yMove));
		}
		return outList;
	}

	vector<Point2i>* getSearchRegion(Rect* bBox, float* movementVect, double days) {

		double xMove = movementVect[0] * days;
		double yMove = movementVect[1] * days;

		Point2i oldCorner = Point2i(bBox->x, bBox->y);
		Point2i rotatedCorner = Point2i(oldCorner.x + xMove, oldCorner.y + yMove);


		Point2i rotatedLowerLeft = Point2i(rotatedCorner.x, rotatedCorner.y + bBox->height);


		Point2i UpperLeft = rotatedCorner;
		Point2i LowerLeft = rotatedLowerLeft;




		Point2i UpperCenterRight = Point2i((rotatedCorner.x + bBox->width), rotatedCorner.y);
		Point2i LowerCenterRight = Point2i((rotatedLowerLeft.x + bBox->width), rotatedLowerLeft.y);

		int length = UpperCenterRight.x - UpperLeft.x;
		int addedHeight = (int)(tan(theta * (M_PI / 180.0)) * length);

		Point2i LowerRight = Point2i(LowerCenterRight.x, LowerCenterRight.y - addedHeight);
		Point2i UpperRight = Point2i(UpperCenterRight.x, UpperCenterRight.y + addedHeight);

		vector<Point2i>* out = new vector<Point2i >();
		out->push_back(UpperLeft);
		out->push_back(LowerLeft);
		out->push_back(LowerRight);
		out->push_back(LowerCenterRight);
		out->push_back(UpperCenterRight);
		out->push_back(UpperRight);

		return out;
	}

	vector<Point2i>* getSearchRegion(Rect* bBox, double days) {
		Point2i oldCorner = Point2i(bBox->x, bBox->y);
		Point2i rotatedCorner = this->getPredictedPos(oldCorner, days);

		//Point2i oldLowerLeft = Point2i(oldCorner.x, oldCorner.y + bBox->height);
		Point2i rotatedLowerLeft = Point2i(rotatedCorner.x, rotatedCorner.y + bBox->height);


		Point2i UpperLeft = rotatedCorner;
		Point2i LowerLeft = rotatedLowerLeft;
		//        int diffXUL = rotatedCorner.x - oldCorner.x;
		//        int diffXLL = rotatedLowerLeft.x - oldLowerLeft.x;
		//        Point2i UpperLeft(rotatedCorner.x - (diffXUL / 2), rotatedCorner.y);
		//        Point2i LowerLeft(rotatedLowerLeft.x - (diffXLL / 2), rotatedLowerLeft.y);



		Point2i UpperCenterRight = Point2i((rotatedCorner.x + bBox->width), rotatedCorner.y);
		Point2i LowerCenterRight = Point2i((rotatedLowerLeft.x + bBox->width), rotatedLowerLeft.y);

		int length = UpperCenterRight.x - UpperLeft.x;
		int addedHeight = (int)(tan(theta * (M_PI / 180.0)) * length);

		Point2i LowerRight = Point2i(LowerCenterRight.x, LowerCenterRight.y - addedHeight);
		Point2i UpperRight = Point2i(UpperCenterRight.x, UpperCenterRight.y + addedHeight);

		vector<Point2i>* out = new vector<Point2i >();
		out->push_back(UpperLeft);
		out->push_back(LowerLeft);
		out->push_back(LowerRight);
		out->push_back(LowerCenterRight);
		out->push_back(UpperCenterRight);
		out->push_back(UpperRight);

		return out;
	}
private:

	/**
	 * calcNewLoc       :calculates the new location in HGS based on time passed and latitude
	 * @param pointIn   :point in HGS to calculate the new position of
	 * @param days      :how far in the future, in days, for which to calculate the position of the point
	 * @return  :new point with new HGS coordinates
	 */
	Point2d calcNewLoc(Point2d pointIn, double days) {
		double x = pointIn.x + days * (14.44 - 3.0 * pow(sin(pointIn.y / RADTODEG), 2.0));
		pointIn.x = (float)x;

		return pointIn;
	}

};
#endif