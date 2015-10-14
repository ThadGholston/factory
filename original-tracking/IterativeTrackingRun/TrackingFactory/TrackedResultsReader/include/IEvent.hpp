/* 
 * File:   IEvent.h
 * Author: Dustin
 *
 * Created on October 23, 2013, 8:12 AM
 */

#ifndef IEVENT_HPP
#define	IEVENT_HPP

#include "boost/date_time/posix_time/posix_time.hpp"
#include "opencv2/core/core.hpp"


#include <vector>

//#include "IEventTime.hpp"

class IEvent {
public:
    //a constructor that takes in each of these and verifies that they are not null pointers would be good,
    //since we are not going to provide a method of setting them once the object is created.
	virtual ~IEvent(){};
    virtual int getId() = 0;
    virtual cv::Point2i* getLocation() = 0;
    virtual cv::Rect* getBBox() = 0; //this one has two points, the upper right and lower left corners of the bounding box
    virtual std::vector<cv::Point2i>* getShape() = 0; //this one may be an empty list but cannot be null
    virtual boost::posix_time::time_period getTimePeriod() = 0;
	virtual void updateTimePeriod(boost::posix_time::time_period* period) = 0;
    virtual std::string getType() = 0;
    virtual IEvent* getPrevious() = 0;
    virtual void setPrevious(IEvent*) = 0;
    virtual IEvent* getNext() = 0;
    virtual void setNext(IEvent*) = 0;
};

#endif	/* IEVENT_H */

