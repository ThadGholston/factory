/*
 * File:   stageOne.cpp
 * Author: Dustin Kempton
 *
 * Created on November 2, 2013, 10:52 AM
 */

#include <opencv2/imgproc/imgproc.hpp>


#include <stdexcept>
#include <algorithm>
#include <vector>
#include <time.h>
#include <TrackingFactory.h>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <omp.h>

#include "../localInclude/IStageOne.hpp"

using namespace std;
using namespace cv;
namespace bt = boost::posix_time;


class StageOne : public IStageOne {
private:

	const std::locale format = std::locale(std::locale::classic(), new boost::posix_time::time_input_facet("%Y-%m-%d %H:%M:%S"));
	static constexpr double secondsToDaysConst = 60.0 * 60.0 * 24.0;

	IEventIndexer* idxer;
	IPositionPredictor* predictor;


public:

	StageOne(IEventIndexer* idxer, IPositionPredictor* predictor) {
		if (idxer == NULL) throw invalid_argument("idxer cannot be null");
		if (predictor == NULL) throw invalid_argument("predictor cannot be null");

		this->idxer = idxer;
		this->predictor = predictor;
	}

	~StageOne() {

	}

	vector<ITrack*>* process() {

		bt::ptime startIn = this->idxer->getFirstTime();
		bt::ptime endIn = this->idxer->getLastTime();

		vector<ITrack*>* tmpOutList = process(startIn, endIn);
		vector<ITrack*>* outList = new vector<ITrack*>();

		for (int i = 0; i < tmpOutList->size(); i++){
			ITrack* trk = tmpOutList->at(i);

			//loop over out list in chunks of 30 in parallel for each chunk
			//if in list breaks after the chunk is done.  
			bool inList = false;
			size_t beginpos = 0;
			size_t n_per_loop = 30 * omp_get_max_threads();
			while ((beginpos < outList->size()) && !inList){
				size_t endpos = std::min(outList->size(), (beginpos + n_per_loop));
				//#pragma omp parallel for reduction(||:inList)
				for (size_t j = beginpos; j < endpos; j++){
					ITrack* trk2 = outList->at(j);
					if (trk->getFirst() == trk2->getFirst()){
						inList = true;
					}
				}
				beginpos = endpos;
			}

			if (inList){
				delete trk;
			}
			else{
				outList->push_back(trk);
			}
		}
		delete tmpOutList;
		return outList;
	}

private:

	std::time_t pt_to_time_t(const bt::ptime& pt) {
		bt::ptime timet_start(boost::gregorian::date(1970, 1, 1));
		bt::time_duration diff = pt - timet_start;
		return diff.ticks() / bt::time_duration::rep_type::ticks_per_second;
	}

	void trackNormMeanMovement(IEvent* inEvent, float* motionNormMean) {
		float xMovement = 0.0;
		float yMovement = 0.0;
		float totalTime = 0.0;
		int count = 0;

		IEvent* tmp = inEvent;
		for (int i = 0; i < 10; i++){
			if (tmp->getPrevious() != NULL)tmp = tmp->getPrevious();
		}



		while (tmp->getNext() != NULL) {
			IEvent* tmp2 = tmp->getNext();
			Point2i* tmp2Loc = tmp2->getLocation();
			Point2i* tmpLoc = tmp->getLocation();
			xMovement += tmp2Loc->x - tmpLoc->x;
			yMovement += tmp2Loc->y - tmpLoc->y;

			double span;
			bt::ptime startSearch;
			bt::ptime endSearch;

			boost::posix_time::time_period tp = tmp->getTimePeriod();
			startSearch = tmp2->getTimePeriod().end();
			endSearch = startSearch + (tmp2->getTimePeriod().begin() - tp.begin());
			span = (endSearch - startSearch).total_seconds() / secondsToDaysConst;

			totalTime += span;
			tmp = tmp2;
			count++;
		}

		if (count > 0) {
			float xMean = xMovement / count;
			float yMean = yMovement / count;
			float tMean = totalTime / count;
			float xMeanPerTime = xMean / tMean;
			float yMeanPerTime = yMean / tMean;
			//float val = (xMeanPerTime * xMeanPerTime) + (yMeanPerTime * yMeanPerTime);
			//val = sqrt(val);

			motionNormMean[0] = xMean;
			motionNormMean[1] = yMean;
		}
		else {
			motionNormMean[0] = 0;
			motionNormMean[1] = 0;
		}
		return;
	}

	vector<ITrack*>* process(bt::ptime start, bt::ptime finish) {
		//get all events between start and end
		vector<IEvent*>* eventsVect = this->idxer->getEventsBetween(start, finish);
		vector<ITrack*>* tracks = new vector<ITrack*>();


		/*for each event between start and end, find events
		 * that are in the next frame after the current one we are looking at.
		 * Then link an event and the current one together iff it is the only
		 * one in the search box determined by our position predictor and the
		 * length of the current event.
		 */
#pragma omp parallel for num_threads(8) schedule(dynamic, 10)
		for (int i = 0; i < eventsVect->size(); i++) {

			IEvent* currentEvent = eventsVect->at(i);

			if (currentEvent->getNext() == NULL || currentEvent->getPrevious()->getPrevious() == NULL) {
				//get search area based on time in current event.

				double span;
				bt::ptime startSearch;
				bt::ptime endSearch;
				vector<Point2i>* searchArea;
				vector<IEvent*>*inSearchArea;
				if (currentEvent->getPrevious() == NULL || currentEvent->getPrevious()->getPrevious() == NULL){
					//get the search area to find tracks that may belong linked to the current one being processed
					span = currentEvent->getTimePeriod().length().total_seconds() / secondsToDaysConst;
					//set the time span to search in as the end of our current track+the 
					//the span of the frame for the last event in our current track being processed.
					startSearch = currentEvent->getTimePeriod().end();
					endSearch = startSearch + currentEvent->getTimePeriod().length();
					searchArea = this->predictor->getSearchRegion(currentEvent->getBBox(), span);

					inSearchArea = idxer->getEventsBetween(startSearch, endSearch, searchArea);
					delete searchArea;

				}
				else{
					boost::posix_time::time_period tp = currentEvent->getPrevious()->getTimePeriod();
					startSearch = currentEvent->getTimePeriod().end();
					endSearch = startSearch + (currentEvent->getTimePeriod().begin() - tp.begin());
					span = (endSearch - startSearch).total_seconds() / secondsToDaysConst;

					float* motionVect = new float[2];
					this->trackNormMeanMovement(currentEvent, motionVect);
					searchArea = this->predictor->getSearchRegion(currentEvent->getBBox(), motionVect, span);

					inSearchArea = idxer->getEventsBetween(startSearch, endSearch, searchArea);
					delete searchArea;
					delete[] motionVect;
				}


				//////////////////////////////////////////////////////////////////////////
				//If the list is size one, it is the only one in our search area.
#pragma omp critical(pushTracks)
				{
					if (inSearchArea->size() == 1) {
						IEvent* nextEvent = inSearchArea->back();
						//If the event in our search area doesn't have the previous event set
						// and our current event doesn't have the next event set,
						//then they can be linked together.


						if (nextEvent->getPrevious() == NULL
							&& currentEvent->getTimePeriod().begin() < nextEvent->getTimePeriod().begin()
							&& nextEvent->getId() != currentEvent->getId()) {

							nextEvent->setPrevious(currentEvent);
							currentEvent->setNext(nextEvent);
							tracks->push_back(TrackingFactory::GetNewTrack(currentEvent, nextEvent));
						}
						else {
							tracks->push_back(TrackingFactory::GetNewTrack(currentEvent, currentEvent));
						}

					}
					else {
						tracks->push_back(TrackingFactory::GetNewTrack(currentEvent, currentEvent));
					}
				}
				delete inSearchArea;
			}
		}

		delete eventsVect;
		return tracks;
	}
};

