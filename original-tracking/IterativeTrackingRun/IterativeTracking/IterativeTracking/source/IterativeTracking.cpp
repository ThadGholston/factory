#include <TrackingFactory.h>
#include <fstream>
#include <cstdlib>

#include "../include/IterativeTracking.h"
#include "StageOne.cpp"
#include "StageTwo.cpp"
#include "StageThree.cpp"
#include "InputReader.cpp"


std::string sourceFolder, eventType, dbConfigFile, idxConfigFile;
double sameMean, sameStdDev, diffMean, diffStdDev;
int yrBeg, yrEnd, moBeg, moEnd, timeSpan;
int gap2, gap3, gap4;
vector<float*>* histRangesVec = NULL;
vector<int>* params = NULL;

extern "C"{
	namespace IterativeTracking{

		bool Init(std::string configFile){

			//////////////////////////////////////////////////////////////////////////
			//Clean up previous init.
			if (params != NULL){
				delete params;
				params = NULL;
			}

			if (histRangesVec != NULL){
				for (int i = 0; i < histRangesVec->size(); i++){
					delete[] histRangesVec->at(i);
				}
				delete histRangesVec;
				histRangesVec = NULL;
			}

			//////////////////////////////////////////////////////////////////////////
			//Try to init
			try{

				ifstream myfile(configFile);
				if (myfile.is_open()) {
					string line;

					//////////////////////////////////////////////////////////////////////////
					//First line is the folder that the event files are stored
					{
						//while there is something to read, process it.
						getline(myfile, line);//get the header line;
						if (line.compare("sourceFolder") == 0){
							getline(myfile, sourceFolder);//get dbConFig line;

						}
						else{
							cout << "First line not sourceFolder" << endl;
							return false;
						}
					}
					//////////////////////////////////////////////////////////////////////////
					//Second line is event type
					{
						getline(myfile, line);//throw away blank line;
						getline(myfile, line);//get title of next set of info;
						if (line.compare("event_type") == 0){
							getline(myfile, eventType);//get eventType line;

						}
						else{
							cout << "Second element must be type" << endl;
							return false;
						}
					}
					//////////////////////////////////////////////////////////////////////////
					//Third line is the location and name of the config file for the db connection
					{
						getline(myfile, line);//throw away blank line;
						getline(myfile, line);//get title of next set of info;
						if (line.compare("dbConfigFile") == 0){
							getline(myfile, dbConfigFile);//get dbConFig line;

						}
						else{
							cout << "Third element must be dbConfigFile" << endl;
							return false;
						}
					}
					//////////////////////////////////////////////////////////////////////////
					//Fourth line is the location and name for the config file for the indexes
					{
						getline(myfile, line);//throw away blank line;
						getline(myfile, line);//get title of next set of info;
						if (line.compare("idxConfigFile") == 0){
							getline(myfile, idxConfigFile);//get idxConfigFile line

						}
						else{
							cout << "Fourth element must be idxConfigFile" << endl;
							return false;
						}
					}
					//////////////////////////////////////////////////////////////////////////
					//Fifth line contains the mean and std dev for the distributions of distances
					{
						getline(myfile, line);//throw away blank line;
						getline(myfile, line);//get title of next set of info;
						stringstream ss(line); //feed the string stream with the line of database information
						string lineTitle;
						getline(ss, lineTitle, '\t');
						if (lineTitle.compare("sameMean") == 0){
							//set up stringstream to read the values
							string lineVal, line2;
							getline(myfile, line2);
							stringstream ss2(line2);

							//read sameMean from line of values
							getline(ss2, lineVal, '\t');
							sameMean = atof(lineVal.c_str());

							//////////////////////////////////////////////////////////////////////////
							//check next title value
							getline(ss, lineTitle, '\t');
							if (lineTitle.compare("sameStdDev") == 0){
								//read sameStdDev from line of values
								getline(ss2, lineVal, '\t');
								sameStdDev = atof(lineVal.c_str());
							}
							else{
								cout << "Failed on sameStdDev" << endl;
								return false;
							}

							//////////////////////////////////////////////////////////////////////////
							//check next title value
							getline(ss, lineTitle, '\t');
							if (lineTitle.compare("diffMean") == 0){
								//read sameStdDev from line of values
								getline(ss2, lineVal, '\t');
								diffMean = atof(lineVal.c_str());
							}
							else{
								cout << "Failed on diffMean" << endl;
								return false;
							}

							//////////////////////////////////////////////////////////////////////////
							//check next title value
							getline(ss, lineTitle);
							if (lineTitle.compare("diffStdDev") == 0){
								//read sameStdDev from line of values
								getline(ss2, lineVal);
								diffStdDev = atof(lineVal.c_str());
							}
							else{
								cout << "Failed on diffStdDev" << endl;
								return false;
							}
							//////////////////////////////////////////////////////////////////////////

						}
						else{
							cout << "Failed on sameMean" << endl;
							return false;
						}
					}
					//////////////////////////////////////////////////////////////////////////
					//Sixth line contains the beginning and ending month/year of data to process.
					{
						getline(myfile, line);//throw away blank line;
						getline(myfile, line);//get title of next set of info;
						stringstream ss(line); //feed the string stream with the line of database information
						string lineTitle;
						getline(ss, lineTitle, '\t');
						if (lineTitle.compare("yrBeg") == 0){
							//set up stringstream to read the values
							string lineVal, line2;
							getline(myfile, line2);
							stringstream ss2(line2);

							//read yrBeg from line of values
							getline(ss2, lineVal, '\t');
							yrBeg = atoi(lineVal.c_str());

							//////////////////////////////////////////////////////////////////////////
							//check next title value
							getline(ss, lineTitle, '\t');
							if (lineTitle.compare("yrEnd") == 0){
								//read sameStdDev from line of values
								getline(ss2, lineVal, '\t');
								yrEnd = atoi(lineVal.c_str());
							}
							else{
								cout << "Failed on yrEnd" << endl;
								return false;
							}

							//////////////////////////////////////////////////////////////////////////
							//check next title value
							getline(ss, lineTitle, '\t');
							if (lineTitle.compare("moBeg") == 0){
								//read sameStdDev from line of values
								getline(ss2, lineVal, '\t');
								moBeg = atoi(lineVal.c_str());
							}
							else{
								cout << "Failed on moBeg" << endl;
								return false;
							}

							//////////////////////////////////////////////////////////////////////////
							//check next title value
							getline(ss, lineTitle, '\t');
							if (lineTitle.compare("moEnd") == 0){
								//read sameStdDev from line of values
								getline(ss2, lineVal, '\t');
								moEnd = atoi(lineVal.c_str());
							}
							else{
								cout << "Failed on moEnd" << endl;
								return false;
							}
							//////////////////////////////////////////////////////////////////////////
							//check next title value
							getline(ss, lineTitle);
							if (lineTitle.compare("timeSpan") == 0){
								//read sameStdDev from line of values
								getline(ss2, lineVal, '\t');
								timeSpan = atoi(lineVal.c_str());
							}
							else{
								cout << "Failed on timeSpan" << endl;
								return false;
							}
							//////////////////////////////////////////////////////////////////////////

						}
						else{
							cout << "Failed on yrBeg" << endl;
							return false;
						}
					}
					//////////////////////////////////////////////////////////////////////////
					//Seventh line contains the gaps allowed between detections for 2nd third and fourth iterations
					{
						getline(myfile, line);//throw away blank line;
						getline(myfile, line);//get title of next set of info;
						stringstream ss(line); //feed the string stream with the line of database information
						string lineTitle;
						getline(ss, lineTitle, '\t');
						if (lineTitle.compare("gap2") == 0){
							//set up stringstream to read the values
							string lineVal, line2;
							getline(myfile, line2);
							stringstream ss2(line2);

							//read gap2 from line of values
							getline(ss2, lineVal, '\t');
							gap2 = atoi(lineVal.c_str());

							//////////////////////////////////////////////////////////////////////////
							//check next title value
							getline(ss, lineTitle, '\t');
							if (lineTitle.compare("gap3") == 0){
								//read gap3 from line of values
								getline(ss2, lineVal, '\t');
								gap3 = atoi(lineVal.c_str());
							}
							else{
								cout << "Failed on gap3" << endl;
								return false;
							}

							//////////////////////////////////////////////////////////////////////////
							//check next title value
							getline(ss, lineTitle);
							if (lineTitle.compare("gap4") == 0){
								//read gap4 from line of values
								getline(ss2, lineVal, '\t');
								gap4 = atoi(lineVal.c_str());
							}
							else{
								cout << "Failed on gap4" << endl;
								return false;
							}
							//////////////////////////////////////////////////////////////////////////

						}
						else{
							cout << "Failed on gap2" << endl;
							return false;
						}
					}
					//////////////////////////////////////////////////////////////////////////
					//Eighth line contains the range for the histograms of image parameters
					{
						getline(myfile, line);//throw away blank line;
						getline(myfile, line);//get title of next set of info;
						stringstream ss(line); //feed the string stream with the line of database information
						string lineTitle;
						getline(ss, lineTitle, '\t');
						if (lineTitle.compare("histRange1") == 0){
							//set up stringstream to read the values
							string lineVal, line2;
							getline(myfile, line2);
							stringstream ss2(line2);

							//read histRange1 from line
							getline(ss2, lineVal, '\t');
							stringstream ss3(lineVal);
							string tmpString;
							getline(ss3, tmpString, ',');
							float tmp1 = atof(tmpString.c_str());
							getline(ss3, tmpString);
							float tmp2 = atof(tmpString.c_str());
							float* histRange1 = new float[2];
							histRange1[0] = tmp1;
							histRange1[1] = tmp2;
							histRangesVec = new vector<float*>();
							histRangesVec->push_back(histRange1);
							//////////////////////////////////////////////////////////////////////////
							//check next title value
							while (getline(ss, lineTitle, '\t')){
								//read histRange from line of values
								getline(ss2, lineVal, '\t');
								stringstream ss3(lineVal);
								string tmpString;
								getline(ss3, tmpString, ',');
								float tmp1 = atof(tmpString.c_str());
								getline(ss3, tmpString);
								float tmp2 = atof(tmpString.c_str());
								float* histRange2 = new float[2];
								histRange2[0] = tmp1;
								histRange2[1] = tmp2;
								histRangesVec->push_back(histRange2);
							}
						}
						else{
							cout << "Failed on histRange1" << endl;
							return false;
						}
					}
					//////////////////////////////////////////////////////////////////////////
					//Nineth line contains the params to use for the histograms
					{
						getline(myfile, line);//throw away blank line;
						getline(myfile, line);//get title of next set of info;
						stringstream ss(line); //feed the string stream with the line of database information
						string lineTitle;
						getline(ss, lineTitle, '\t');
						if (lineTitle.compare("param1") == 0){
							//set up stringstream to read the values
							string lineVal, line2;
							getline(myfile, line2);
							stringstream ss2(line2);

							//read histRange1 from line
							getline(ss2, lineVal, '\t');
							int param1 = atoi(lineVal.c_str());
							params = new vector<int>();
							params->push_back(param1);
							//////////////////////////////////////////////////////////////////////////
							//check next title value
							while (getline(ss, lineTitle, '\t')){
								getline(ss2, lineVal, '\t');
								int param = atoi(lineVal.c_str());
								params->push_back(param);
							}
						}
						else{
							cout << "Failed on param1" << endl;
							return false;
						}
						//done close the file and return
						myfile.close();
						return true;
					}
				}
				else{
					cout << "Failed to open file" << endl;
					return false;
				}


			}
			catch (Exception ex){
				return false;
			}
		}

		vector<ITrack*>* TrackEvents(){

			InputReader rdr(sourceFolder, eventType, yrBeg, yrEnd, moBeg, moEnd, timeSpan);
			/*vector<IEvent*>* events = rdr.readFiles();*/
			IEventIndexer* eventIdx = TrackingFactory::Indexing::GetEventIndexer(idxConfigFile, rdr.readFiles());
			IPositionPredictor* pred = TrackingFactory::GetPositionPredictor();
			IDBConnection* db = TrackingFactory::DBConnect::GetDBConnection(dbConfigFile);

			//Process stage one
			StageOne s1(eventIdx, pred);
			vector<ITrack*>* tracks = s1.process();
			cout << "Tracks after Stage1: " << tracks->size() << endl;


			//Process stage two
			ITrackIndexer* trkIdxr1 = TrackingFactory::Indexing::GetTrackIndexer(idxConfigFile, tracks);
			StageTwo s2(db, pred, trkIdxr1, eventIdx, gap2, timeSpan, sameMean, sameStdDev, diffMean, diffStdDev, histRangesVec, params);
			tracks = s2.process();
			cout << "Tracks after Stage2: " << tracks->size() << endl;

			//Process stage three
			ITrackIndexer* trkIdxr2 = TrackingFactory::Indexing::GetTrackIndexer(idxConfigFile, tracks);
			StageThree s3(db, pred, trkIdxr2, eventIdx, gap3, timeSpan, sameMean, sameStdDev, diffMean, diffStdDev, histRangesVec, params);
			tracks = s3.process();
			cout << "Tracks after Stage3: " << tracks->size() << endl;

			//Process stage four
			ITrackIndexer* trkIdxr3 = TrackingFactory::Indexing::GetTrackIndexer(idxConfigFile, tracks);
			StageThree s4(db, pred, trkIdxr3, eventIdx, gap4, timeSpan, sameMean, sameStdDev, diffMean, diffStdDev, histRangesVec, params);
			tracks = s4.process();
			cout << "Tracks after Stage4: " << tracks->size() << endl;

			delete trkIdxr1;
			delete trkIdxr2;
			delete trkIdxr3;
			delete pred;
			delete eventIdx;
			delete db;
			/*delete events;*/

			return tracks;
		}

		vector<IEvent*>* ReadEventsFromSource(){
			InputReader rdr(sourceFolder, eventType, yrBeg, yrEnd, moBeg, moEnd, timeSpan);
			return rdr.readFiles();
		}
	}
}