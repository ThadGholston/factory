/*
 * File:   EventIndexer.cpp
 * Author: Dustin Kempton
 *
 * Created on December 17, 2014, 6:52 PM
 */

#ifndef EVENTINDEXER_CPP
#define	EVENTINDEXER_CPP
#define _USE_MATH_DEFINES

#include <cstdlib>
#include <fstream>
#include <iostream>
#include <vector>
#include <math.h>

#include <algorithm>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include "../include/IEventIndexer.hpp"

#include <boost/date_time.hpp>
#include "boost/date_time/posix_time/posix_time.hpp"




using namespace std;
using namespace cv;

namespace bt = boost::posix_time;

class EventIndexer : public IEventIndexer {
private:
	const std::locale format = std::locale(std::locale::classic(), new boost::posix_time::time_input_facet("%Y-%m-%d %H:%M:%S"));

	//static constexpr int ARTIMESPAN = 12600;//10800;
	//static constexpr int EFTIMESPAN = 9600;
	//static constexpr int SGTIMESPAN = 3200;
	//static constexpr int FITIMESPAN = 37800; //32400;
	//static constexpr int SSTIMESPAN = 16200;//18000;
	//static constexpr int CHTIMESPAN = 12600;

	int regionDim;
	int regionDivisor;
	boost::posix_time::time_duration frameSpan;
	bt::time_period* globalPeriod = NULL;


	vector<IEvent*>*** regionalEvents;
	vector<vector<IEvent*>*>* frames = NULL;
	/*static constexpr int paramDim = 64;*/



public:

	EventIndexer(string configFile, vector<IEvent*>* events){
		if (this->readConfigFile(configFile)){
			cout << "Event Idx Init ok" << endl;
		}
		else{
			cout << "Event Idx Init failed" << endl;
		}

		this->indexEvents(events);
		delete events;
	}

	~EventIndexer() {


		for (int i = 0; i < this->regionDim; i++){
			for (int j = 0; j < this->regionDim; j++){
				delete this->regionalEvents[i][j];
			}
			delete[] this->regionalEvents[i];
		}
		delete[] this->regionalEvents;

		delete this->globalPeriod;

		for (int i = 0; i < this->frames->size(); i++){
			delete this->frames->at(i);
		}
		delete this->frames;

	}

	/**
	* getExpectedCangePerFrame: returns a the expected change in the number of events seen
	* per frame with the current frame period of the index and the given period
	* as input.
	*
	* @param period    :the period over which calculation will take place
	* @return          :returns the expected value of change in number of events per frame over the given period
	*/
	int getExpectedCangePerFrame(boost::posix_time::time_period period){
		if (period.end()< this->globalPeriod->begin()){
			return 0;
		}
		else if (period.begin()> this->globalPeriod->end()){
			return 0;
		}

		bt::time_period per = this->globalPeriod->intersection(period);
		vector<int> periods = this->getIdx(per);


		double sum = 0.0;
		if (periods.size() > 1){
			vector<IEvent*>* evnts = this->frames->at(periods.at(0));
			double lastVal = evnts->size();

			for (int i = 1; i < periods.size(); i++){
				evnts = this->frames->at(periods.at(i));
				double curVal = evnts->size();
				sum += abs(curVal - lastVal);
				lastVal = curVal;
			}

		}

		int retVal = (int)sum / periods.size();
		return retVal;
	}

	/**
	 * getEventsBetween: returns a vector of IEvent pointers to the IEvents that
	 * fall between the begin time and end time.
	 *
	 * @param begin     :the time to start searching from
	 * @param end       :the time to search to
	 * @return          :returns the list of pointers to the events in the range
	 */
	vector<IEvent*>* getEventsBetween(bt::ptime begin, bt::ptime end) {

		vector<IEvent*>* outList = new vector<IEvent*>();
		bt::time_period per(begin, end);
		if (!this->globalPeriod->intersects(per)){
			return outList;
		}

		per = this->globalPeriod->intersection(per);
		vector<int> periods = this->getIdx(per);


		for (int i = 0; i < periods.size(); i++){
			vector<IEvent*>* evnts = this->frames->at(periods.at(i));
			for (int j = 0; j < evnts->size(); j++){
				IEvent* ev = evnts->at(j);
				if (ev->getTimePeriod().intersects(per) && !this->isInList(outList, ev)){
					outList->push_back(ev);
				}
			}
		}
		return outList;
	}


	vector<IEvent*>* getEventsBetween(bt::ptime begin, bt::ptime end, vector<Point2i>* searchArea) {

		vector<IEvent*>* inSearch = new vector<IEvent*>();
		//for each region in our array of regions, check if they are in the
		//search area for our next event.
		vector<Point2i> scaledSearchArea;
		for (vector<Point2i>::iterator it = searchArea->begin(); it != searchArea->end(); it++) {
			Point2i p = it.operator *();
			scaledSearchArea.push_back(Point2i(p.x / regionDivisor, p.y / regionDivisor));
		}
		Rect rec = cv::boundingRect(scaledSearchArea);
		bt::time_period range(begin, end);

#pragma omp parallel for collapse(2)
		for (int i = rec.x; i <= rec.x + rec.width; i++) {
			for (int j = rec.y; j <= rec.y + rec.height; j++) {

				if (i >= this->regionDim || j >= this->regionDim)continue;

				//if inside the search area and there are events associated with the location
				if (pointPolygonTest(scaledSearchArea, Point2i(i, j), false) >= 0 &&
					regionalEvents[i][j]->size() > 0) {

					//then for every event associated with this location
					for (int k = 0; k < this->regionalEvents[i][j]->size(); k++) {

						//See if tmpEvent is in our search time, 
						//has the same type as what we are looking for,
						// and that the event is not in the list already
						IEvent* tmpEvent = this->regionalEvents[i][j]->at(k);



						if (range.intersects(tmpEvent->getTimePeriod())){

#pragma omp critical(inSearchUpdate)  
							{
								if (!this->isInList(inSearch, tmpEvent)) {
									inSearch->push_back(tmpEvent);
								}
							}
						}//if past time we are searching break as the list is sorted by start time
						else if (range < tmpEvent->getTimePeriod()) {
							break;
						}

					}

				}
			}
		}
		return inSearch;

	}

	vector<IEvent*>* getEventsInNeighborhood(bt::ptime begin, bt::ptime end, vector<Point2i>* searchArea, double neighborHoodMultiply){

		vector<IEvent*>* inSearch = new vector<IEvent*>();
		//for each region in our array of regions, check if they are in the
		//search area for our next event.
		vector<Point2i> scaledSearchArea;
		for (vector<Point2i>::iterator it = searchArea->begin(); it != searchArea->end(); it++) {
			Point2i p = it.operator *();
			scaledSearchArea.push_back(Point2i(p.x / regionDivisor, p.y / regionDivisor));
		}

		Point2f center;
		float radius;
		minEnclosingCircle(scaledSearchArea, center, radius);

		radius = radius*neighborHoodMultiply;

		vector<Point2i> neighborHoodPoly;
		double circumference = 2 * M_PI * radius;
		double halvCir = circumference / 2.0;
		double qurtCir = halvCir / 2.0;
		for (int i = 0; i < halvCir; i++){
			double theta = i / qurtCir;
			int x = center.x + radius*cos(theta);
			int y = center.y + radius*sin(theta);
			neighborHoodPoly.push_back(Point2i(x, y));
		}


		Rect neighborHoodBox = boundingRect(neighborHoodPoly);
		bt::time_period range(begin, end);

#pragma omp parallel for collapse(2)
		for (int i = neighborHoodBox.x; i <= neighborHoodBox.x + neighborHoodBox.width; i++) {
			for (int j = neighborHoodBox.y; j <= neighborHoodBox.y + neighborHoodBox.height; j++) {

				if (i >= this->regionDim || j >= this->regionDim)continue;
				if (i < 0 || j<0)continue;
				//if inside the search area and there are events associated with the location
				if (pointPolygonTest(neighborHoodPoly, Point2i(i, j), false) >= 0 &&
					regionalEvents[i][j]->size() > 0) {

					//then for every event associated with this location
					for (int k = 0; k < this->regionalEvents[i][j]->size(); k++) {

						//See if tmpEvent is in our search time, 
						//has the same type as what we are looking for,
						// and that the event is not in the list already
						IEvent* tmpEvent = this->regionalEvents[i][j]->at(k);



						if (range.intersects(tmpEvent->getTimePeriod())){

#pragma omp critical(inSearchUpdateNeighbor)  
							{
								if (!this->isInList(inSearch, tmpEvent)) {
									inSearch->push_back(tmpEvent);
								}
							}
						}//if past time we are searching break as the list is sorted by start time
						else if (range < tmpEvent->getTimePeriod()) {
							break;
						}

					}

				}
			}
		}
		return inSearch;


	}
	/**
	 * getFirstTime: returns the first start time in the set of events
	 *
	 * @return time_t, time of the first start time in the set of events
	 */
	boost::posix_time::ptime getFirstTime() {

		return this->globalPeriod->begin();


	}

	/**
	 * getLastTime: returns the last end time in the set of events
	 * @return time_t, time of the last end time in the set of events
	 */
	boost::posix_time::ptime getLastTime() {

		return this->globalPeriod->end();
	}


private:



	bool readConfigFile(string configFile) {

		try{

			ifstream myfile(configFile);
			if (myfile.is_open()) {

				std::string frame_span, region_dim, region_div, line;
				//////////////////////////////////////////////////////////////////////////
				//Get Frame span
				{
					getline(myfile, line);//get title of first param;
					if (line.compare("frame_span") == 0){
						getline(myfile, frame_span);//get frame span
						int tmpVal = atoi(frame_span.c_str());
						this->frameSpan = boost::posix_time::seconds(tmpVal);
					}
					else{
						return false;
					}
				}
				//////////////////////////////////////////////////////////////////////////
				//Get Region dim
				{
					getline(myfile, line);//throw away blank line;
					getline(myfile, line);//get title of next set of info;
					if (line.compare("region_dim") == 0){
						getline(myfile, region_dim);//get number of region dimensions
						this->regionDim = atoi(region_dim.c_str());
					}
					else{
						return false;
					}
				}
				//////////////////////////////////////////////////////////////////////////
				//Get Region divisor
				{
					getline(myfile, line);//throw away blank line;
					getline(myfile, line);//get title of next set of info;
					if (line.compare("region_divisor") == 0){
						getline(myfile, region_div);//get region divisor
						this->regionDivisor = atoi(region_div.c_str());
					}
					else{
						return false;
					}
				}
				//////////////////////////////////////////////////////////////////////////
				myfile.close();
				return true;
			}
			else{
				return false;
			}


		}
		catch (Exception ex){
			return false;
		}
	}

	/**
	 * indexEvents  :is called to create various indices for fast retreival of events
	 */
	void indexEvents(vector<IEvent*>* events) {
		//create a 2d array of vectors to store pointers of the events in.
		//this will be used for finding events in a region defined by a
		//polygon.
		this->regionalEvents = new vector<IEvent*>**[this->regionDim];
		for (int k = 0; k < this->regionDim; k++){
			this->regionalEvents[k] = new vector<IEvent*>*[this->regionDim];
			for (int l = 0; l < this->regionDim; l++){
				this->regionalEvents[k][l] = new vector<IEvent*>();
			}
		}


		//put events into regional arrays for searching by shape
		for (int i = 0; i < events->size(); i++){
			std::vector<Point2i> searchArea;
			IEvent* ev = events->at(i);
			std::vector<Point2i>* shape = ev->getShape();
			for (vector<Point2i>::iterator it = shape->begin(); it != shape->end(); ++it) {
				Point2i val = it.operator*();
				searchArea.push_back(Point2i(val.x / this->regionDivisor, val.y / this->regionDivisor));
			}

			Rect r = cv::boundingRect(searchArea);
			//#pragma omp parallel for collapse(2)
			for (int x = r.x; x <= r.x + r.width; x++) {
				for (int y = r.y; y <= r.y + r.height; y++) {
					if (pointPolygonTest(searchArea, Point2d(x, y), false) >= 0) {
						regionalEvents[x][y]->push_back(ev);
					}
				}
			}

		}

		//#pragma omp parallel sections
		{
			//#pragma omp section
			{

				//index based upon time so we can quickly calculate the rolling average of events per frame 
				//so we can have an expected number of events per frame with resonable accuracy
				for (int i = 0; i < events->size(); i++){
					IEvent* ev = events->at(i);
					/*	cout << "Ev period: " << ev->getTimePeriod() << endl;*/
					vector<int> frameIdxVect = this->getIdx(ev->getTimePeriod());
					for (int k = 0; k < frameIdxVect.size(); k++){
						int idx = frameIdxVect.at(k);
						/*		cout << "Idx: " << idx << endl;*/
						this->frames->at(idx)->push_back(ev);
					}
				}

			}
			//#pragma omp section
			{


				//sort all the vectors corresponding to regions for faster searching each region based on time
				for (int i = 0; i < this->regionDim; i++) {
					for (int j = 0; j < this->regionDim; j++) {
						std::sort(regionalEvents[i][j]->begin(), regionalEvents[i][j]->end(), this->compareDates);
					}
				}
			}
		}

	}



	/**
	 * compareDates     :compares the dates, returns true when evOne is before
	 * evTwo, else false.
	 * @param evOne     :first time for comparison
	 * @param evTwo     :second time for comparison
	 * @return          :true if evOne is before evTwo, else false.
	 */
	static bool compareDates(IEvent* evOne, IEvent * evTwo) {

		return evOne->getTimePeriod().begin() < evTwo->getTimePeriod().begin();

	}



	// just a method to test if an object pointer is already in a list of those object pointers
	bool isInList(vector<IEvent*>* l, IEvent * e) {
		for (int i = 0; i < l->size(); i++) {
			IEvent* tmp = l->at(i);

			if (tmp == e)return true;
		}
		return false;
	}



	vector<int> getIdx(bt::time_period period) {

		//if not init then do so
		if (this->globalPeriod == NULL) {

			//find the length, in frames, of the current period
			int length = (int)period.length().total_seconds() / this->frameSpan.total_seconds();

			//calculate the end time of the global period as the bigining time of the current period plus the number of frame periods to accomidate and one extra
			bt::ptime end = period.begin() + (this->frameSpan * length + this->frameSpan);

			//set the global period as the beginning of the current period to the end calculated in the previous line.
			this->globalPeriod = new bt::time_period(period.begin(), end);

			//init the frames vector to be added to
			this->frames = new vector< vector<IEvent*>*>();

			//add enough frames vectors to accomidate the new time range of the index
			this->addVects(length + 1);
		}

		// if this period starts before our current time period we will need to widen the period
		if (this->globalPeriod->begin() > period.begin()) {

			//get the duration of the difference between the original start period and the new one
			//that is needed to accommodate this new time period as well.
			bt::time_duration diff = this->globalPeriod->begin() - period.begin();

			//find the the number of periods this difference covers
			int length = (int)diff.total_seconds() / this->frameSpan.total_seconds();

			//calculate a new start time as the current start minus the time needed to accommodate the number of time frames we are adding 
			bt::ptime start = this->globalPeriod->begin() - bt::seconds((this->frameSpan.total_seconds()*length) + this->frameSpan.total_seconds());

			//update the time period with the new start time
			bt::time_period* oldPeriod = this->globalPeriod;
			this->globalPeriod = new bt::time_period(start, oldPeriod->end());
			delete oldPeriod;

			//add the vectors needed to accommodate the new span
			this->addVects(length + 1, true);
		}

		//if this period ends after our current time period we will need to widen the period
		if (this->globalPeriod->end() < period.end()) {

			//get the duration of the difference between the original end period and the new one
			//that is needed to accommodate this new time period as well.
			bt::time_duration diff = period.end() - this->globalPeriod->end();

			//calculate the length, in frames, that we need to add to accommodate this expansion
			int length = (int)diff.total_seconds() / this->frameSpan.total_seconds();

			//calculate the new end time as the current end plus the time needed to accommodate the number of frames plus one
			bt::ptime end = this->globalPeriod->end() + bt::seconds((this->frameSpan.total_seconds() * length) + this->frameSpan.total_seconds());

			//update the end time of the time period for this index
			bt::time_period* oldPeriod = this->globalPeriod;
			this->globalPeriod = new bt::time_period(oldPeriod->begin(), end);
			delete oldPeriod;

			//add the vectors needed for the new time range
			this->addVects(length + 1);
		}

		//vector of index values this period falls into that will be returned
		vector<int> returnVect;

		//find the offset between the global beginning and the period we are testing
		bt::time_duration diff = period.begin() - this->globalPeriod->begin() + bt::seconds(1);

		//calculate the index that this offset falls into for the starting position
		int beginIdx = (int)diff.total_seconds() / this->frameSpan.total_seconds();

		//calculate the ending index of this period based upon its length and the starting index just calculated
		int endIdx = beginIdx + (int)period.length().total_seconds() / this->frameSpan.total_seconds();
		for (int i = beginIdx; i <= endIdx; i++) {

			//calculate the time passed since the index beginning to the beginning of the current frame
			bt::time_duration afterIndexStart(bt::seconds(this->frameSpan.total_seconds()*i));

			//calculate the period of the current frame
			bt::time_period indexPeriod(this->globalPeriod->begin() + afterIndexStart, this->globalPeriod->begin() + (afterIndexStart + this->frameSpan));

			//see if the current frame intersect the time period passed into the method and add to return vector if so
			if (period.intersects(indexPeriod)){
				returnVect.push_back(i);
			}
		}

		return returnVect;
	}


	/*
	This method assumes that this->trees is already created
	do not call it until the vector is created
	*/
	void addVects(int num, bool front = false) {

		//if we are adding to the front of the frames vector
		if (front) {

			//hold onto the old frames vector
			vector<vector<IEvent*>*>* oldVects = this->frames;

			//create a new frames vector to add to
			this->frames = new vector<vector<IEvent*>*>();

			//push the number of new vectors onto the new list that we need.
			for (int i = 0; i < num; i++) {
				vector<IEvent*>* evntVect = new vector<IEvent*>();
				this->frames->push_back(evntVect);
			}

			//push on the vectors from the old set as well
			for (int i = 0; i < oldVects->size(); i++) {
				this->frames->push_back(oldVects->at(i));
			}

			//delete the old vector of vectors
			delete oldVects;
		}
		else {
			//we just push new vectors onto the frames vector
			for (int i = 0; i < num; i++) {
				vector<IEvent*>* evntVect = new vector<IEvent*>();
				this->frames->push_back(evntVect);
			}
		}
	}
};

#endif
