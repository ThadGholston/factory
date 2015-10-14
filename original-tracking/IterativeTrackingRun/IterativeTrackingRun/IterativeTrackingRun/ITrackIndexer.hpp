/* 
 * File:   ITrackIndexer.hpp
 * Author: Dustin Kempton
 *
 * Created on December 18, 2014, 10:01 PM
 */

#ifndef ITRACKDB_INDEX_HPP
#define	ITRACKDB_INDEX_HPP

#include <vector>
#include "ITrack.hpp"
#include "boost/date_time/posix_time/posix_time.hpp"

class ITrackIndexer {
public:  
	virtual ~ITrackIndexer(){};
    virtual std::vector<ITrack*>* getTracks(boost::posix_time::ptime begin, boost::posix_time::ptime end) = 0;
    virtual std::vector<ITrack*>* getTracksStartBetween(boost::posix_time::ptime begin, boost::posix_time::ptime end, std::vector<cv::Point2i>* searchArea) = 0;
    virtual std::vector<ITrack*>* getTracksEndBetween(boost::posix_time::ptime begin, boost::posix_time::ptime end, std::vector<cv::Point2i>* searchArea) = 0;
    virtual std::vector<ITrack*>* getAll() = 0;
    virtual boost::posix_time::ptime getFirstTime() = 0;
    virtual boost::posix_time::ptime getLastTime() = 0;
};

#endif	/* ITRACKDB_INDEX_HPP */

