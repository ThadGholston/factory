/* 
 * File:   IEventIndexer.hpp
 * Author: Dustin Kempton
 *
 * Created on December 17, 2014, 6:55 PM
 */

#ifndef IEVENTINDEXER_HPP
#define	IEVENTINDEXER_HPP
#include "IEvent.hpp"
#include <vector>
#include "boost/date_time/posix_time/posix_time.hpp"

class IEventIndexer {
public:

	virtual ~IEventIndexer(){};
	virtual int getExpectedCangePerFrame(boost::posix_time::time_period period) = 0;
    virtual std::vector<IEvent*>* getEventsBetween(boost::posix_time::ptime begin, boost::posix_time::ptime end) = 0;
    virtual std::vector<IEvent*>* getEventsBetween(boost::posix_time::ptime begin, boost::posix_time::ptime end, std::vector<cv::Point2i>* searchArea) = 0;
	virtual std::vector<IEvent*>* getEventsInNeighborhood(boost::posix_time::ptime begin, boost::posix_time::ptime end, std::vector<cv::Point2i>* searchArea, double neighborHoodMultiply) = 0;
    
    virtual boost::posix_time::ptime getFirstTime() = 0;
    virtual boost::posix_time::ptime getLastTime() = 0;
};

#endif	/* IEVENTINDEXER_HPP */

