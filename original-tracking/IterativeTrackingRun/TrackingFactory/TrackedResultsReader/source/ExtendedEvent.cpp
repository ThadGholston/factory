/*
 * File:   KarthikEvent.cpp
 * Author: Dustin
 *
 * Created on January 16, 2014, 4:06 PM
 */
#ifndef EXTENDEDEVENT
#define EXTENDEDEVENT



#include "boost/date_time/posix_time/posix_time.hpp"
#include "../include/IPointConverter.hpp"
#include "../include/IEvent.hpp"
#include <stdexcept>
#include<typeinfo>


using namespace std;
using namespace cv;
namespace bt = boost::posix_time;

class ExtendedEvent : public IEvent {
private:


	int id;

	Point2d* hpcLocation;
	vector<Point2d>* hpcBBox;
	vector<Point2d>* hpcPoly = NULL;

	Point2d* hgsLocation;
	vector<Point2d>* hgsBBox;
	vector<Point2d>* hgsPoly = NULL;

	Point2i* location;
	Rect* boundBox;
	vector<Point2i>* shape;

	string specificid;
	string type;
	bt::time_period* timePeriod;

	IEvent* next = NULL;
	IEvent* previous = NULL;
	IPointConverter* convtr;

public:


	ExtendedEvent(IPointConverter* convtr, int Id, Point2i* pix_Location, Rect* pix_BBoxPtr,
		vector<Point2i>* pix_ShapePtr, boost::posix_time::time_period* timeStampPtr, string type){
		if (convtr == NULL) throw invalid_argument("PositionConverter cannot be null");
		if (pix_Location == NULL) throw invalid_argument("Location cannot be null");
		if (pix_BBoxPtr == NULL) throw invalid_argument("BBox cannot be null");
		if (pix_ShapePtr == NULL) throw invalid_argument("Shape cannot be null");

		this->convtr = convtr;

		this->setBBoxFromPixXY(pix_BBoxPtr);
		this->boundBox = pix_BBoxPtr;
		this->setShapefromPixXY(pix_ShapePtr);
		this->shape = pix_ShapePtr;
		this->setPosFromPixXY(pix_Location);
		this->location = pix_Location;

		this->timePeriod = timeStampPtr;
		this->type = type;
		this->id = id;

	}

	ExtendedEvent(IPointConverter* convtr, int id, Point2d* hpcLocation, vector<Point2d>* hpcBBox, Point2d* hgsLocation, vector<Point2d>* hgsBBox,
		bt::time_period* timePeriod, string type, string specificid) : id(id), specificid(specificid), type(type) {

		if (convtr == NULL) throw invalid_argument("PositionConverter cannot be null");
		if (hpcLocation == NULL) throw invalid_argument("hpcLocation cannot be null");
		if (hgsLocation == NULL) throw invalid_argument("hgsLocation cannot be null");
		if (hpcBBox == NULL) throw invalid_argument("hpcBBox cannot be null");
		if (hgsBBox == NULL) throw invalid_argument("hgsBBox cannot be null");

		this->convtr = convtr;

		this->hpcLocation = hpcLocation;
		this->hgsLocation = hgsLocation;
		this->hpcBBox = hpcBBox;
		this->hgsBBox = hgsBBox;

		this->timePeriod = timePeriod;

		this->setPosFromHPC(hpcLocation);
		this->setShapeFromHPCBBox(hpcBBox);
		this->setRectFromHPC(hpcBBox);


	}

	ExtendedEvent(IPointConverter* convtr, int id, Point2d* hpcLocation, vector<Point2d>* hpcBBox, vector<Point2d>* hpcCCode,
		Point2d* hgsLocation, vector<Point2d>* hgsBBox, vector<Point2d>* hgsCCode,
		bt::time_period* timePeriod, string type, string specificid)
		: id(id), type(type), specificid(specificid) {

		if (convtr == NULL) throw invalid_argument("PositionConverter cannot be null");
		if (hpcLocation == NULL) throw invalid_argument("hpcLocation cannot be null");
		if (hgsLocation == NULL) throw invalid_argument("hgsLocation cannot be null");
		if (hpcBBox == NULL) throw invalid_argument("hpcBBox cannot be null");
		if (hgsBBox == NULL) throw invalid_argument("hgsBBox cannot be null");
		if (hpcCCode == NULL) throw invalid_argument("hpcCCode cannot be null");
		if (hgsCCode == NULL) throw invalid_argument("hgsCCode cannot be null");

		this->convtr = convtr;

		this->hpcLocation = hpcLocation;
		this->hgsLocation = hgsLocation;
		this->hpcBBox = hpcBBox;
		this->hgsBBox = hgsBBox;
		this->hgsPoly = hgsCCode;
		this->hpcPoly = hpcCCode;
		this->timePeriod = timePeriod;

		this->setPosFromHPC(hpcLocation);
		this->setShapeFromHPC(hpcCCode);
		this->setRectFromHPC(hpcBBox);

	}

	~ExtendedEvent() {
		delete this->hpcLocation;
		delete this->hpcBBox;
		delete this->hpcPoly;

		delete this->hgsLocation;
		delete this->hgsBBox;
		delete this->hgsPoly;

		delete this->location;
		delete this->boundBox;
		delete this->shape;

		delete this->timePeriod;

		if (this->next != NULL) {
			this->next->setPrevious(NULL);
		}
		if (this->previous != NULL) {
			this->previous->setNext(NULL);
		}
	}

	int getId() {
		return this->id;
	};

	Point2i* getLocation() {
		return this->location;
	};

	Rect* getBBox() {
		return this->boundBox;
	};

	Point2d* getHGSLocation() {
		return this->hgsLocation;
	};

	vector<Point2d>* getHGSBBox() {
		return this->hgsBBox;
	};

	vector<Point2d>* getHGSPoly() {
		return this->hgsPoly;
	};

	Point2d* getHPCLocation() {
		return this->hpcLocation;
	};

	vector<Point2d>* getHPCBBox() {
		return this->hpcBBox;
	};

	vector<Point2d>* getHPCPoly() {
		return this->hpcPoly;
	};

	string getSpecificID() {
		return this->specificid;
	}

	vector<Point2i>* getShape() {
		return this->shape;
	};


	bt::time_period getTimePeriod() {
		bt::time_period returnTime(this->timePeriod->begin(), this->timePeriod->end());
		return returnTime;
	}

	void updateTimePeriod(boost::posix_time::time_period* period){
		delete this->timePeriod;
		this->timePeriod = period;
	}

	string getType() {
		return this->type;
	}

	IEvent* getPrevious() {
		return this->previous;
	}

	void setPrevious(IEvent* previous) {
		if (typeid (previous) == typeid (IEvent*)) {
			this->previous = previous;
		}
		else if (previous == NULL) {
			this->previous = NULL;
		}
		else {
			string s = "Type of";
			s.append(typeid (previous).name());
			s.append("cannot be assigned as previous");
			throw invalid_argument(s);
		}
	}

	IEvent* getNext() {
		return this->next;
	}

	void setNext(IEvent* next) {
		if (typeid (next) == typeid (IEvent*)) {
			this->next = next;
		}
		else if (next == NULL) {
			this->next = NULL;
		}
		else {
			string s = "Type of";
			s.append(typeid (next).name());
			s.append("cannot be assigned as next");
			throw invalid_argument(s);
		}
	}

private:


	Rect * getRect(vector<Point2i>* poly) {
		int minX, minY, maxX, maxY;
		vector<Point2i>::iterator iterator = poly->begin();
		Point2i point = iterator.operator *();
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

	void setShapeFromHPCBBox(vector<Point2d>* poly){
		vector<Point2i>* xyPoly = new vector<Point2i>();
		vector<Point2d>* hgsPoly = new vector<Point2d>();
		vector<Point2d>* hpcPoly = new vector<Point2d>();
		for (int i = 0; i < poly->size(); i++){
			Point2i p = this->convtr->convertHPCToPixXY(poly->at(i));
			xyPoly->push_back(p);
			hgsPoly->push_back(this->convtr->convertPixXYToHGS(p));
			hpcPoly->push_back(poly->at(i));
		}

		this->shape = xyPoly;
		this->hgsPoly = hgsPoly;
		this->hpcPoly = hpcPoly;
	}

	void setShapeFromHPC(vector<Point2d>* poly) {
		this->shape = new vector<Point2i >();
		for (vector<Point2d>::iterator iterator = poly->begin(); iterator != poly->end(); ++iterator) {
			Point2d pointf = iterator.operator *();
			this->shape->push_back(this->convtr->convertHPCToPixXY(pointf));
		}
	}

	void setShapefromPixXY(vector<Point2i>* poly){
		vector<Point2d>* hpcPoly = new vector<Point2d>();
		vector<Point2d>* hgsPoly = new vector<Point2d>();

		for (int i = 0; i < poly->size(); i++){
			Point2i p = poly->at(i);
			hpcPoly->push_back(this->convtr->convertPixXYToHPC(p));
			hgsPoly->push_back(this->convtr->convertPixXYToHGS(p));
		}

		this->hpcPoly = hpcPoly;
		this->hgsPoly = hgsPoly;
	}

	void setPosFromPixXY(Point2i* ptr){
		Point2i p(ptr->x, ptr->y);
		this->hpcLocation = new Point2d(this->convtr->convertPixXYToHPC(p));
		this->hgsLocation = new Point2d(this->convtr->convertPixXYToHGS(p));
	}

	void setPosFromHPC(Point2d* ptr){
		Point2d p(ptr->x, ptr->y);
		this->location = new Point2i(this->convtr->convertHPCToPixXY(p));
	}

	void setRectFromHPC(vector<Point2d>* bBox){
		vector<Point2i>* vec = new vector<Point2i>();
		for (int i = 0; i < bBox->size(); i++){
			vec->push_back(this->convtr->convertHPCToPixXY(bBox->at(i)));
		}
		Rect* r = this->getRect(vec);
		delete vec;
		this->boundBox = r;
	}

	void setBBoxFromPixXY(Rect* rec){
		Point2i p1(rec->x, rec->y);
		Point2i p2(rec->x, rec->y + rec->height);
		Point2i p3(rec->x + rec->width, rec->y + rec->height);
		Point2i p4(rec->x + rec->width, rec->y);

		vector<Point2d>* hgsBBox = new vector<Point2d>();
		hgsBBox->push_back(this->convtr->convertPixXYToHGS(p1));
		hgsBBox->push_back(this->convtr->convertPixXYToHGS(p2));
		hgsBBox->push_back(this->convtr->convertPixXYToHGS(p3));
		hgsBBox->push_back(this->convtr->convertPixXYToHGS(p4));

		this->hgsBBox = hgsBBox;

		vector<Point2d>* hpcBBox = new vector<Point2d>();
		hpcBBox->push_back(this->convtr->convertPixXYToHPC(p1));
		hpcBBox->push_back(this->convtr->convertPixXYToHPC(p2));
		hpcBBox->push_back(this->convtr->convertPixXYToHPC(p3));
		hpcBBox->push_back(this->convtr->convertPixXYToHPC(p4));

		this->hpcBBox = hpcBBox;
	}


};
#endif