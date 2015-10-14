/* 
 * File:   IEvent_DB.hpp
 * Author: Dustin Kempton
 *
 * Created on December 17, 2014, 3:59 PM
 */

#ifndef IEVENT_DB_CONNECT_HPP
#define	IEVENT_DB_CONNECT_HPP
#include "IEvent.hpp"
#include "boost/date_time/posix_time/posix_time.hpp"
#include <vector>

class IDBConnection {
public:
   
	virtual ~IDBConnection(){};
    
	//virtual bool getImageParam(IEvent* evnt, cv::Mat& mat, bool leftSide) = 0;
	virtual std::vector< std::vector< std::vector<float> > >* getImageParam(IEvent* evnt, bool leftSide) = 0;
	virtual bool getFirstImage(cv::Mat& img, boost::posix_time::time_period period) = 0;
};

#endif	/* IEVENT_DB_CONNECT_HPP */

