/*
* File:   IPointConverter.hpp
* Author: Dustin Kempton
*
* Created on November 23, 2014
*/
#ifndef POINTCONVERTER_HPP
#define	POINTCONVERTER_HPP

#include "opencv2/core/core.hpp"
#include <vector>

using namespace cv;
using namespace std;

class IPointConverter {
public:

	virtual Point2i convertHPCToPixXY(Point2d pointIn) = 0;
	virtual Point2i convertHGSToPixXY(Point2d pointIn) = 0;
	virtual Point2d convertPixXYToHGS(Point2i pointIn) = 0;
	virtual Point2d convertPixXYToHPC(Point2i pointIn) = 0;
	virtual Point2d convertHPC_HCC(double x, double y) = 0;
	virtual Point2d convertHCC_HG(double x, double y) = 0;
	virtual Point2d convertHCC_HPC(double x, double y) = 0;
	virtual Point2d convertHG_HCC(double hglon_deg, double hglat_deg) = 0;
};


#endif	/* IPOINTCONVERTER_HPP */