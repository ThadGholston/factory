/*
 * File:   StageThree.cpp
 * Author: Dustin
 *
 * Created on January 11, 2014, 1:27 PM
 */
#ifndef STAGETHREE_CPP
#define	STAGETHREE_CPP
#include <math.h>
#include "BaseUpperStage.cpp"
#include "../localInclude/IStageTwoThree.hpp"

using namespace std;

class StageThree : public BaseUpperStage {
private:

	int maxFrameSkip;

	int getMaxFrameSkip() {
		return maxFrameSkip;
	}

public:

	StageThree(IDBConnection* db, IPositionPredictor* predictor, ITrackIndexer* tracksDB, IEventIndexer* evntIdxr, int maxFrameSkip, int timespan, double sameMean,
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
#pragma omp section
			{
				p *= this->PMotionModel(leftTrack, rightTrack);
			}
		}

		return p;
	}

private:

	double PMotionModel(ITrack* leftTrack, ITrack* rightTrack) {
		double leftMotion[2];
		double rightMotion[2];

		this->trackNormalizedMeanMovement(leftTrack, leftMotion);
		this->trackNormalizedMeanMovement(rightTrack, rightMotion);

		double xdiff = leftMotion[0] - rightMotion[0];
		//cout << "Xdiff: " << xdiff << endl;
		double ydiff = leftMotion[1] - rightMotion[1];
		//cout << "Ydiff: " << ydiff << endl;

		double val = (xdiff * xdiff) + (ydiff * ydiff);
		//cout << "Val before sqrt: " << val << endl;
		val = sqrt(val);
		//cout << "After sqrt: " << val << endl;
		double prob = 1.0 - (0.5 * val);
		//cout << "Prob: " << prob << endl;
		return prob;
	}

	void trackNormalizedMeanMovement(ITrack* track, double* motionNormMean) {
		double xMovement = 0.0;
		double yMovement = 0.0;
		IEvent* tmp = track->getFirst();
		int count = 0;
		while (tmp->getNext() != NULL) {
			IEvent* tmp2 = tmp->getNext();
			Point2i* tmp2Loc = tmp2->getLocation();
			Point2i* tmpLoc = tmp->getLocation();
			xMovement += tmp2Loc->x - tmpLoc->x;
			yMovement += tmp2Loc->y - tmpLoc->y;
			tmp = tmp2;
			count++;
		}


		if (count > 0) {
			//average the movement
			double xMean = xMovement / count;
			double yMean = yMovement / count;

			//Now normalize the movement
			double val = (xMean * xMean) + (yMean * yMean);
			val = sqrt(val);

			//Store in array for return
			motionNormMean[0] = xMean / val;
			motionNormMean[1] = yMean / val;
		}
		else {
			motionNormMean[0] = 0;
			motionNormMean[1] = 0;
		}

		return;
	}

};
#endif