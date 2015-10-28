/*
 * File:   KarthikResultsReaderDB.cpp
 * Author: Dustin
 *
 * Created on January 24, 2014, 8:50 AM
 */

#ifndef RESULTREADER_CPP
#define	RESULTREADER_CPP
#include <fstream>
#include <cstdlib>
#include <iostream>
#include <opencv2/opencv.hpp>
#include "ExtendedEvent.cpp"
#include "../include/IPointConverter.hpp"
#include "Track.cpp"
#include <stdexcept>


using namespace std;
using namespace cv;
namespace bt = boost::posix_time;

class ResultsReader {
private:
	const std::locale format = std::locale(std::locale::classic(), new boost::posix_time::time_input_facet("%Y-%m-%d %H:%M:%S"));
	static constexpr double CDELT = 0.599733;
	static constexpr double HPCCENTER = 4096.0 / 2.0;

	//static constexpr int ARTIMESPAN = 12600;//10800;
	//static constexpr int EFTIMESPAN = 9600;
	//static constexpr int SGTIMESPAN = 3200;
	//static constexpr int FITIMESPAN = 37800; //32400;
	//static constexpr int SSTIMESPAN = 16200;//18000;
	//static constexpr int CHTIMESPAN = 12600;

	IPointConverter* convtr;
	//IObjectFactory* factory;
	string eventFilesLocation;
	int span;

public:


	ResultsReader(IPointConverter* convtr, string eventFilesLocation, int span) {
		if (convtr == NULL) throw invalid_argument("PositionConverter cannot be null");
		this->convtr = convtr;
		this->eventFilesLocation = eventFilesLocation;
		this->span = span;

	}

	~ResultsReader() {

	}

	vector<ITrack*>* getTracks(string type) {
		return this->readFile(type);
	}

private:

	vector<ITrack*>* readFile(string eventType) {

		vector<ITrack*>* returnList = new vector<ITrack*>();

		ifstream myfile((this->eventFilesLocation + "/" + eventType + "DustinTracked.txt").c_str());

		if (myfile.is_open()) {
			string line;


			//while there is something to read, process it.
			int lastTrackId = 0;
			int count = 0;
			IEvent* lastEvent;
			while (getline(myfile, line)) {

				string trackIdString, eventId, eventTypeString, startTimeString, endTimeString, hpc_coord_string, hpc_bbox_string, hgs_coord_string,
					hgs_bbox_string, hgs_ccode_string, hpc_ccode_string;

				stringstream ss(line); //feed the string stream with the line
				//if there is something in the line, we will process it
				if (getline(ss, trackIdString, '\t')) {
					int trackId = atoi(trackIdString.c_str());

					getline(ss, startTimeString, '\t');
					getline(ss, endTimeString, '\t');
					getline(ss, eventTypeString, '\t');


					getline(ss, hpc_coord_string, '\t');
					getline(ss, hpc_bbox_string, '\t');
					getline(ss, hpc_ccode_string, '\t');

					getline(ss, hgs_coord_string, '\t');
					getline(ss, hgs_bbox_string, '\t');
					getline(ss, hgs_ccode_string, '\t');
					//getline(ss, specificid);
					//int id = atoi(specificid.c_str());


					//lines have unneeded characters so remove them
					hpc_coord_string = this->removeBrackets(hpc_coord_string);
					hpc_bbox_string = this->removeBrackets(hpc_bbox_string);


					Point2d tmp_coord = this->getPoint(hpc_coord_string);
					Point2d* hpc_coord = new Point2d(tmp_coord);
					vector<Point2d>* hpc_bbox = this->getPoly(hpc_bbox_string);


					hgs_coord_string = this->removeBrackets(hgs_coord_string);
					hgs_bbox_string = this->removeBrackets(hgs_bbox_string);


					tmp_coord = this->getPoint(hgs_coord_string);
					Point2d* hgs_coord = new Point2d(tmp_coord);
					vector<Point2d>* hgs_bbox = this->getPoly(hgs_bbox_string);

					vector<Point2d>* hpc_ccode;
					vector<Point2d>* hgs_ccode;

					if (!hpc_ccode_string.empty()){
						hpc_ccode_string = this->removeBrackets(hpc_ccode_string);
						hpc_ccode = this->getPoly(hpc_ccode_string);

						hgs_ccode_string = this->removeBrackets(hgs_ccode_string);
						hgs_ccode = this->getPoly(hgs_ccode_string);
					}
					else{
						hpc_ccode = this->getPoly(hpc_bbox_string);
						hgs_ccode = this->getPoly(hgs_bbox_string);
					}

					bt::ptime startIn, endIn;
					std::istringstream startStringStream(startTimeString);
					startStringStream.imbue(this->format);
					startStringStream >> startIn;

					bt::time_period* range = new bt::time_period(startIn, startIn + bt::seconds(this->span));
					



					/* IEvent* event = this->factory->getNewEvent(id, coord, this->getRect(bbox), bbox, range, eventTypeString);*/
					IEvent* ev = new ExtendedEvent(this->convtr, trackId, hpc_coord, hpc_bbox, hpc_ccode, hgs_coord, hgs_bbox, hgs_ccode, range, eventTypeString, std::to_string(count++));

					if (trackId == lastTrackId) {
						lastEvent->setNext(ev);
						ev->setPrevious(lastEvent);
						bt::time_period tmpRange = lastEvent->getTimePeriod();
						bt::time_period* newRng = new bt::time_period(tmpRange.begin(), range->begin() + bt::seconds(1));
						lastEvent->updateTimePeriod(newRng);
						lastEvent = ev;

					}
					else {
						//ITrack* track = this->factory->getNewTrack(event, event);
						ITrack* track = new Track(ev, ev);
						returnList->push_back(track);
						lastEvent = ev;
						lastTrackId = trackId;
					}

				} 
				else{
					cout << "Id did not Read!" << endl;
				}
			}
			myfile.close();

		}
		return returnList;
	}

	/**
	 * getPoly: returns a list of 2D points extracted from the input string
	 * the list of points are assumed to create a polygon, they are not tested
	 * @param pointsString      :the string to extract the points from
	 * @return          :returns the list of 2D points
	 */
	vector<Point2d>* getPoly(string pointsString) {
		stringstream ss(pointsString);
		string xy;
		vector<Point2d>* pointsList = new vector<Point2d >();
		while (getline(ss, xy, ',')) {
			pointsList->push_back(this->getPoint(xy));
		}
		getline(ss, xy);
		pointsList->push_back(this->getPoint(xy));
		return pointsList;
	}

	/**
	 * getPoint :returns a 2D point from the input string
	 * @param xy        :input string containing x and y coordinate
	 * @return  :the 2D point extracted from the string
	 */
	Point2d getPoint(string xy) {
		unsigned spaceIdx = xy.find(' ');
		float x = atof(xy.substr(0, spaceIdx).c_str());
		float y = atof(xy.substr(spaceIdx).c_str());
		return Point2d(x, y);
	}

	/**
	 * removeBrackets   : used to remove anything preceeding or following a ( or a )
	 * @param in        : the string to trim up
	 * @return          : the trimmed string
	 */
	string removeBrackets(string in) {
		int begin = in.find('(');
		int end = in.find(')');
		while (begin >= 0 || end >= 0) {
			in = in.substr(begin + 1, end - begin - 1);
			begin = in.find('(');
			end = in.find(')');
		}
		return in;
	}

	Rect * getRect(vector<Point2i>* poly) {
		int minX, minY, maxX, maxY;
		vector<Point2i>::iterator iterator = poly->begin();
		Point2d point = iterator.operator *();
		// Point2i point = this->convertToPixXY(pointf);
		minX = point.x;
		maxX = point.x;
		minY = point.y;
		maxY = point.y;
		iterator++;
		for (; iterator != poly->end(); ++iterator) {
			//            Point2d pointf = iterator.operator *();
			Point2i point = iterator.operator *();
			if (point.x < minX)minX = point.x;
			if (point.x > maxX)maxX = point.x;
			if (point.y < minY)minY = point.y;
			if (point.y > maxY)maxY = point.y;
		}

		Rect* rec = new Rect();
		rec->x = minX;
		rec->y = minY;
		rec->height = maxY - minY;
		rec->width = maxX - minX;
		return rec;
	}
};
#endif