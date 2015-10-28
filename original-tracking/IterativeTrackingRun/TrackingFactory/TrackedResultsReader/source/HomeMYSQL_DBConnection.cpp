/*
 * File:  HomeMYSQL_DBConnection.cpp
 * Author: Dustin Kempton
 *
 * Created on December 17, 2014, 2:30 PM
 */

#ifndef HOMEMYSQL_DBCONNECTION_CPP
#define	HOMEMYSQL_DBCONNECTION_CPP
#include <fstream>
//#include <iostream>
#include <vector>

#include <omp.h>
#include "../include/IEvent.hpp"
#include "../include/IDBConnection.hpp"

//#include <future>  
#include <chrono>
#include <thread>
#include <unordered_map>
#include <boost/asio.hpp>
#include <boost/bind.hpp>
#include <boost/date_time.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/make_shared.hpp>
#include <boost/thread/future.hpp>
#include <boost/thread/thread.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <boost/thread/thread.hpp>
#include "boost/date_time/posix_time/posix_time.hpp"


#include "../mysql-connector/include/mysql_driver.h"
#include "../mysql-connector/include/mysql_connection.h"
#include "../mysql-connector/include/cppconn/connection.h"
#include "../mysql-connector/include/cppconn/driver.h"
#include "../mysql-connector/include/cppconn/exception.h"
#include "../mysql-connector/include/cppconn/resultset.h"
#include "../mysql-connector/include/cppconn/statement.h"
#include "../mysql-connector/include/cppconn/prepared_statement.h"


using namespace std;
using namespace cv;
using namespace sql;
namespace bt = boost::posix_time;

class HomeMYSQL_DBConnection : public IDBConnection {

	class ConnPool
	{
	private:
		//map where each connection is in the pool vector
		typedef std::unordered_map<std::string, int> IdxMap;
		IdxMap* poolMap;
		vector<std::pair<sql::Connection*, bool>>* pool;
		boost::asio::deadline_timer* timer_;
		sql::Driver* driver = NULL;



		int maxConn;
		int maxThreads;
		int currCon;

		string server;
		string user;
		string passwrd;
		string db;
		omp_lock_t getLock;
		omp_lock_t checkLock;
		boost::thread* t;
		boost::asio::io_service io_srvce;
		boost::thread_group threads;
		boost::asio::io_service::work* work;

	public:
		ConnPool(sql::Driver* driver, std::string server, std::string user, std::string passwrd, std::string db){
			this->driver = driver;
			this->server = server;
			this->user = user;
			this->passwrd = passwrd;
			this->db = db;
			this->maxConn = 2;
			this->maxThreads = 1;
			this->currCon = 0;
			omp_init_lock(&getLock);
			omp_init_lock(&checkLock);


			//init the pool vector and map 
			this->pool = new vector<std::pair<sql::Connection*, bool>>();
			this->poolMap = new IdxMap();


			for (int i = 0; i < this->maxConn; i++){
				sql::Connection* con = this->driver->connect(this->server, this->user, this->passwrd);
				con->setAutoCommit(false);
				con->setSchema(this->db);
				this->pool->push_back(std::make_pair(con, false));
				this->poolMap->insert(std::make_pair(std::to_string((long)con), i));
			}

			//create a thread pool to use for testing/pinging connections
			this->work = new boost::asio::io_service::work(io_srvce);
			for (int i = 0; i < this->maxThreads; i++){
				this->threads.create_thread(boost::bind(&boost::asio::io_service::run, &io_srvce));
			}

			//init the timer
			this->timer_ = new boost::asio::deadline_timer(this->io_srvce, boost::posix_time::seconds(30));
			timer_->async_wait(boost::bind(&ConnPool::callPing, this));
			t = new boost::thread(boost::bind(&boost::asio::io_service::run, &this->io_srvce));

		}
		~ConnPool(){

			delete t;
			for (int i = 0; i < pool->size(); i++)
			{
				std::pair<sql::Connection*, bool> pr = pool->at(i);
				delete pr.first;
			}
			delete pool;
			delete poolMap;
			delete timer_;
			omp_destroy_lock(&getLock);
			omp_destroy_lock(&checkLock);

			this->io_srvce.stop();
			threads.join_all();
			delete work;
		}


		sql::Connection* getCurrConnect(){

			//check to see if something is available
			sql::Connection* con;
			bool isSet = false;

			omp_set_lock(&checkLock);
			try{
				//	cout << "Set CheckLock in Get" << endl;
				//loop and wait for a connection to open up, then continue to grab the connection from
				//the pool of connections available.  
				bool goodToGo = false;
				while (!goodToGo){
					for (int i = 0; i < this->maxConn; i++){
						std::pair<sql::Connection*, bool> pr = this->pool->at(i);
						if (!pr.second){
							goodToGo = true;
							break;
						}
					}

					//We don't want a spinning wait, so sleep for a bit and then check again.
					if (!goodToGo){
						std::this_thread::sleep_for(std::chrono::seconds(1));
					}
				}



				//We use two locks because we want to be able to release connections while
				//we are waiting for one to open up.  This would not be possible with one.
				omp_set_lock(&getLock);
				try{
					//cout << "Set GetLock in Get" << endl;
					//loop through the pool to find one that is unused, starting with the one that 
					//is supposed to be open according to our currCon index variable.
					for (int i = 0; i < this->maxConn; i++){
						int idx = (i + this->currCon) % this->maxConn;
						std::pair<sql::Connection*, bool> pr = this->pool->at(idx);
						//if not in use then use it
						if (!pr.second){
							//set the connection to being used
							pr.second = true;
							this->pool->at(idx) = pr;

							//set for return
							con = pr.first;

							//update the current position in the pool
							this->currCon++;
							if (this->currCon >= this->maxConn){
								this->currCon = currCon % this->maxConn;
							}
							isSet = true;
							break;
						}
					}
					//cout << "Unset GetLock in Get" << endl;
				}
				catch (std::exception ex){
					cout << ex.what() << endl;
				}
				omp_unset_lock(&getLock);
				//cout << "Unset CheckLock in Get" << endl;
			}
			catch (std::exception ex){
				cout << ex.what() << endl;
			}
			omp_unset_lock(&checkLock);




			//make damn sure it is set
			if (isSet){
				//see if the connection is open and working and replace it if not
				bool needsReplaced = false;
				try{

					//create task to send to the th
					typedef boost::packaged_task<bool> task_t;
					boost::shared_ptr<task_t> task = boost::make_shared<task_t>(boost::bind(&ConnPool::Ping, con));
					boost::shared_future<bool> fut = task->get_future();
					io_srvce.post(boost::bind(&task_t::operator(), task));

					//wait for the ping to retunr for three seconds, if it takes that long
					//we will consider it dead.  It should return MUCH sooner than that.
					boost::future_status status = fut.wait_for(boost::chrono::seconds(3));
					if (status == boost::future_status::ready){
						if (fut.get()){
							needsReplaced = false;
						}
						else{
							needsReplaced = true;
						}
					}
					else{
						needsReplaced = true;
					}
				}
				catch (std::exception ex){
					cout << "Connection Failed to open" << endl;
					needsReplaced = true;
				}

				if (needsReplaced){
					con = this->replaceConnection(con);
				}
				return con;
			}
			else{
				return this->getCurrConnect();
			}
		}

		void releaseConnect(sql::Connection* con){
			//tried to make it a nowait task but this failed when in parallel loop?
			//Maybe try again later.
			//#pragma omp single nowait
			{
				//#pragma omp task
				releaseTask(con);
			}
		}



	private:
		void close(sql::Connection* conn){
			conn->close();
		}
		void releaseTask(sql::Connection* con)
		{

			//we just want to catch any exception here. If the connection fails when we
			//try to see if it is open when we go to use it again, then it will be replaced
			try{
				//cout << "Release:" << idx << endl;
				con->commit();
				con->clearWarnings();
				//this->driver->threadEnd();
			}
			catch (std::exception ex){
				cout << ex.what() << endl;
			}

			//find the index of the passed in connection
			int idx = this->poolMap->at(std::to_string((long)con));
			std::pair<sql::Connection*, bool> pr = this->pool->at(idx);
			pr.second = false;

			//update it to unused
			omp_set_lock(&getLock);
			try{
				//cout << "Set GetLock in Rel" << endl;
				this->pool->at(idx) = pr;
				//cout << "Unset GetLock in Rel" << endl;
			}
			catch (std::exception ex){
				cout << ex.what() << endl;
			}
			omp_unset_lock(&getLock);
		}

		void callPing(){
			//just a quick ping to keep everything alive


			//this->driver->threadInit();
			vector<int> replaceIdx;
			omp_set_lock(&checkLock);
			omp_set_lock(&getLock);
			std::pair<sql::Connection*, bool> pr;
			try{
				for (int i = 0; i < this->pool->size(); i++){
					std::pair<sql::Connection*, bool> pr = this->pool->at(i);
					if (!pr.second){

						sql::Connection* con = pr.first;
						try{
							//create a task to send to the thread pool to execute ping
							typedef boost::packaged_task<bool> task_t;
							boost::shared_ptr<task_t> task = boost::make_shared<task_t>(boost::bind(&ConnPool::Ping, con));
							boost::shared_future<bool> fut = task->get_future();
							io_srvce.post(boost::bind(&task_t::operator(), task));

							//wait for the ping to return for 3 seconds, if it doesn't return int
							//that amount of time we will consider it dead and replace it.
							boost::future_status status = fut.wait_for(boost::chrono::seconds(3));
							if (status == boost::future_status::ready){
								if (fut.get()){
									continue;
								}
								else{
									replaceIdx.push_back(i);
									pr.second = true;
									this->pool->at(i) = pr;
								}
							}
							else{
								replaceIdx.push_back(i);
								pr.second = true;
								this->pool->at(i) = pr;
							}
						}
						catch (std::exception ex){
							cout << "Keepalive Failed" << endl;
							cout << ex.what() << endl;
							replaceIdx.push_back(i);
							pr.second = true;
							this->pool->at(i) = pr;
						}
					}
				}
			}
			catch (std::exception ex){
				cout << ex.what() << endl;
			}
			omp_unset_lock(&getLock);
			omp_unset_lock(&checkLock);

			//replace any that failed
			for (int i = 0; i < replaceIdx.size(); i++){
				this->replaceConnection(this->pool->at(replaceIdx.at(i)).first);
				omp_set_lock(&getLock);
				try{
					std::pair<sql::Connection*, bool> pr = this->pool->at(replaceIdx.at(i));
					pr.second = false;
					this->pool->at(i) = pr;
				}
				catch (std::exception ex){
					cout << ex.what() << endl;
				}
				omp_unset_lock(&getLock);
			}

			//reset the timer for another 45 seconds
			timer_->expires_at(timer_->expires_at() + boost::posix_time::seconds(45));
			timer_->async_wait(boost::bind(&ConnPool::callPing, this));
		}

		static bool Ping(sql::Connection* con)
		{
			//cout << "Ping" << endl;
			try{
				string statementString = string("DO 1");
				std::unique_ptr<sql::PreparedStatement> prep_stmt(con->prepareStatement(statementString));
				prep_stmt->execute();
				//cout << "Ping end" << endl;
				return true;
			}
			catch (sql::SQLException ex){
				cout << "Ping Fail" << endl;
				return false;
			}
		}

		sql::Connection* replaceConnection(sql::Connection* con){

			//find where it is in the list of connections
			sql::Connection* tmp;
			bool isSet = false;
			try{
				tmp = this->driver->connect(this->server, this->user, this->passwrd);
				tmp->setAutoCommit(false);
				tmp->setSchema(this->db);
				isSet = true;
			}
			catch (sql::SQLException ex){
				cout << ex.what() << endl;
			}

			//if we got a new connection then replace the old one, else try again.
			if (isSet){
				string idxStrng = std::to_string((long)con);
				int idx = this->poolMap->at(idxStrng);

				//get the connection pair
				omp_set_lock(&getLock);
				try{
					std::pair<sql::Connection*, bool> pr = this->pool->at(idx);

					//cout << "Set GetLock in Replace" << endl;
					//delete the old connection first, and remove it from the map
					pr.first->close();
					delete pr.first;
					this->poolMap->erase(idxStrng);

					//replace the connection with the new one, place it in the map, and the pool vector
					pr.first = tmp;
					this->poolMap->insert(std::make_pair(std::to_string((long)pr.first), idx));
					this->pool->at(idx) = pr;
					//cout << "Unset GetLock in Replace" << endl;
				}
				catch (std::exception ex){
					cout << ex.what() << endl;
				}
				omp_unset_lock(&getLock);


				return tmp;
			}
			else{
				return this->replaceConnection(con);
			}
		}
	};


	//typedef for name/vector pointer pair
	typedef std::pair< std::string, vector< vector< vector<float> > >* > EntryPair;

	//typedef for the cache list (list of entries)
	typedef std::list< EntryPair> CacheList;

	//typedef for the name-index map into the cachelist
	typedef std::unordered_map< std::string, CacheList::iterator > CacheMap;

private:
	//if we have OpenMP enabled we will have locking on portions of this class
#ifdef _OPENMP
	omp_lock_t lock;
#endif

	const std::locale format = std::locale(std::locale::classic(), new boost::posix_time::time_input_facet("%Y-%m-%d %H:%M:%S"));

	string user;
	string password;
	string server;
	string database;
	//string paramStatementLeft;
	//string paramStatementRight;
	string vecParamStatementLeft;
	string vecParamStatementRight;
	sql::Driver* driver = NULL;

	int paramDim;
	int wavelength;
	int maxCacheSize = 10000;
	int cachEntries = 0;
	//vector<string> params;

	CacheList* cache = NULL;
	CacheMap* idxMap = NULL;

	//pool for connections
	ConnPool* pool = NULL;

public:

	HomeMYSQL_DBConnection(string configFile, sql::Driver* driver){
		cout << "Config File:" << configFile << endl;
		if (this->readFile(configFile)){
			this->driver = driver;
			/*this->driver = sql::mysql::get_driver_instance();*/
			cout << "Init ok" << endl;
			/*cout << "User: " << this->user << endl;
			cout << "Password: " << this->password << endl;
			cout << "Server: " << this->server << endl;
			cout << "DB: " << this->database << endl;*/
			/*string leftstatement = "SELECT x, y, ";
			string rightstatement = "SELECT x, y, ";
			for (int k = 0; k < 3; k++){
			leftstatement += this->params.at(k);
			rightstatement += this->params.at(k);
			if (k < 2){
			leftstatement += ", ";
			rightstatement += ", ";
			}
			else{
			leftstatement += " ";
			rightstatement += " ";
			}
			}

			leftstatement += string("from(SELECT file_id from(SELECT id FROM trackingdata.files ") +
			"WHERE(((startdate BETWEEN ? AND ?) OR (enddate BETWEEN ? AND ?)) and (wavelength = ?)) order by startdate desc) as t1 " +
			"inner join trackingdata.image_param ON trackingdata.image_param.file_id = t1.id " +
			"limit 1) as t2 left join " +
			"trackingdata.image_param ON trackingdata.image_param.file_id = t2.file_id " +
			"WHERE (x BETWEEN ? AND ? ) AND(y BETWEEN ? AND ? )";

			rightstatement += string("from(SELECT file_id from(SELECT id FROM trackingdata.files ") +
			"WHERE(((startdate BETWEEN ? AND ?) OR (enddate BETWEEN ? AND ?)) and (wavelength = ?)) order by startdate asc) as t1 " +
			"inner join trackingdata.image_param ON trackingdata.image_param.file_id = t1.id " +
			"limit 1) as t2 left join " +
			"trackingdata.image_param ON trackingdata.image_param.file_id = t2.file_id " +
			"WHERE (x BETWEEN ? AND ? ) AND(y BETWEEN ? AND ? )";*/

			/*	this->paramStatementLeft = leftstatement;
				this->paramStatementRight = rightstatement;*/
		}
		else{
			cout << "Failed to Init" << endl;
		}

		this->vecParamStatementLeft = string("SELECT x, y, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10 ") +
			"from(SELECT file_id from (SELECT id FROM trackingdata.files " +
			"WHERE(((startdate BETWEEN ? AND ?) OR (enddate BETWEEN ? AND ?)) and (wavelength = ?)) order by startdate desc) as t1 " +
			"inner join trackingdata.image_param ON trackingdata.image_param.file_id = t1.id " +
			"limit 1) as t2 left join " +
			"trackingdata.image_param ON trackingdata.image_param.file_id = t2.file_id " +
			"WHERE (x BETWEEN ? AND ? ) AND(y BETWEEN ? AND ? )";

		this->vecParamStatementRight = string("SELECT x, y, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10 ") +
			"from(SELECT file_id from (SELECT id FROM trackingdata.files " +
			"WHERE(((startdate BETWEEN ? AND ?) OR (enddate BETWEEN ? AND ?)) and (wavelength = ?)) order by startdate asc) as t1 " +
			"inner join trackingdata.image_param ON trackingdata.image_param.file_id = t1.id " +
			"limit 1) as t2 left join " +
			"trackingdata.image_param ON trackingdata.image_param.file_id = t2.file_id " +
			"WHERE (x BETWEEN ? AND ? ) AND(y BETWEEN ? AND ? )";

		cache = new CacheList();
		idxMap = new CacheMap();

		this->pool = new ConnPool(this->driver, this->server, this->user, this->password, this->database);

		//init the lock
#ifdef _OPENMP
		omp_init_lock(&lock);
#endif
	}

	~HomeMYSQL_DBConnection() {
		for (CacheList::iterator it = this->cache->begin(); it != this->cache->end(); it++){
			EntryPair ep = *it;
			vector< vector< vector<float> > >* tmpvec = ep.second;
			delete tmpvec;
		}
		delete cache;
		delete idxMap;

		//destroy the lock
#ifdef _OPENMP
		omp_destroy_lock(&lock);
#endif
	}

	bool getFirstImage(cv::Mat& img, boost::posix_time::time_period period){

		sql::Connection* con;
		try{
			
			//get a database connection from the pool			
			con = this->pool->getCurrConnect();


			string statement = string("SELECT * FROM files ") +
				"WHERE (((startdate BETWEEN ? AND ?) OR (enddate BETWEEN ? AND ?)) and (wavelength = ?))";


			std::unique_ptr< sql::PreparedStatement>
				prep_stmt(con->prepareStatement(statement));


			try {
				prep_stmt->setDateTime(1, boost::posix_time::to_iso_string(period.begin()));
				prep_stmt->setDateTime(2, boost::posix_time::to_iso_string(period.end()));
				prep_stmt->setDateTime(3, boost::posix_time::to_iso_string(period.begin()));
				prep_stmt->setDateTime(4, boost::posix_time::to_iso_string(period.end()));
				prep_stmt->setInt(5, this->wavelength);

				std::unique_ptr<sql::ResultSet> res(prep_stmt->executeQuery());


				if (res->next()) {



					int frameID = res->getUInt("id");

					std::unique_ptr<sql::PreparedStatement> prep_stmt2;
					prep_stmt.reset(con->prepareStatement(string("SELECT image_file FROM image_files ") +
						"WHERE file_id=?"));

					prep_stmt->setInt(1, frameID);

					std::unique_ptr<sql::ResultSet> res2(prep_stmt->executeQuery());
					//            res.reset(prep_stmt->getResultSet());
					res2->next();
					istream* stre = res2->getBlob("image_file");


					stre->seekg(0, stre->end);
					int length = stre->tellg();
					stre->seekg(0, stre->beg);

					char* buffer = new char[length];

					stre->read(buffer, length);

					cv::Mat img_data(length, 1, CV_8U, buffer);
					img = imdecode(img_data, IMREAD_GRAYSCALE);

					delete buffer;
					delete stre;

					this->pool->releaseConnect(con);
					return true;
				}
				else{
					this->pool->releaseConnect(con);
					return false;
				}


			}
			catch (std::exception e) {
				this->pool->releaseConnect(con);
				cout << e.what() << endl;
				return false;
			}

		}
		catch (std::exception sqEx){
			this->pool->releaseConnect(con);
			cout << sqEx.what() << endl;
			return false;
		}
	}



	vector< vector< vector<float> > >* getImageParam(IEvent* evnt, bool leftSide) {


		string evntName = "";
		evntName.append(std::to_string((long)evnt));
		if (leftSide){
			evntName.append("T");
		}
		else{
			evntName.append("F");
		}
		vector< vector< vector<float> > >* vec;

		//lock so only one thread update the cache at one time
#ifdef _OPENMP
		omp_set_lock(&lock);
#endif
		bool notInCache = false;
		try{
			notInCache = (this->idxMap->find(evntName) == this->idxMap->end());
		}
		catch (exception ex){
			cout << ex.what() << endl;
		}
#ifdef _OPENMP
			omp_unset_lock(&lock);
#endif

		if (notInCache){
			

			Rect* tmpBbox = evnt->getBBox();

			Rect rec((tmpBbox->x / paramDim) - 4, (tmpBbox->y / paramDim) - 4,
				(tmpBbox->width / paramDim) + 8, (tmpBbox->height / paramDim) + 8);

			vec = new vector < vector < vector<float > > >(rec.height, vector< vector<float> >(rec.width, vector<float>(10)));

			int tryCount = 0;
			bool executed = false;
			while (!executed && tryCount < 3){
				sql::Connection* con;
				try{
					//get a database connection from the pool
					con = this->pool->getCurrConnect();

					std::unique_ptr< sql::PreparedStatement> prep_stmt;
					int tryCount = 0;
					executed = false;
					while (!executed && tryCount < 3){
						try{
							if (leftSide){
								prep_stmt.reset(con->prepareStatement(this->vecParamStatementLeft));
							}
							else{
								prep_stmt.reset(con->prepareStatement(this->vecParamStatementRight));
							}
							executed = true;
						}
						catch (sql::SQLException &e) {
							tryCount++;
						}
					}
					bt::time_period eventPeriod = evnt->getTimePeriod();

					prep_stmt->setDateTime(1, boost::posix_time::to_iso_string(eventPeriod.begin()));
					prep_stmt->setDateTime(2, boost::posix_time::to_iso_string(eventPeriod.end()));
					prep_stmt->setDateTime(3, boost::posix_time::to_iso_string(eventPeriod.begin()));
					prep_stmt->setDateTime(4, boost::posix_time::to_iso_string(eventPeriod.end()));
					prep_stmt->setInt(5, this->wavelength);
					prep_stmt->setInt(6, rec.x);
					prep_stmt->setInt(7, rec.x + rec.height - 1);
					prep_stmt->setInt(8, rec.y);
					prep_stmt->setInt(9, rec.y + rec.width - 1);

					std::unique_ptr<sql::ResultSet> res2;

					tryCount = 0;
					executed = false;
					while (!executed && tryCount < 3){
						try{
							res2.reset(prep_stmt->executeQuery());
							while (res2->next()) {

								int x = res2->getInt("x");
								x = x - rec.x;

								int y = res2->getInt("y");
								y = y - rec.y;


								for (int i = 1; i < 11; i++) {
									stringstream ss;
									ss << "p" << i;
									vec->at(x)[y][i - 1] = res2->getDouble(ss.str());
								}
							}
							executed = true;
						}
						catch (sql::SQLException &e) {
							cout << e.what() << endl;
							tryCount++;
						}
					}


					//free the connection in the pool
					this->pool->releaseConnect(con);
				}
				catch (std::exception e) {
					this->pool->releaseConnect(con);
					cout << "Failed Query Prep" << endl;
					cout << e.what() << endl;
					tryCount++;
				}
			}


			//lock so only one thread update the cache at one time
#ifdef _OPENMP
			omp_set_lock(&lock);
#endif
			try{
				this->cache->push_front(std::make_pair(evntName, vec));
				this->idxMap->insert(std::make_pair(evntName, this->cache->begin()));
				this->cachEntries++;

				//if the cache is grown bigger than the maximum, then shrink it by removing
				//the last entry in the cache.  This entry will be the least recently used.
				if (this->cachEntries > this->maxCacheSize){

					EntryPair ep = this->cache->back();
					this->cache->pop_back();

					this->idxMap->erase(ep.first);
					vector< vector< vector<float> > >* tmpvec = ep.second;
					delete tmpvec;
					this->cachEntries--;
				}
			}
			catch (exception ex){
				cout << ex.what() << endl;
			}

			//release the lock
#ifdef _OPENMP
			omp_unset_lock(&lock);
#endif

		}
		else{


			CacheList::iterator itr = this->idxMap->at(evntName);
			EntryPair ep = *itr;
			vec = ep.second;

			//update the cache to have the currently accessed item in the front
			//because this is a LRU cache
#ifdef _OPENMP
			omp_set_lock(&lock);
#endif
			try{
				this->cache->erase(itr);
				this->cache->push_front(ep);
				this->idxMap->at(evntName) = this->cache->begin();
			}
			catch (exception ex){
				cout << ex.what() << endl;
			}

#ifdef _OPENMP
			omp_unset_lock(&lock);
#endif
		}
		return vec;
	}

private:

	bool readFile(string configFile) {

		try{

			ifstream myfile(configFile);
			if (myfile.is_open()) {
				string line;
				//while there is something to read, process it.
				getline(myfile, line);//throw away the header line;
				getline(myfile, line);//get config line;

				std::string user, password, server, database;

				stringstream ss(line); //feed the string stream with the line of database information
				getline(ss, user, '\t');
				this->user = user;
				getline(ss, password, '\t');
				this->password = password;
				getline(ss, server, '\t');
				this->server = server;
				getline(ss, database);
				this->database = database;

				//getline(myfile, line);//throw away blank line;
				//getline(myfile, line);//get title of next set of info;
				//if (line.compare("params") == 0){
				//	getline(myfile, line);//get first param name
				//	this->params.push_back(line);

				//	getline(myfile, line);//get second param name
				//	this->params.push_back(line);

				//	getline(myfile, line);//get third param name
				//	this->params.push_back(line);
				//}


				getline(myfile, line);//throw away blank line;
				getline(myfile, line);//get title of next set of info;
				if (line.compare("wavelength") == 0){
					getline(myfile, line);//get wavelength
					this->wavelength = atoi(line.c_str());
				}


				getline(myfile, line);//throw away blank line;
				getline(myfile, line);//get title of next set of info;
				if (line.compare("param_dim") == 0){
					getline(myfile, line);//get param dim
					this->paramDim = atoi(line.c_str());
				}

				getline(myfile, line);//throw away blank line;
				getline(myfile, line);//get title of next set of info;
				if (line.compare("cache_size") == 0){
					getline(myfile, line);//get cache max size
					this->maxCacheSize = atoi(line.c_str());
					cout << "Cache Size Set: " << this->maxCacheSize << endl;
				}

				myfile.close();
				return true;
			}
			else{
				return false;
			}


		}
		catch (Exception ex){
			return false;
		}
	}
};

#endif
