/*
 * File:   ResultsWriter.cpp
 * Author: Dustin
 *
 * Created on January 6, 2015, 11:53 AM
 */

#ifndef RESULTSWRITER_CPP
#define	RESULTSWRITER_CPP

#include "../include/ITrack.hpp"
#include "ExtendedEvent.cpp"
#include "boost/date_time/posix_time/posix_time_types.hpp"
#include <boost/lexical_cast.hpp>
#include <fstream>
#include <algorithm>

class ResultsWriter {
public:

	ResultsWriter() {
	}

	~ResultsWriter() {
	}

	void writeResults(vector<ITrack*>* results, string fileLocation, string type) {

		ofstream outputFile((fileLocation + "/" + type + "DustinTracked.txt").c_str());
		if (outputFile.is_open()) {
			int id = 1;
			for (vector< ITrack* >::iterator iter = results->begin(); iter != results->end(); iter++) {
				ITrack* track = iter.operator *();
				vector<IEvent*>* events = track->getEvents();
				for (vector<IEvent*>::iterator evIter = events->begin(); evIter != events->end(); evIter++) {
					ExtendedEvent* ev = (ExtendedEvent*)evIter.operator *();
					string hgs_point_string = this->getPointString(ev->getHGSLocation());
					string hgs_bbox_string = this->getPolyString(ev->getHGSBBox());

					string hgs_polygon_string;
					if (ev->getHGSPoly() == NULL){
						hgs_polygon_string = "";
					}
					else{
						hgs_polygon_string = this->getPolyString(ev->getHGSPoly());
					}

					string hpc_point_string = this->getPointString(ev->getHPCLocation());
					string hpc_bbox_string = this->getPolyString(ev->getHPCBBox());

					string hpc_polygon_string;
					if (ev->getHPCPoly() == NULL){
						hpc_polygon_string = "";
					}
					else{
						hpc_polygon_string = this->getPolyString(ev->getHPCPoly());
					}

					string date_string = this->getDateString(ev->getTimePeriod().begin());
					string next_date_string;
					if (ev->getNext() != NULL) {
						next_date_string = this->getDateString(ev->getNext()->getTimePeriod().begin());
					}
					outputFile << id << "\t" << date_string << "\t" << next_date_string << "\t"
						<< ev->getType() << "\t" << hpc_point_string << "\t" << hpc_bbox_string << "\t" << hpc_polygon_string
						<< "\t" << hgs_point_string << "\t" << hgs_bbox_string << "\t" << hgs_polygon_string << "\t" << ev->getSpecificID() << endl;
					delete ev;
				}
				id++;
				delete events;
				delete track;
			}
			delete results;
			outputFile.flush();
			outputFile.close();
		}
	}
private:
	const std::locale format = std::locale(std::locale::classic(), new boost::posix_time::time_facet("%Y-%m-%d %H:%M:%S"));

	string getPolyString(vector<Point2d>* points) {
		string result = "POLYGON((";
		//cout << "size:" << points->size() << endl;
		for (vector<Point2d>::iterator iter = points->begin(); iter != points->end();) {
			Point2d p = iter.operator *();
			result.append(boost::lexical_cast<string> ((float)p.x));
			result.append(" ");
			result.append(boost::lexical_cast<string> ((float)p.y));
			iter++;
			if (iter == points->end())break;
			result.append(",");
		}
		result.append("))");
		return result;
	}

	string getPointString(Point2d* point) {
		string result = "POINT(";
		result.append(boost::lexical_cast<string> ((float)point->x));
		result.append(" ");
		result.append(boost::lexical_cast<string> ((float)point->y));
		result.append(")");
		return result;
	}

	string getDateString(boost::posix_time::ptime pt) {
		//        using namespace boost::posix_time;


		stringstream ss;
		ss.imbue(format);
		ss << pt;
		string result = ss.str();
		return result;
	}

};
#endif

