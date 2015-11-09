/*
 * File:   BaseUpperStage.cpp
 * Author: Dustin
 *
 * Created on December 24, 2013, 11:30 AM
 */

#ifndef BASEUPPERSTAGE_HPP
#define	BASEUPPERSTAGE_HPP
#ifndef M_PI
#define M_PI 3.14159265358979323846264338327
#endif
#include <math.h>
#include <cstdlib>
#include <fstream>
#include <stdexcept>
#include <algorithm>
#include <unordered_map>

#include <TrackingFactory.h>

#include "../localInclude/IStageTwoThree.hpp"
#include "../localInclude/CostGraph.hpp"
#include "../localInclude/SuccessiveShortestPath.hpp"



#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>


//#include <boost/foreach.hpp>


#include <boost/math/special_functions/factorials.hpp>






using namespace std;
using namespace cv;
namespace bt = boost::posix_time;


class BaseUpperStage : public IStageTwoThree {
private:


	struct trackRelat {
		ITrack* trk;
		vector<ITrack*>* relate;
	};

	double sameMean, sameStdDev, diffMean, diffStdDev;
	int timespan, numSpan;
	vector<int>* params;
	//float* histRange1;
	//float* histRange2;
	//float* histRange3;
	const float** histRanges;

	static constexpr int regionDim = 64;
	static constexpr int regionDivisor = 64;
	//static constexpr int threadOverlap = 28800;


	static constexpr double secondsToDaysConst = 60.0 * 60.0 * 24.0;
	ITrackIndexer* tracksIdxr;
	IPositionPredictor* predictor;
	IEventIndexer* evntsIdxr;
	IDBConnection* db;


	double pValues[regionDim][regionDim][3];
	double pValMax[3];
public:

	BaseUpperStage(IDBConnection* db, IPositionPredictor* predictor, ITrackIndexer* tracksIdxr, IEventIndexer* evntsIdxr,
		int timespan, double sameMean, double sameStdDev, double diffMean, double diffStdDev, vector<float*>* histRangesVec, vector<int>* params) {
		if (db == NULL) throw invalid_argument("db cannot be null");
		if (tracksIdxr == NULL) throw invalid_argument("tracksIdxr cannot be null");
		if (evntsIdxr == NULL) throw invalid_argument("evntsIdxr cannot be null");
		if (predictor == NULL) throw invalid_argument("predictor cannot be null");
		this->tracksIdxr = tracksIdxr;
		this->evntsIdxr = evntsIdxr;
		this->predictor = predictor;
		this->db = db;


		this->sameMean = sameMean;
		this->sameStdDev = sameStdDev;
		this->diffMean = diffMean;
		this->diffStdDev = diffStdDev;
		this->timespan = timespan;

		this->popFile("eventsStartProb", 0);
		this->popFile("eventsEndProb", 1);
		this->popFile("eventsProb", 2);


		//get the number of spans the data crosses so we can use for 
		//getting the expected change in detections per frame later.
		bt::ptime strt = this->evntsIdxr->getFirstTime();
		bt::time_duration diff = this->evntsIdxr->getLastTime() - strt;
		this->numSpan = diff.total_seconds() / this->timespan;
		if (this->numSpan > 10){
			this->numSpan = 10;
		}

		this->histRanges = new const float*[histRangesVec->size()];
		for (int i = 0; i < histRangesVec->size(); i++){
			this->histRanges[i] = histRangesVec->at(i);
		}

		this->params = params;

		//this->histRange1 = histRange1;
		//this->histRange2 = histRange2;
		//this->histRange3 = histRange3;
		//histRanges[0] = this->histRange1;
		////cout << "HisRange1: " << histRanges[0][0] << ", " << histRanges[0][1] << endl;
		//histRanges[1] = this->histRange2;
		////cout << "HisRange2: " << histRanges[1][0] << ", " << histRanges[1][1] << endl;
		//histRanges[2] = this->histRange3;
		////cout << "HisRange3: " << histRanges[2][0] << ", " << histRanges[2][1] << endl;
	}

	~BaseUpperStage(){

		delete this->histRanges;

	}

	vector<ITrack*>* process() {

		cout << "------ start upper stage ---------\n";
		vector< ITrack* >* vectOfTracks = this->tracksIdxr->getAll();


		if (vectOfTracks->size() > 0) {


			std::unordered_map < int, int >* eventMap = new std::unordered_map < int, int >();

			int countX = 0;

			std::vector < trackRelat > trackRelationsList;

			//for each track that we got back in the list we will find the potential
			//matches for that track and link it to the one with the highest probability of
			//being a match.
#pragma omp parallel for num_threads(8) shared(countX) schedule(dynamic, 3)
			for (uint i = 0; i < vectOfTracks->size(); i++) {


				//we get the event associated with the end of our current track as that is
				//the last frame of the track and has position information and image
				//information associated with it.
				ITrack* currentTrack = vectOfTracks->at(i);
				IEvent* currentEvent = currentTrack->getLast();

				double span;
				bt::ptime startSearch;
				bt::ptime endSearch;
				vector<Point2i>* searchArea;
				vector<ITrack*>* potentialTracks;


				if (currentEvent->getPrevious() == NULL || currentEvent->getPrevious()->getPrevious() == NULL){
					//get the search area to find tracks that may belong linked to the current one being processed
					span = currentEvent->getTimePeriod().length().total_seconds() / secondsToDaysConst;
					//set the time span to search in as the end of our current track+the 
					//the span of the frame for the last event in our current track being processed.
					startSearch = currentEvent->getTimePeriod().end();
					endSearch = startSearch + currentEvent->getTimePeriod().length();
					searchArea = this->predictor->getSearchRegion(currentEvent->getBBox(), span);
					potentialTracks = this->tracksIdxr->getTracksStartBetween(startSearch, endSearch, searchArea);

				}
				else{
					boost::posix_time::time_period tp = currentEvent->getPrevious()->getTimePeriod();
					startSearch = currentEvent->getTimePeriod().end();
					endSearch = startSearch + (currentEvent->getTimePeriod().begin() - tp.begin());
					span = (endSearch - startSearch).total_seconds() / secondsToDaysConst;

					float* motionVect = new float[2];
					this->trackMovement(currentTrack, motionVect);
					searchArea = this->predictor->getSearchRegion(currentEvent->getBBox(), motionVect, span);
					potentialTracks = this->tracksIdxr->getTracksStartBetween(startSearch, endSearch, searchArea);

					delete[] motionVect;
				}


				//search locations for potential matches up to the maxFrameSkip away
				//using the previously predicted search area as the starting point
				//for the next search area. 
				startSearch = currentEvent->getTimePeriod().end();
				for (int i = 0; i < this->getMaxFrameSkip(); i++) {
					vector<ITrack*>* potentialTracks2;
					if (currentEvent->getPrevious() == NULL || currentEvent->getPrevious()->getPrevious() == NULL){
						//get the search area to find tracks that may belong linked to the current one being processed
						double span = currentEvent->getTimePeriod().length().total_seconds() / secondsToDaysConst;
						//set the time span to search in as the end of our current track+the 
						//the span of the frame for the last event in our current track being processed.
						bt::ptime endSearch = startSearch + currentEvent->getTimePeriod().length();
						Rect rect = cv::boundingRect(*searchArea);
						delete searchArea;
						searchArea = this->predictor->getSearchRegion(&rect, span); //getPredictedPos(&searchArea, span);
						//cout << "get track if" << endl;
						potentialTracks2 = this->tracksIdxr->getTracksStartBetween(startSearch, endSearch, searchArea);
						//cout << "end get" << endl;
						startSearch = endSearch;
					} else {
						boost::posix_time::time_period tp = currentEvent->getPrevious()->getTimePeriod();
						//startSearch = currentEvent->getTimePeriod().end();
						bt::ptime endSearch = startSearch + (currentEvent->getTimePeriod().begin() - tp.begin());
						double span2 = (endSearch - startSearch).total_seconds() / secondsToDaysConst;

						float* motionVect = new float[2];
						this->trackMovement(currentTrack, motionVect);
						Rect rect = cv::boundingRect(*searchArea);
						delete searchArea;
						searchArea = this->predictor->getSearchRegion(&rect, motionVect, span); //getPredictedPos(&searchArea, motionVect, span2);

						//cout << "get track else" << endl;
						potentialTracks2 = this->tracksIdxr->getTracksStartBetween(startSearch, endSearch, searchArea);
						//cout << "end get" << endl;
						delete[] motionVect;
						startSearch = endSearch;
					}


					//put potential matches not in potentialTracks list into the list
					while (!potentialTracks2->empty()) {

						ITrack* possibleTrackFromSkippedFrames = potentialTracks2->back();
						potentialTracks2->pop_back();

						bool isInList = false;
						//cout << "Search for potential2 in potential" << endl;
						for (vector<ITrack*>::iterator iterator = potentialTracks->begin();
							iterator != potentialTracks->end(); ++iterator) {

							ITrack* trackInList = iterator.operator *();
							if (possibleTrackFromSkippedFrames->getFirst()->getId() == trackInList->getFirst()->getId()) {
								isInList = true;
								break;
							}
						}
						//cout << "done search" << endl;
						if (!isInList) {
							potentialTracks->push_back(possibleTrackFromSkippedFrames);
						}
					}

					delete potentialTracks2;
				}

				delete searchArea;
				//cout << "after frame skip" << endl;
				//if the list of potential matches has anything in it we will process
				//those tracks.
				if (potentialTracks->size() >= 1) {
					//if the last event of our track is not already in the unordered map
					//we insert the event/idx pair into the Map
#pragma omp critical(updateXMap)
					{
						int indexID = currentTrack->getFirst()->getId();
						std::unordered_map<int, int>::const_iterator got = eventMap->find(indexID);
						if (got == eventMap->end()) {
							pair< int, int> idx(indexID, countX++);
							eventMap->insert(idx);
						}


						//test the first event in each of the potential matches to see if
						//it is in the Map and insert it if it is not.
						for (vector<ITrack*>::iterator iter = potentialTracks->begin(); iter != potentialTracks->end(); iter++) {
							ITrack* tmpTrk = iter.operator *();
							IEvent* tmpEve = tmpTrk->getFirst();
							int tmpEvID = tmpEve->getId();
							std::unordered_map<int, int>::const_iterator got2 = eventMap->find(tmpEvID);
							if (got2 == eventMap->end()) {
								pair< int, int> idx(tmpEvID, countX++);
								eventMap->insert(idx);
							}
						}
					}

					//create a relation of the track and the potential matches and
					//push it onto the vector of relations
					trackRelat tr;
					tr.trk = currentTrack;
					tr.relate = potentialTracks;
#pragma omp critical(insertTRackRelate)
					{
						trackRelationsList.push_back(tr);
					}
				}
			}

			
			delete vectOfTracks;
			
			ITrack * trackletArray[countX];
			

			CostGraph::Graph g;


			//add two vertices for all left and right events plus a source and sink vertex
			CostGraph::size_type N((countX * 2) + 2);
			for (CostGraph::size_type i = 0; i < N; i++) {
				boost::add_vertex(g);
			}

			CostGraph::Capacity capacity = get(boost::edge_capacity, g);

			typedef property_map < CostGraph::Graph, edge_reverse_t >::type Reversed;
			Reversed rev = get(boost::edge_reverse, g);
			CostGraph::ResidualCapacity residual_capacity = get(boost::edge_residual_capacity, g);
			CostGraph::Weight weight = get(boost::edge_weight, g);


			CostGraph::EdgeAdder ea(g, weight,
			, rev, residual_capacity);

			//all edges will have a flow of 1
			int edgeCap = 1;
			//process all of the associations in the list of assocaitions
#pragma omp parallel for num_threads(8) schedule(dynamic, 20)
			for (uint i = 0; i < trackRelationsList.size(); i++) {

				trackRelat tr = trackRelationsList.at(i);

				//get the x index of the track to process
				int x = eventMap->at(tr.trk->getFirst()->getId());

				//add the tracklet to process to the array of events
				trackletArray[x] = tr.trk;

				//process each track associated with this track
				//cout << "TR.Relate Size: " << tr.relate->size() << endl;
				while (!tr.relate->empty()) {
					ITrack* tmp = tr.relate->back();
					tr.relate->pop_back();

					//get the y index of the track that is a potential match
					//to our current track.
					//cout << "IDy:"<<tmp->getFirst()->getId() << endl;
					int y = eventMap->at(tmp->getFirst()->getId());
					trackletArray[y] = tmp;
					//get the probability*multFactor of the two tracks belonging together
					//it is multiplied by multFactor because the algorithm uses int and not
					//a floating point value.

					//add the probability in the appropriate location
					double prob = this->prob(tr.trk, tmp);
					//printf("prob: %E \n", prob);
					int weightVal = -(int)(std::log(prob)*multFactor);
					//cout << "weight: " << weightVal << endl;
#pragma omp critical(graphEdges)
					{
						ea.addEdge(((x * 2) + 1), (y * 2), 1, edgeCap);
					}
				}

				delete tr.relate;
			}

			CostGraph::vertex_descriptor source, sink;
			//set source vertex
			source = (countX * 2);
			//cout << "Source: " << source << endl;
			//set sink vertex;
			sink = (countX * 2) + 1;
			//cout << "Sink: " << sink << endl;

			//add edges from source to tracklets
			//and from their second node to sink
			for (int j = 0; j < countX; j++) {

				ITrack* track = trackletArray[j];
				double Bi = this->getPoissonProb(track->getFirst());
				//printf("Bi: %E \n", Bi);
				int obsCost = (int)(std::log(Bi / (1.0 - Bi))*multFactor);
				//cout << "ObsCost: " << obsCost << endl;

				ea.addEdge((j * 2), ((j * 2) + 1), obsCost, edgeCap);

				double entPd = this->pEnter(track);
				/*printf("entP: %E \n", entPd);*/
				int entP = -(int)(std::log(entPd)*multFactor);
				//cout << "entP: " << entP << endl;

				ea.addEdge(source, (j * 2), entP, edgeCap);

				double exPd = this->pExit(track);
				/*printf("exP: %E \n", exPd);*/
				int exP = -(int)(std::log(exPd)*multFactor);
				//cout << "exP: " << exP << endl;

				ea.addEdge(((j * 2) + 1), sink, exP, edgeCap);

			}



			Graphs::successive_shortest_path(g, source, sink);

			long cost = boost::find_flow_cost(g);
			cout << "cost: " << cost << endl;
			boost::graph_traits<CostGraph::Graph>::vertex_iterator u_iter, u_end;
			boost::graph_traits<CostGraph::Graph>::out_edge_iterator ei, e_end;

			//get iterator for all vertices of the graph and process it up to the end iterator
			for (boost::tie(u_iter, u_end) = boost::vertices(g); u_iter != u_end; u_iter++) {

				//If the source vertex then we don't need to process it.
				//Similarly we don't want to process the first vertex added for a track fragment
				int x = *u_iter;
				if ((!(x % 2)) || (x == source))continue;

				//get all the edges going out of the current vertex and process them
				for (boost::tie(ei, e_end) = boost::out_edges(*u_iter, g); ei != e_end; ei++) {

					//If the capacity was a non-zero number then it is on the cost graph and not the residual capacity 
					//graph so we will process it.
					int eiCap = capacity[*ei];
					if (eiCap > 0) {

						//if the target for this edge is the sink we don't want to process it
						int y = boost::target(*ei, g);
						if (y == sink)continue;

						//if residual capacity is 0 then we are using it and need to process it
						int eiResidual = residual_capacity[*ei];
						if ((eiCap - eiResidual) == 1) {
							ITrack* leftTrack = trackletArray[x / 2];
							IEvent* leftEvent = leftTrack->getLast();
							ITrack* rightTrack = trackletArray[y / 2];
							IEvent* rightEvent = rightTrack->getFirst();

							//If the events do not have any others already assocaited with them.
							if (leftEvent->getNext() == NULL && rightEvent->getPrevious() == NULL){

								//A final sanity check, to make sure it isn't the same event detection we are trying to 
								//attach.  This would create an infinite loop (no good). This is probably not needed.
								if (leftEvent != rightEvent) {
									//cout << "Link x: " << x << " and y: " << y << endl;
									leftEvent->setNext(rightEvent);
									rightEvent->setPrevious(leftEvent);
								}
							}
						}
					}
				}
			}


			delete eventMap;
			trackRelationsList.clear();
		}

		cout << "------ finish upper stage ---------\n";




		return this->tracksIdxr->getAll();
	}

private:
	static constexpr int multFactor = 100;

	/*float getRandPoisssonProb(){
		int rv = std::rand();
		rv = 4 + (rv % 8);
		double lambda = 1.0;
		double lamPow = std::pow(lambda, rv);
		double lamExp = std::exp(-lambda);
		double fact = boost::math::factorial<double>(double(rv));
		return ((lamPow*lamExp) / fact);
		}*/

	double getPoissonProb(IEvent* evnt){
		bt::ptime strt = this->evntsIdxr->getFirstTime();
		bt::ptime strtEnd = strt + bt::seconds(this->timespan)*this->numSpan;
		bt::time_period range(strt, strtEnd);

		int lambda, delta;
		if (evnt->getTimePeriod().intersects(range)){
			strtEnd = strt + bt::seconds(this->timespan * this->numSpan);
			bt::time_period range2(strt, strtEnd);
			lambda = evntsIdxr->getExpectedCangePerFrame(range2);
			range2 = evnt->getTimePeriod();
			range2 = bt::time_period(range2.begin(), range2.end() + bt::seconds(this->timespan)*this->numSpan);
			delta = evntsIdxr->getExpectedCangePerFrame(range2);
		}
		else{
			bt::ptime end = this->evntsIdxr->getLastTime();
			strt = end - bt::seconds(this->timespan*this->numSpan);
			bt::time_period range2(strt, end);
			lambda = evntsIdxr->getExpectedCangePerFrame(range2);
			range2 = evnt->getTimePeriod();
			range2 = bt::time_period(range2.begin() - bt::seconds(this->timespan*this->numSpan), range2.end());
			delta = evntsIdxr->getExpectedCangePerFrame(range2);
		}

		/*cout << "Expected Change: " << lambda << endl;*/
		if (lambda > 0){

			double lamPow = std::pow(lambda, delta);
			double lamExp = std::exp(-lambda);
			double fact = boost::math::factorial<double>(double(delta));
			double retVal = ((lamPow*lamExp) / fact);
			return retVal;
		}
		else if (lambda == 0 && delta < 2){
			return 0.0000001;
		}
		else{
			return 0.98;
		}


	}

	void getHist(Mat& hist, IEvent* evnt, bool left) {

		if (evnt == NULL){
			Mat m0(1, 1, CV_32FC(3));
			bool uniform = true;
			bool accumulate = false;
			//upper bound on ranges are exclusive



			int channelList[] = { 0, 1, 2 };
			const int histSize = 15;
			int histSizes[] = { histSize, histSize, histSize };
			calcHist(&m0, 1, channelList, Mat(), hist, 3, histSizes, this->histRanges, uniform, accumulate);
		}
		else{
			try{


				//Mat m;
				/*this->db->getImageParam(evnt, m, left);*/
				vector< vector < vector<float> > >* paramsVect = this->db->getImageParam(evnt, left);

				typedef Vec<float, 10 > Vec10f;
				Mat m(paramsVect->size(), paramsVect->at(0).size(), CV_32FC(10));

				for (int i = 0; i < 10; i++) {
					for (int x = 0; x < paramsVect->size(); x++) {
						for (int y = 0; y < paramsVect->at(0).size(); y++) {
							m.at<Vec10f >(x, y)[i] = paramsVect->at(x).at(y).at(i);
						}
					}
				}

				/*delete paramsVect;*/

				bool uniform = true;
				bool accumulate = false;


				const int histSize = 15;

				int channelList[this->params->size()];
				int histSizes[this->params->size()];
				for (int i = 0; i < this->params->size(); i++){
					channelList[i] = params->at(i);
					histSizes[i] = histSize;
				}
				calcHist(&m, 1, channelList, Mat(), hist, this->params->size(), histSizes, this->histRanges, uniform, accumulate);
			}
			catch (Exception ex){
				Mat m0(1, 1, CV_32FC(3));
				bool uniform = true;
				bool accumulate = false;
				//upper bound on ranges are exclusive

				int channelList[] = { 0, 1, 2 };
				const int histSize = 15;
				int histSizes[] = { histSize, histSize, histSize };
				calcHist(&m0, 1, channelList, Mat(), hist, 3, histSizes, this->histRanges, uniform, accumulate);
				cout << "Issues with getting hist of image params" << endl;
			}
		}
	}

	double eventProb(IEvent * event, int idx) {

		//for each region in our array of regions, check if they are in the
		//search area for our next event.
		vector<Point2i> scaledSearchArea;

		Rect rec;
		if (event->getType() == "SG" || event->getType() == "FL") {
			Rect* r = event->getBBox();
			int x = r->x / regionDivisor;
			int y = r->y / regionDivisor;
			int width = r->width / regionDivisor;
			int height = r->height / regionDivisor;
			scaledSearchArea.push_back(Point2i(x, y));
			scaledSearchArea.push_back(Point2i(x, y + height));
			scaledSearchArea.push_back(Point2i(x + width, y + height));
			scaledSearchArea.push_back(Point2i(x + width, y));

			rec = Rect();
			rec.x = x;
			rec.y = y;
			rec.height = height;
			rec.width = width;
		}
		else {
			for (vector<Point2i>::iterator it = event->getShape()->begin(); it != event->getShape()->end(); it++) {
				Point2i p = it.operator *();
				scaledSearchArea.push_back(Point2i(p.x / regionDivisor, p.y / regionDivisor));
				rec = boundingRect(scaledSearchArea);
			}
		}

		double returnValue = 0;
		double minVal;
		bool isSet = false;
		int count = 0;
		//#pragma omp parallel for collapse(2) reduction(+:returnValue, count) 
		for (int i = rec.x - 2; i <= rec.x + rec.width + 2; i++) {
			for (int j = rec.y - 2; j <= rec.y + rec.height + 2; j++) {
				//if inside the search area and there are events associated with the location
				if (i >= regionDim || j >= regionDim || i < 0 || j < 0)continue;
				if (pointPolygonTest(scaledSearchArea, Point2i(i, j), false) >= 0) {

					if (!isSet){
						minVal = this->pValues[i][j][idx];
						isSet = true;
					}
					else{
						if (this->pValues[i][j][idx] < minVal){
							minVal = this->pValues[i][j][idx];
						}
					}
					returnValue += this->pValues[i][j][idx];
					count++;
				}
			}
		}

		//cout << "retCount: " << returnCount << endl;
		//printf("PentEx: %E \n", returnValue);
		//returnValue = returnValue / count;
		if (minVal > 0){
			returnValue = minVal / this->pValMax[idx];
		}
		else{
			returnValue = returnValue / count;
			returnValue = returnValue / this->pValMax[idx];
		}

		//if we can't calculate a value, who knows what the probability is.  We'll just cal it 50/50.
		if (returnValue <= 0){
			returnValue = 0.5;
		}

		return returnValue;
	}


	void popFile(string file, int idx) {
		string fileName = "/usr/local/share/tracking/prob_files/" + file + ".csv";
		ifstream myfile(fileName);
		if (myfile.is_open()) {
#pragma omp parallel for collapse(2)
			for (int i = 0; i < regionDim; i++) {
				for (int j = 0; j < regionDim; j++)this->pValues[i][j][idx] = 0;
			}


			string line;
			for (int i = 0; i < regionDim; i++) {
				getline(myfile, line);
				stringstream ss(line); //feed the string stream with the line
				for (int j = 0; j < regionDim - 1; j++) {

					string num;
					getline(ss, num, ',');
					this->pValues[j][i][idx] = ::atof(num.c_str());
					if (this->pValues[j][i][idx]>this->pValMax[idx]){
						this->pValMax[idx] = this->pValues[j][i][idx];
					}
				}
				string num;
				getline(ss, num);
				this->pValues[regionDim - 1][i][idx] = ::atof(num.c_str());
				if (this->pValues[regionDim - 1][i][idx] > this->pValMax[idx]){
					this->pValMax[idx] = this->pValues[regionDim - 1][i][idx];
				}
			}
			myfile.close();
		}
		else{
			cout << "fialed to open file";
		}
	}

	static constexpr double sqrt2Pi = sqrt(2 * M_PI);

	double calcNormProb(double x, double mean, double stdDev) {
		double val1 = normCDF(x - stdDev, mean, stdDev);
		double val2 = normCDF(x + stdDev, mean, stdDev);
		double val = val2 - val1;

		return val;
	}

	double normCDF(double x, double mean, double stdDev){
		double val = (x - mean) / (stdDev*sqrt(2.0));
		val = 1.0 + erf(val);
		val = val*0.5;
		return val;
	}

protected:


	virtual int getMaxFrameSkip() = 0;
	virtual double prob(ITrack* leftTrack, ITrack * rightTrack) = 0;




	double pEnter(ITrack* track){
		return this->eventProb(track->getFirst(), 0);
	}

	double pExit(ITrack* track){
		return this->eventProb(track->getLast(), 1);
	}



	double PFrameGap(ITrack* leftTrack, ITrack * rightTrack) {

		bt::ptime leftTime = leftTrack->getLast()->getTimePeriod().end();
		bt::ptime rightTime = rightTrack->getFirst()->getTimePeriod().begin();
		bt::time_duration diff = rightTime - leftTime;
		int span = (int)leftTrack->getLast()->getTimePeriod().length().total_seconds();
		int frameSkip = diff.total_seconds() / span;

		if (frameSkip) {
			double pExitVal = this->pExit(leftTrack);
			double pFalseNeg = 1 - (pExitVal);
			for (int i = 0; i < frameSkip; i++){
				pFalseNeg *= pFalseNeg;
				//cout << "FrameGap prob: " << pFalseNeg << endl;
			}
			return pFalseNeg;
		}
		else {

			return 1.0;
		}
	}

	double PAppearance(ITrack* leftTrack, ITrack * rightTrack) {


		Mat leftFrameHist, rightFrameHist;

#pragma omp parallel sections
		{
#pragma omp section
			{
				this->getHist(leftFrameHist, leftTrack->getLast(), true);
			}
#pragma omp section
			{
				this->getHist(rightFrameHist, rightTrack->getFirst(), false);
			}
		}


		double compVal = cv::compareHist(leftFrameHist, rightFrameHist, CV_COMP_BHATTACHARYYA);
		//cout << "Hist dist: " << compVal << endl;
		double sameProb, diffProb;

		//#pragma omp parallel sections 
		{
			//#pragma omp section
			{
				sameProb = this->calcNormProb(compVal, sameMean, sameStdDev);
			}
			//#pragma omp section
			{
				diffProb = this->calcNormProb(compVal, diffMean, diffStdDev);
			}
		}

		double prob = sameProb / (sameProb + diffProb);
		//cout << "Appear Prob: " << prob << endl;
		return prob;
	}


	void trackMovement(ITrack* inTrack, float* motionNormMean) {
		double xMovement = 0.0;
		double yMovement = 0.0;
		double totalTime = 0.0;
		int count = 0;

		IEvent* tmp = inTrack->getFirst();



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
			double xMean = xMovement / count;
			double yMean = yMovement / count;
			double tMean = totalTime / count;
			float xMeanPerTime = (float)(xMean / tMean);
			float yMeanPerTime = (float)(yMean / tMean);
			//float val = (xMeanPerTime * xMeanPerTime) + (yMeanPerTime * yMeanPerTime);
			//val = sqrt(val);

			motionNormMean[0] = xMeanPerTime;
			motionNormMean[1] = yMeanPerTime;
		}
		else {
			motionNormMean[0] = 0;
			motionNormMean[1] = 0;
		}
		return;
	}
};
#endif