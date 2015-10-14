package edu.gsu.dmlab.stages;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import edu.gsu.dmlab.stages.interfaces.Stage;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.apache.commons.configuration.Configuration;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Created by thad on 9/23/15.
 */
public class StageOne extends Stage {

    public StageOne(IEventIndexer eventIndexer, Configuration configuration){
        super(eventIndexer, configuration);
    }

    @Override
    public ArrayList<ITrack> process() {
        DateTime startTime = eventIndexer.getFirstTime();
        DateTime endTime = eventIndexer.getLastTime();
        ArrayList<ITrack> intermediateResults = process(startTime, endTime);
        ArrayList<ITrack> results = new ArrayList<>();
        for (ITrack track: intermediateResults){
            boolean inList = false;
            int beginningPosition = 0;
            while ((beginningPosition < results.size() && !inList)){
                for(int j = beginningPosition; j < results.size(); j++){
                    ITrack trk = results.get(j);
                    if(trk.getFirst().equals(track.getFirst())){
                        inList = true;
                    }
                }
                beginningPosition = results.size();
            }
            if (!inList){
                results.add(track);
            }
        }
        return results;
    }

    private ArrayList<ITrack> process(DateTime startTime, DateTime endTime) {
        //get all events between start and end
        ArrayList<ArrayList<IEvent>> linkedEvents = null;// this.eventIndexer.getEventsBetween(startTime, endTime);
        ArrayList<ITrack> tracks = new ArrayList<ITrack>();


		/*for each event between start and end, find events
		 * that are in the next frame after the current one we are looking at.
		 * Then link an event and the current one together iff it is the only
		 * one in the search box determined by our position predictor and the
		 * length of the current event.
		 */
//        #pragma omp parallel for num_threads(8) schedule(dynamic, 10)
        for (ArrayList<IEvent> eventList: linkedEvents){

            //get search area based on time in current event.

            double span;
            DateTime startSearch;
            DateTime endSearch;
            ArrayList<Point2D.Double> searchArea;
            ArrayList<IEvent> inSearchArea;
            //TODO: Reimplement the if statements
//            if (true) {
//                    //get the search area to find tracks that may belong linked to the current one being processed
//                    span = currentEvent->getTimePeriod().length().total_seconds() / secondsToDaysConst;
//                    //set the time span to search in as the end of our current track+the
//                    //the span of the frame for the last event in our current track being processed.
//                    startSearch = currentEvent->getTimePeriod().end();
//                    endSearch = startSearch + currentEvent->getTimePeriod().length();
//                    searchArea = this->predictor->getSearchRegion(currentEvent->getBBox(), span);
//
//                    inSearchArea = idxer->getEventsBetween(startSearch, endSearch, searchArea);
//            } else if (true){
//                    Interval tp = currentEvent->getPrevious()->getTimePeriod();
//                    startSearch = currentEvent->getTimePeriod().end();
//                    endSearch = startSearch + (currentEvent->getTimePeriod().begin() - tp.begin());
//                    span = (endSearch - startSearch).total_seconds() / secondsToDaysConst;
//
//                    float* motionVect = new float[2];
//                    this->trackNormMeanMovement(currentEvent, motionVect);
//                    searchArea = this->predictor->getSearchRegion(currentEvent->getBBox(), motionVect, span);
//
//                    inSearchArea = idxer->getEventsBetween(startSearch, endSearch, searchArea);
//            }
//
//
//                //////////////////////////////////////////////////////////////////////////
//                //If the list is size one, it is the only one in our search area.
//                #pragma omp critical(pushTracks)
//                {
//                    if (inSearchArea->size() == 1) {
//                        IEvent* nextEvent = inSearchArea->back();
//                        //If the event in our search area doesn't have the previous event set
//                        // and our current event doesn't have the next event set,
//                        //then they can be linked together.
//
//
//                        if (nextEvent->getPrevious() == NULL
//                                && currentEvent->getTimePeriod().begin() < nextEvent->getTimePeriod().begin()
//                                && nextEvent->getId() != currentEvent->getId()) {
//
//                            nextEvent->setPrevious(currentEvent);
//                            currentEvent->setNext(nextEvent);
//                            tracks->push_back(TrackingFactory::GetNewTrack(currentEvent, nextEvent));
//                        }
//                        else {
//                            tracks->push_back(TrackingFactory::GetNewTrack(currentEvent, currentEvent));
//                        }
//
//                    }
//                    else {
//                        tracks->push_back(TrackingFactory::GetNewTrack(currentEvent, currentEvent));
//                    }
//                }
//            }
        }

        return tracks;
    }

    private double[] trackNormalMeanMovement(ArrayList<IEvent> events) {
        double xMovement = 0.0;
        double yMovement = 0.0;
        double totalTime = 0.0;
        double [] motionNormMean = new double[2];
        int count = 0;

        IEvent firstEvent = events.get(0);
        for(int eventIndex = 1; eventIndex < events.size(); eventIndex++){
            IEvent currentEvent = events.get(eventIndex);
            Point2D.Double currentLocation = currentEvent.getLocation();
            Point2D.Double firstLocation = firstEvent.getLocation();
            xMovement += currentLocation.getX() - firstLocation.getX();
            yMovement += currentLocation.getY() - firstLocation.getY();

            double span;
            DateTime startSearch;
            DateTime endSearch;

            Interval timePeriod = firstEvent.getTimePeriod();
            startSearch = currentEvent.getTimePeriod().getEnd();
            // TODO: Calculate time
            // startSearch.plus(currentEvent.getTimePeriod().getStart().minus(timePeriod.getStart().getMillis()));
            // endSearch = startSearch + (tmp2->getTimePeriod().begin() - tp.begin());
            startSearch = null;
            endSearch = null;
            span = ((endSearch.minusMillis((int)startSearch.getMillis())).getMillis()/ 1000) / secondsToDaysConstant;

            totalTime += span;
            firstEvent = currentEvent;
        }

        if (events.size()>0) {
            double xMean = xMovement / events.size();
            double yMean = yMovement / events.size();
            double tMean = totalTime / events.size();
            double xMeanPerTime = xMean / tMean;
            double yMeanPerTime = yMean / tMean;
            //float val = (xMeanPerTime * xMeanPerTime) + (yMeanPerTime * yMeanPerTime);
            //val = sqrt(val);

            motionNormMean[0] = xMean;
            motionNormMean[1] = yMean;
        }
        else {
            motionNormMean[0] = 0;
            motionNormMean[1] = 0;
        }
        return motionNormMean;
    }
}
