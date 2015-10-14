#pragma once

#include "ITrack.hpp"
#include <vector>

#ifdef __cplusplus
extern "C" {
#endif
	namespace IterativeTracking{
		bool Init(std::string confgFile);
		vector<ITrack*>* TrackEvents();
		vector<IEvent*>* ReadEventsFromSource();
	}

#ifdef __cplusplus
}
#endif
