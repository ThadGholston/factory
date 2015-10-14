/*
 * File:   TrackDB.cpp
 * Author: Dustin
 *
 * Created on December 23, 2013, 3:51 PM
 */

#include "../include/ITrackIndexer.hpp"
#include <opencv2/imgproc/imgproc.hpp>

#include <fstream>
#include <iostream>
#include <stdexcept>
#include <algorithm>
#include <vector>

using namespace std;
using namespace cv;
namespace bt = boost::posix_time;

class TrackIndexer : public ITrackIndexer {
private:
	int regionDim;
	int regionDivisor;
	vector<ITrack*>* tracksPtr = NULL;
	vector<ITrack*>*** regionalTracksStart;
	vector<ITrack*>*** regionalTracksEnd;
public:

	~TrackIndexer(){
		delete tracksPtr;

		for (int i = 0; i < this->regionDim; i++){
			for (int j = 0; j < this->regionDim; j++){
				delete this->regionalTracksStart[i][j];
				delete this->regionalTracksEnd[i][j];
			}
			delete[] this->regionalTracksStart[i];
			delete[] this->regionalTracksEnd[i];
		}
		delete[] this->regionalTracksStart;
		delete[] this->regionalTracksEnd;

	}

	TrackIndexer(std::string configFile, vector<ITrack*>* tracks) {
		if (tracks == NULL) throw invalid_argument("tracks cannot be null");
		this->tracksPtr = tracks;

		if (this->readConfigFile(configFile)){
			cout << "Track Idx Init ok" << endl;
		}
		else{
			cout << "Track Idx Init failed" << endl;
			throw Exception(1, "Track Idx Failed Init", "TrackIndexer Constructor", "TrackIndexer.cpp", 40);
		}

		//create a 2d array of vectors to store pointers of the events in.
		//this will be used for finding events in a region defined by a
		//polygon.
		this->regionalTracksStart = new vector<ITrack*>**[this->regionDim];
		this->regionalTracksEnd = new vector<ITrack*>**[this->regionDim];
		for (int k = 0; k < this->regionDim; k++){
			this->regionalTracksStart[k] = new vector<ITrack*>*[this->regionDim];
			this->regionalTracksEnd[k] = new vector<ITrack*>*[this->regionDim];
			for (int l = 0; l < this->regionDim; l++){
				this->regionalTracksStart[k][l] = new vector<ITrack*>();
				this->regionalTracksEnd[k][l] = new vector<ITrack*>();
			}
		}

		for (int i = 0; i < tracks->size(); i++) {
			ITrack* track = tracks->at(i);


			if (track->getFirst()->getType() == "SG"){
				Rect* beginBbox = track->getFirst()->getBBox();
				Rect* endBbox = track->getLast()->getBBox();
				Rect beginSearchBox, endSearchBox;

				beginSearchBox.x = beginBbox->x / this->regionDivisor;
				beginSearchBox.y = beginBbox->y / this->regionDivisor;
				beginSearchBox.height = beginBbox->height / this->regionDivisor;
				beginSearchBox.width = beginBbox->width / this->regionDivisor;

				endSearchBox.x = endBbox->x / this->regionDivisor;
				endSearchBox.y = endBbox->y / this->regionDivisor;
				endSearchBox.height = endBbox->height / this->regionDivisor;
				endSearchBox.width = endBbox->width / this->regionDivisor;
				#pragma omp sections
				{
					#pragma omp section
					{
						this->popRecArea(&beginSearchBox, track, true);
					}
					#pragma omp section
					{
						this->popRecArea(&endSearchBox, track, false);
					}
				}

			}
			else{
				#pragma omp sections
				{
					#pragma omp section
					{
						//get scaled search area for the beginning of the track
						vector<Point2i> searchAreaBegin;
						vector<Point2i>* startShape = track->getFirst()->getShape();
						for (vector<Point2i>::iterator startShapeIter = startShape->begin();
							startShapeIter != startShape->end(); ++startShapeIter) {

							Point2i startShapePoint = startShapeIter.operator*();
							searchAreaBegin.push_back(Point2i(startShapePoint.x / regionDivisor, startShapePoint.y / regionDivisor));

						}

						//then populate the array based upon the scaled search area and the bounding box   
						Rect startBoundingRect = boundingRect(searchAreaBegin);
						for (int x = startBoundingRect.x; x <= startBoundingRect.x + startBoundingRect.width; x++) {
							for (int y = startBoundingRect.y; y <= startBoundingRect.y + startBoundingRect.height; y++) {
								if ((x >= 0 && x < this->regionDim) && (y >= 0 && y < this->regionDim)){
									if (pointPolygonTest(searchAreaBegin, Point2i(x, y), false) >= 0) {
										//cout << "Start Push X:" << x << " Y:" << y << endl;
										this->regionalTracksStart[x][y]->push_back(track);
									}
								}
							}
						}
					}
					#pragma omp section
					{
						//get the scaled search area for the end of the track. 
						vector<Point2i> searchAreaEnd;
						vector<Point2i>* endShape = track->getLast()->getShape();
						for (vector<Point2i>::iterator endShapeIter = endShape->begin();
							endShapeIter != endShape->end(); ++endShapeIter) {

							Point2i endShapePoint = endShapeIter.operator*();
							searchAreaEnd.push_back(Point2i(endShapePoint.x / regionDivisor, endShapePoint.y / regionDivisor));
						}

						//populate the array based upon that search area
						Rect endBoundingRect = boundingRect(searchAreaEnd);
						for (int x = endBoundingRect.x; x <= endBoundingRect.x + endBoundingRect.width; x++) {
							for (int y = endBoundingRect.y; y <= endBoundingRect.y + endBoundingRect.height; y++) {
								if ((x >= 0 && x < this->regionDim) && (y >= 0 && y < this->regionDim)){
									if (pointPolygonTest(searchAreaEnd, Point2i(x, y), false) >= 0) {
										this->regionalTracksEnd[x][y]->push_back(track);
									}
								}
							}
						}
					}
				}
			}
		}

		#pragma omp sections
		{
			#pragma omp section
			{
				//this->tracksPtr->sort(this->compareDates);
				std::sort(this->tracksPtr->begin(), this->tracksPtr->end(), this->compareDates);
			}
			#pragma omp section
			{
				for (int i = 0; i < regionDim; i++) {
					for (int j = 0; j < regionDim; j++) {
						//regionalTracksStart[i][j].sort(this->compareDates);
						std::sort(regionalTracksStart[i][j]->begin(), regionalTracksStart[i][j]->end(), this->compareDates);
					}
				}
			}
			#pragma omp section
			{
				for (int i = 0; i < regionDim; i++) {
					for (int j = 0; j < regionDim; j++) {
						//regionalTracksEnd[i][j].sort(this->compareDates);
						std::sort(regionalTracksEnd[i][j]->begin(), regionalTracksEnd[i][j]->end(), this->compareDates);
					}
				}
			}
		}
	}


	virtual std::vector<ITrack*>* getAll(){
		vector<ITrack*>* outList = new vector<ITrack*>();

		for (int i = 0; i < this->tracksPtr->size(); i++) {

			ITrack* tmpTrack = this->tracksPtr->at(i);

			bool isInList = false;
			bool same = false;

			for (int k = 0; k < outList->size(); k++) {
				ITrack* innerTmpTrack = outList->at(k);

				//check if we have this pointer in the vector already
				if (tmpTrack == innerTmpTrack){
					same = true;
					break;
				}

				//check if we have a track that has the same first event in the vector already
				if (tmpTrack->getFirst()->getId() == innerTmpTrack->getFirst()->getId()) {
					isInList = true;
					break;
				}
			}

			//if neither same pointer nor same first event in the vector push it on
			if (!isInList && !same) {
				outList->push_back(tmpTrack);
			}
			else if (!same) {
				//cleans up multiple entries of same track with different pointers
				this->tracksPtr->erase(this->tracksPtr->begin() + (i - 1));
				i--;
				delete tmpTrack;

			}

		}

		return outList;
	}



	vector<ITrack*>* getTracks(bt::ptime begin, bt::ptime end) {

		vector<ITrack*>* returnedTracks = new vector<ITrack*>();
		bt::time_period range(begin, end);
		for (int i = 0; i < this->tracksPtr->size(); i++) {
			ITrack* tmpTrk = this->tracksPtr->at(i);
			IEvent* tmpEvent = tmpTrk->getFirst();

			bt::time_period period = tmpEvent->getTimePeriod();
			//if start time is in the time range add to list
			if (range.intersects(period)) {
				returnedTracks->push_back(tmpTrk);
			}//if past time, no reason to continue through the list as it is sorted by start time
			else if (range < period) {
				break;
			}
		}
		return returnedTracks;
	}

	vector<ITrack*>* getTracksStartBetween(bt::ptime begin, bt::ptime end, vector<Point2i>* searchArea) {

		vector<ITrack*>* inSearch = new vector<ITrack*>();
		//for each region in our array of regions, check if they are in the
		//search area for our next event.
		vector<Point2i> scaledSearchArea;
		//cout << "access search area" << endl;

		for (vector<Point2i>::iterator it = searchArea->begin(); it != searchArea->end(); it++) {
			Point2i p = it.operator *();
			scaledSearchArea.push_back(Point2i(p.x / regionDivisor, p.y / regionDivisor));
		}
		//cout << "done with search area" << endl;
		Rect rec = boundingRect(scaledSearchArea);
		bt::time_period range(begin, end);

		//	cout << "start searching lists" << endl;
		#pragma omp parallel for collapse(2)
		for (int i = rec.x; i <= rec.x + rec.width; i++) {
			for (int j = rec.y; j <= rec.y + rec.height; j++) {

				if ((i >= this->regionDim || i < 0) || (j >= this->regionDim || j < 0))continue;
				//if inside the search area and there are events associated with the location
				if (pointPolygonTest(scaledSearchArea, Point2i(i, j), false) >= 0 &&
					this->regionalTracksStart[i][j]->size() > 0) {
					//then for every event associated with this location
					for (int k = 0; k < this->regionalTracksStart[i][j]->size(); k++) {

						//See if tmpEvent is in our search time, 
						//has the same type as what we are looking for,
						// and that the event is not in the list already
						ITrack* tmpTrack = this->regionalTracksStart[i][j]->at(k);
						IEvent* tmpEvent = tmpTrack->getFirst();

						bt::time_period period = tmpEvent->getTimePeriod();

						if (range.intersects(period)) {
							#pragma omp critical(inTrackSearchUpdate)
							{
								//cout << "check is in list" << endl;
								if (!this->isInList(inSearch, tmpTrack))inSearch->push_back(tmpTrack);
								//	cout << "done check" << endl;
							}
						}//if past time we are searching break as the list is sorted by start time
						else if (range < period) {
							break;
						}
					}
				}
			}
		}
		return inSearch;
	}

	vector<ITrack*>* getTracksEndBetween(bt::ptime begin, bt::ptime end, vector<Point2i>* searchArea) {

		vector<ITrack*>* inSearch = new vector<ITrack*>();
		//for each region in our array of regions, check if they are in the
		//search area for our next event.
		vector<Point2i> scaledSearchArea;
		for (vector<Point2i>::iterator it = searchArea->begin(); it != searchArea->end(); it++) {
			Point2i p = it.operator *();
			scaledSearchArea.push_back(Point2i(p.x / regionDivisor, p.y / regionDivisor));
		}
		Rect rec = boundingRect(scaledSearchArea);
		bt::time_period range(begin, end);

		#pragma omp parallel for collapse(2)
		for (int i = rec.x; i <= rec.x + rec.width; i++) {
			for (int j = rec.y; j <= rec.y + rec.height; j++) {

				if (i >= this->regionDim || j >= this->regionDim)continue;
				//if inside the search area and there are events associated with the location
				if (pointPolygonTest(scaledSearchArea, Point2d(i, j), false) >= 0 &&
					this->regionalTracksEnd[i][j]->size() > 0) {
					//then for every event associated with this location
					for (int k = 0; k < this->regionalTracksEnd[i][j]->size(); k++) {

						//See if tmpEvent is in our search time, 
						//has the same type as what we are looking for,
						// and that the event is not in the list already
						ITrack* tmpTrack = this->regionalTracksEnd[i][j]->at(k);
						IEvent* tmpEvent = tmpTrack->getFirst();

						bt::time_period period = tmpEvent->getTimePeriod();
						if (range.intersects(period)) {
							#pragma omp critical(inTrackSearchUpdate)
							{
								if (!this->isInList(inSearch, tmpTrack))inSearch->push_back(tmpTrack);
							}
						}//if past time we are searching break as the list is sorted by start time
						else if (range < period) {
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
	bt::ptime getFirstTime() {
		ITrack* tmpTrk = this->tracksPtr->front();
		IEvent* tmpPtr = tmpTrk->getFirst();
		return tmpPtr->getTimePeriod().begin();
	}

	/**
	 * getLastTime: returns the last end time in the set of events
	 * @return time_t, time of the last end time in the set of events
	 */
	bt::ptime getLastTime() {
		ITrack* tmpTrk = this->tracksPtr->back();
		IEvent* tmpPtr = tmpTrk->getFirst();
		return tmpPtr->getTimePeriod().end();
	}

private:

	void popRecArea(Rect* r, ITrack* track, bool start) {
		#pragma omp parallel for collapse(2)
		for (int x = r->x; x <= r->x + r->width; x++) {
			for (int y = r->y; y <= r->y + r->height; y++) {
				if (x < this->regionDim && y < this->regionDim) {
					if (start) {
						this->regionalTracksStart[x][y]->push_back(track);
					}
					else {
						this->regionalTracksEnd[x][y]->push_back(track);
					}
				}
			}
		}
	}


	bool readConfigFile(string configFile) {

		try{

			ifstream myfile(configFile);
			if (myfile.is_open()) {

				std::string frame_span, region_dim, region_div, line;
				//////////////////////////////////////////////////////////////////////////
				//Get Frame span (Throw this away as TrackIndexer doesn't use it).
				{
					getline(myfile, line);//get title of first param;
					if (line.compare("frame_span") == 0){
						getline(myfile, frame_span);//get frame span
						//int tmpVal = atoi(frame_span.c_str());
						//this->frameSpan = boost::posix_time::seconds(tmpVal);
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
	 * compareDates     :compares the dates, returns true when trackOne is before
	 * trackTwo, else false.
	 * @param trackOne     :first track to use time for comparison
	 * @param trackTwo     :second track to use time for comparison
	 * @return          :true if trackOne is before trackTwo, else false.
	 */
	static bool compareDates(ITrack* trackOne, ITrack* trackTwo) {

		bt::ptime firstTime = trackOne->getStartTime();
		bt::ptime secondTime = trackTwo->getStartTime();
		return firstTime < secondTime;
	}

	// just a method to test if an object pointer is already in a list of those object pointers
	bool isInList(vector<ITrack*>* l, ITrack* e) {
		for (int i = 0; i < l->size(); i++) {
			ITrack* tmp = l->at(i);
			if (tmp->getFirst()->getId() == e->getFirst()->getId())return true;
		}
		return false;
	}

};

