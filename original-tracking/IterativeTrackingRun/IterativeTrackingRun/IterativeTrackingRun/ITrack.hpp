/*
 * File:   ITracks.hpp
 * Author: Dustin
 *
 * Created on October 25, 2013, 2:14 PM
 */

#ifndef ITRACKS_HPP
#define	ITRACKS_HPP
#include "IEvent.hpp"
#include <vector>


class ITrack {
public:
	virtual ~ITrack(){};
	virtual boost::posix_time::ptime getStartTime() = 0;
	virtual boost::posix_time::ptime getEndTime() = 0;
	virtual std::vector<IEvent*>* getEvents() = 0;
	virtual IEvent* getFirst() = 0;
	virtual IEvent* getLast() = 0;
};

#endif	/* ITRACKS_HPP */

