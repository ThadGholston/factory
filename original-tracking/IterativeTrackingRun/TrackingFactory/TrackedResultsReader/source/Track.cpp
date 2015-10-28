/*
 * File:   Track.cpp
 * Author: Dustin
 *
 * Created on November 16, 2013, 11:35 AM
 */
#ifndef ITRACKS_CPP
#define	ITRACKS_CPP
#include "../include/ITrack.hpp"
#include <stdexcept>

using namespace std;
using namespace cv;
namespace bt = boost::posix_time;

class Track : public ITrack {
private:
	IEvent* first;
	IEvent* last;
public:

	Track(IEvent* first, IEvent* last) {
		if (first == NULL) throw invalid_argument("First event cannot be null");
		if (last == NULL)throw invalid_argument("Last event cannot be null");
		this->first = first;
		this->last = last;
	}

	~Track() {
	}

	bt::ptime getStartTime() {
		return this->getFirst()->getTimePeriod().begin();
	}

	bt::ptime getEndTime() {
		return this->getLast()->getTimePeriod().end();
	}

	vector<IEvent*>* getEvents() {
		vector<IEvent*>* returnList = new vector<IEvent*>();

		IEvent* current = this->getFirst();
		while (current != NULL) {
			returnList->push_back(current);
			current = current->getNext();
		}

		return returnList;
	}

	IEvent* getFirst() {
		while (this->first->getPrevious() != NULL) {
			this->first = this->first->getPrevious();
		}
		return this->first;
	}

	IEvent* getLast() {
		while (this->last->getNext() != NULL) {
			this->last = this->last->getNext();
		}
		return this->last;
	}
};

#endif

