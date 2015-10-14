/*
* File:   InputReader.cpp
* Author: Dustin Kempton
*
* Created on December 20, 2014, 9:11 PM
*/

#ifndef INPUTREADER_CPP
#define	INPUTREADER_CPP

#include <TrackingFactory.h>
#include <vector>
#include <cstdlib>
#include <fstream>
//#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include "boost/date_time/posix_time/posix_time.hpp"

using namespace std;
using namespace cv;
namespace bt = boost::posix_time;

class InputReader{
	const std::locale format = std::locale(std::locale::classic(), new boost::posix_time::time_input_facet("%Y-%m-%d %H:%M:%S"));
	string sourceFolder;
	string eventType;
	int yrBeg, yrEnd, moBeg, moEnd, timeSpan;
public:

	InputReader(string sourceFolder, string eventType, int yrBeg, int yrEnd, int moBeg, int moEnd, int timeSpan){
		this->sourceFolder = sourceFolder;
		this->eventType = eventType;
		this->yrBeg = yrBeg;
		this->yrEnd = yrEnd;
		this->moBeg = moBeg;
		this->moEnd = moEnd;
		this->timeSpan = timeSpan;
	}

	vector<IEvent*>* readFiles() {
		int filesTried = 0;
		int filesCompleted = 0;
		int num = 0;
		vector<IEvent*>* eventsPtr = new vector<IEvent*>();

		for (int yr = this->yrBeg; yr < this->yrEnd; yr++){
			int end;
			if (yrEnd > yr + 1){
				end = 13;
			}else{
				end = this->moEnd;
			}

			for (int mo = this->moBeg; mo < end; mo++){
				filesTried++;
				std::string fileName;
				if (mo < 10){
					fileName = this->sourceFolder + "/" + eventType + "_";
					fileName.append(std::to_string(yr));
					fileName.append("_0");
					fileName.append(std::to_string(mo));
					fileName.append(".txt");
				}
				else{
					fileName = this->sourceFolder + "/" + eventType + "_";
					fileName.append(std::to_string(yr));
					fileName.append("_");
					fileName.append(std::to_string(mo));
					fileName.append(".txt");
				}

				ifstream myfile(fileName.c_str());
				if (myfile.is_open()) {
					string line;
					//ditch the header line
					getline(myfile, line);
					//while there is something to read, process it.
					while (getline(myfile, line)) {

						std::string eventId, eventTypeString, startTimeString, hpc_coord_string, hpc_bbox_string,
							hpc_ccode_string, hgs_coord_string, hgs_bbox_string, hgs_ccode_string,
							specificid, restOfLine;

						stringstream ss(line); //feed the string stream with the line
						//if there is something in the line, we will process it
						if (getline(ss, eventTypeString, '\t')) {
							getline(ss, startTimeString, '\t');

							getline(ss, hpc_coord_string, '\t');
							getline(ss, hpc_bbox_string, '\t');
							getline(ss, hpc_ccode_string, '\t');

							getline(ss, hgs_coord_string, '\t');
							getline(ss, hgs_bbox_string, '\t');
							getline(ss, hgs_ccode_string, '\t');
							getline(ss, specificid, '\t');
							getline(ss, restOfLine, '\n');
                            
                            cout << eventTypeString;
                            cout << startTimeString;
                            cout << hpc_coord_string;
                            cout << hpc_bbox_string;
                            cout << hpc_ccode_string;
                            cout << hgs_coord_string;
                            cout << hgs_bbox_string;
                            cout << hgs_ccode_stringl
                            cout << specificid;
                            cout << restOfLine;

							//lines have unneeded characters so remove them
							hpc_coord_string = this->removeBrackets(hpc_coord_string);
							hpc_bbox_string = this->removeBrackets(hpc_bbox_string);
							hpc_ccode_string = this->removeBrackets(hpc_ccode_string);

							hgs_coord_string = this->removeBrackets(hgs_coord_string);
							hgs_bbox_string = this->removeBrackets(hgs_bbox_string);
							hgs_ccode_string = this->removeBrackets(hgs_ccode_string);

							bool flip = false;//apparently this was needed once maybe change in the future as it is constant? 
							Point2d tmp_hpc_coord = this->getPoint(hpc_coord_string, flip);
							Point2d* hpc_coord = new Point2d(tmp_hpc_coord);
							vector<Point2d>* hpc_bbox = this->getPoly(hpc_bbox_string, flip);

							Point2d tmp_hgs_coord = this->getPoint(hgs_coord_string, flip);
							Point2d* hgs_coord = new Point2d(tmp_hgs_coord);
							vector<Point2d>* hgs_bbox = this->getPoly(hgs_bbox_string, flip);

							bt::ptime startIn;
							std::istringstream startStringStream(startTimeString);
							startStringStream.imbue(this->format);
							startStringStream >> startIn;

							bt::time_period* range;

							//I guess the AR and CH type are off by one time period (SPOCA module?)
							if (eventType == "AR" || eventType == "CH") {
								startIn = startIn + bt::seconds(this->timeSpan);
								bt::ptime endIn = startIn + bt::seconds(this->timeSpan);
								range = new bt::time_period(startIn, endIn);
							}
							else{
								bt::ptime endIn = startIn + bt::seconds(this->timeSpan);
								range = new bt::time_period(startIn, endIn);
							}

							//We are just going to use this to keep the rest of the information tacked onto the end of the event for now.
							specificid = specificid + "\t" + restOfLine;



							IEvent* ev;
							int id = num++;

							if (hpc_ccode_string.length()) {
								//not all events have a chain code representation 
								std::vector<Point2d>* hpc_ccode = this->getPoly(hpc_ccode_string, flip);
								std::vector<Point2d>* hgs_ccode = this->getPoly(hgs_ccode_string, flip);
								ev = TrackingFactory::GetNewEventWCCode(id, hpc_coord, hpc_bbox, hpc_ccode,
									hgs_coord, hgs_bbox, hgs_ccode, range, eventTypeString, specificid);
							}
							else {

								ev = TrackingFactory::GetNewEventNoCCode(id, hpc_coord, hpc_bbox,
									hgs_coord, hgs_bbox, range, eventTypeString,
									specificid);
							}

							//push the pointer to our new event onto the back of the list
							eventsPtr->push_back(ev);
						}
					}
					myfile.close();
					filesCompleted++;
				}
				else{
					cout << "filed to open file " << fileName << endl;
				}

			}
		}
		if (filesCompleted == filesTried){
			cout << "All files read." << endl;
		}
		else{
			cout << "Some files failed to read." << endl;
		}
		return eventsPtr;
	}
private:
	/**
	* getPoly: returns a list of 2D points extracted from the input string
	* the list of points are assumed to create a polygon, they are not tested
	* @param pointsString      :the string to extract the points from
	* @return          :returns the list of 2D points
	*/
	vector<Point2d>* getPoly(std::string pointsString, bool flip) {
		stringstream ss(pointsString);
		std::string xy;
		vector<Point2d>* pointsList = new vector<Point2d >();
		while (getline(ss, xy, ',')) {
			pointsList->push_back(this->getPoint(xy, flip));
		}
		getline(ss, xy);
		pointsList->push_back(this->getPoint(xy, flip));

		return pointsList;
	}

	/**
	* getPoint :returns a 2D point from the input string
	* @param xy        :input string containing x and y coordinate
	* @return  :the 2D point extracted from the string
	*/
	Point2d getPoint(string xy, bool flip) {
		unsigned spaceIdx = xy.find(' ');
		float x = atof(xy.substr(0, spaceIdx).c_str());
		float y = atof(xy.substr(spaceIdx).c_str());

		if (flip){
			return Point2d(-1.0*x, y);
		}
		else{
			return Point2d(x, y);
		}
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
};
#endif /* INPUTREADER_CPP */