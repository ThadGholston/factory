#include <iostream>
#include "TrackingFactory.h"
#include "IterativeTracking.h"
#include <vector>

void Exp3()
{
	vector<string> types;
	types.push_back("CH");
	types.push_back("AR");
	//types.push_back("EF");
	//types.push_back("FI");
	//types.push_back("SG");
	//types.push_back("SS");
	int yrBeg = 2012;
	int yrEnd = 2014;
	int moBeg = 1;
	int moEnd = 13;
	for (int yr = yrBeg; yr < yrEnd; yr++){
		for (int mo = moBeg; mo < moEnd; mo++){

			for (int i = 0; i < types.size(); i++){
				string cfgFile = "../config/exp3/";
				cfgFile.append(to_string(yr));
				cfgFile.append("/");
				if (mo < 10){
					cfgFile.append("0");
					cfgFile.append(to_string(mo));
					cfgFile.append("/");
				}
				else{
					cfgFile.append(to_string(mo));
					cfgFile.append("/");
				}
				cfgFile = cfgFile + types.at(i);
				cfgFile += "_IterativeTracking.cfg";
				IterativeTracking::Init(cfgFile);
				vector<ITrack*>* trks = IterativeTracking::TrackEvents();
				string saveLoc = "/netdrive/files/exp3/";
				saveLoc.append(to_string(yr));
				saveLoc.append("/");
				saveLoc.append(to_string(mo));
				saveLoc.append("/");

				//TrackingFactory::IO::WriteTracksToFile(trks, saveLoc, types.at(i));
			}
		}
	}
}

void run1Mo()
{
	vector<string> types;
	types.push_back("CH");
	types.push_back("AR");
	types.push_back("EF");
	types.push_back("FI");
	types.push_back("SG");
	types.push_back("SS");

	string saveLoc = "/netdrive/files/tmp/data/1Mo/";

	for (int i = 0; i < types.size(); i++){
		string cfgFile = "../config/1Mo/";

		cfgFile = cfgFile + types.at(i);
		cfgFile += "_IterativeTracking.cfg";
		IterativeTracking::Init(cfgFile);
		vector<ITrack*>* trks = IterativeTracking::TrackEvents();
		//TrackingFactory::IO::WriteTracksToFile(trks, saveLoc, types.at(i));
	}
}


void run3Mo()
{
	vector<string> types;
	types.push_back("CH");
	types.push_back("AR");
	types.push_back("EF");
	types.push_back("FI");
	types.push_back("SG");
	types.push_back("SS");

	string saveLoc = "/netdrive/files/tmp/data/3Mo/";

	for (int i = 0; i < types.size(); i++){
		string cfgFile = "../config/3Mo/";

		cfgFile = cfgFile + types.at(i);
		cfgFile += "_IterativeTracking.cfg";
		IterativeTracking::Init(cfgFile);
		vector<ITrack*>* trks = IterativeTracking::TrackEvents();
		//TrackingFactory::IO::WriteTracksToFile(trks, saveLoc, types.at(i));
	}
}

void run6Mo()
{
	vector<string> types;
	types.push_back("CH");
	types.push_back("AR");
	types.push_back("EF");
	types.push_back("FI");
	types.push_back("SG");
	types.push_back("SS");

	string saveLoc = "/netdrive/files/tmp/data/6Mo/";

	for (int i = 0; i < types.size(); i++){
		string cfgFile = "../config/6Mo/";

		cfgFile = cfgFile + types.at(i);
		cfgFile += "_IterativeTracking.cfg";
		IterativeTracking::Init(cfgFile);
		vector<ITrack*>* trks = IterativeTracking::TrackEvents();
		//TrackingFactory::IO::WriteTracksToFile(trks, saveLoc, types.at(i));
	}
}

void run12Mo()
{
	vector<string> types;
	types.push_back("CH");
	types.push_back("AR");
	types.push_back("EF");
	types.push_back("FI");
	types.push_back("SG");
	types.push_back("SS");

	string saveLoc = "/netdrive/files/tmp/data/12Mo/";

	for (int i = 0; i < types.size(); i++){
		string cfgFile = "../config/12Mo/";

		cfgFile = cfgFile + types.at(i);
		cfgFile += "_IterativeTracking.cfg";
		IterativeTracking::Init(cfgFile);
		vector<ITrack*>* trks = IterativeTracking::TrackEvents();
		//TrackingFactory::IO::WriteTracksToFile(trks, saveLoc, types.at(i));
	}
}


void run24Mo()
{
	vector<string> types;
	types.push_back("CH");
	types.push_back("AR");
	types.push_back("EF");
	types.push_back("FI");
	types.push_back("SG");
	types.push_back("SS");

	string saveLoc = "/netdrive/files/tmp/data/24Mo/";

	for (int i = 0; i < types.size(); i++){
		string cfgFile = "../config/24Mo/";

		cfgFile = cfgFile + types.at(i);
		cfgFile += "_IterativeTracking.cfg";
		IterativeTracking::Init(cfgFile);
		vector<ITrack*>* trks = IterativeTracking::TrackEvents();
		//TrackingFactory::IO::WriteTracksToFile(trks, saveLoc, types.at(i));
	}
}

using namespace std;

int main(int argc, char *argv[])
{
	//Exp3();

	run3Mo();
	run6Mo();


}