#pragma once
#include "ITrack.hpp"
#include "IEvent.hpp"
#include "IEventIndexer.hpp"
#include "ITrackIndexer.hpp"
#include "IDBConnection.hpp"
#include "IPointConverter.hpp"
#include "IPositionPredictor.hpp"

#include <vector>

#ifdef __cplusplus
extern "C" {
#endif
	namespace TrackingFactory{

		namespace DBConnect{

			//sql::Driver* GetDriver();
			//IDBConnection* GetDBConnection(std::string configFile);
		}

		namespace Indexing{
			IEventIndexer* GetEventIndexer(std::string configFile, std::vector<IEvent*>* events);
			ITrackIndexer* GetTrackIndexer(std::string configFile, std::vector<ITrack*>* tracks);
		}

		namespace IO{
			std::vector<ITrack*>* GetTracksFromFile(std::string  fileLoc, std::string type, int span);
			void WriteTracksToFile(std::vector<ITrack*>* trks, std::string fileLoc, std::string type);
		}
		IEvent* GetNewEventFromPix(int Id, cv::Point2i* pix_location, cv::Rect* pix_BBox, std::vector<cv::Point2i>* pix_shape, boost::posix_time::time_period* timeStampPtr,
			std::string type);

		IEvent* GetNewEventNoCCode(int id, cv::Point2d* hpcLocation, std::vector<cv::Point2d>* hpcBBox, cv::Point2d* hgsLocation, std::vector<cv::Point2d>* hgsBBox,
			boost::posix_time::time_period* timePeriod, std::string type, std::string specificid);

		IEvent* GetNewEventWCCode(int id, cv::Point2d* hpcLocation, std::vector<cv::Point2d>* hpcBBox, std::vector<cv::Point2d>* hpcCCode,
			cv::Point2d* hgsLocation, std::vector<cv::Point2d>* hgsBBox, std::vector<cv::Point2d>* hgsCCode,
			boost::posix_time::time_period* timePeriod, std::string type, std::string specificid);

		ITrack* GetNewTrack(IEvent* first, IEvent* last);
		IPointConverter* GetPointConverter();
		IPositionPredictor* GetPositionPredictor();
	}
#ifdef __cplusplus
}
#endif
