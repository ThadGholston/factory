#include "../include/TrackingFactory.h"

#include "ResultsReader.cpp"
#include "EventIndexer.cpp"
#include "TrackIndexer.cpp"
#include "PointConverter.cpp"
#include "PositionPredictor.cpp"
#include "ResultsWriter.cpp"

static IPointConverter* convtr = NULL;
static sql::Driver* driver = NULL;

extern "C"{
	namespace TrackingFactory{

		namespace DBConnect{
			sql::Driver* GetDriver(){
				if (driver == NULL){
					driver = sql::mysql::get_driver_instance();
				}
				return driver;
			}
			IDBConnection* GetDBConnection(std::string configFile)
			{
				if (driver == NULL){
					driver = sql::mysql::get_driver_instance();
				}
				return new HomeMYSQL_DBConnection(configFile, driver);
			};
		}

		namespace Indexing{
			IEventIndexer* GetEventIndexer(std::string configFile, std::vector<IEvent*>* events){
				return new EventIndexer(configFile, events);
			}

			ITrackIndexer* GetTrackIndexer(std::string configFile, std::vector<ITrack*>* tracks){
				return new TrackIndexer(configFile, tracks);
			}
		}


		namespace IO{
			vector<ITrack*>* GetTracksFromFile(std::string  fileLoc, std::string type, int span)
			{
				if (convtr == NULL){
					convtr = GetPointConverter();
				}
				ResultsReader rdr(convtr, fileLoc, span);
				vector<ITrack*>* evnts = rdr.getTracks(type);

				return evnts;
			}

			void WriteTracksToFile(std::vector<ITrack*>* trks, std::string fileLoc, std::string type){
				ResultsWriter rr;
				rr.writeResults(trks, fileLoc, type);
			}
		}

		IEvent* GetNewEventFromPix(int Id, cv::Point2i* pix_location, cv::Rect* pix_BBox, std::vector<cv::Point2i>* pix_shape, boost::posix_time::time_period* timeStampPtr, std::string type){
			if (convtr == NULL){
				convtr = GetPointConverter();
			}

			return new ExtendedEvent(convtr, Id, pix_location, pix_BBox, pix_shape, timeStampPtr, type);
		}

		IEvent*  GetNewEventNoCCode(int id, Point2d* hpcLocation, vector<Point2d>* hpcBBox, Point2d* hgsLocation, vector<Point2d>* hgsBBox,
			boost::posix_time::time_period* timePeriod, string type, string specificid){

			if (convtr == NULL){
				convtr = GetPointConverter();
			}
			return new ExtendedEvent(convtr, id, hpcLocation, hpcBBox, hgsLocation, hgsBBox, timePeriod, type, specificid);
		}

		IEvent* GetNewEventWCCode(int id, Point2d* hpcLocation, vector<Point2d>* hpcBBox, vector<Point2d>* hpcCCode,
			Point2d* hgsLocation, vector<Point2d>* hgsBBox, vector<Point2d>* hgsCCode,
			boost::posix_time::time_period* timePeriod, string type, string specificid){

			if (convtr == NULL){
				convtr = GetPointConverter();
			}

			return new ExtendedEvent(convtr, id, hpcLocation, hpcBBox, hpcCCode, hgsLocation, hgsBBox, hgsCCode, timePeriod, type, specificid);
		}

		ITrack* GetNewTrack(IEvent* first, IEvent* last){
			return new Track(first, last);
		}

		IPointConverter* GetPointConverter(){
			return new PointConverter();
		}

		IPositionPredictor* GetPositionPredictor(){
			if (convtr == NULL){
				convtr = GetPointConverter();
			}
			return new PositionPredictor(convtr);
		}
	}
}