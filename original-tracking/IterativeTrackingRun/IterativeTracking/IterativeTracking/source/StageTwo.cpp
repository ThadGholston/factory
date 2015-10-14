/* 
 * File:   StageTwo.cpp
 * Author: Dustin
 * 
 * Created on December 23, 2013, 2:52 PM
 */
#ifndef STAGETWO_CPP
#define	STAGETWO_CPP
#include "BaseUpperStage.cpp"
#include "../localInclude/IStageTwoThree.hpp"


using namespace std;

class StageTwo : public BaseUpperStage {
private:
    int maxFrameSkip;

    int getMaxFrameSkip() {
        return maxFrameSkip;
    }
public:

	StageTwo(IDBConnection* db, IPositionPredictor* predictor, ITrackIndexer* tracksDB, IEventIndexer* evntIdxr, int maxFrameSkip, int timespan, double sameMean,
		double sameStdDev, double diffMean, double diffStdDev, vector<float*>* histRangesVec, vector<int>* params)
		: BaseUpperStage(db, predictor, tracksDB, evntIdxr, timespan, sameMean, sameStdDev, diffMean, diffStdDev, histRangesVec, params) {
        this->maxFrameSkip = maxFrameSkip;
    }

protected:

    double prob(ITrack* leftTrack, ITrack * rightTrack) {
        double p = 1;
#pragma omp parallel sections reduction(*:p)
        {
#pragma omp section
            {
                p *= this->PAppearance(leftTrack, rightTrack);
            }
#pragma omp section
            {
                p *= this->PFrameGap(leftTrack, rightTrack);
            }
        }
        
        return p;
    }

};

#endif


