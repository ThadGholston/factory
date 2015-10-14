package edu.gsu.dmlab.stages.interfaces;

import edu.gsu.dmlab.graph.algo.SuccessiveShortestPaths;
import edu.gsu.dmlab.graph.algo.Edge;
import edu.gsu.dmlab.datatypes.TrackRelation;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.geometry.Rectangle2D;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import edu.gsu.dmlab.util.Utility;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.math3.special.Erf;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.*;

import static org.opencv.core.CvType.CV_32FC;
import static org.opencv.imgproc.Imgproc.calcHist;
import static org.opencv.imgproc.Imgproc.compareHist;

/**
 * Created by thad on 9/23/15.
 */
public abstract class BaseUpperStage {
    protected IPositionPredictor positionPredictor;
    protected ITrackIndexer trackIndexer;
    protected int maxFrameSkip;
    protected double sameMean;
    protected double sameStdDev;
    protected double diffMean;
    protected double diffStdDev;
    protected static final int multFactor = 100;
    protected int regionDivisor;
    protected int regionDimension;
    protected double[][][] pValues;
    protected double[] pValMax;
    protected ArrayList<Integer> params;
    protected double[] histRanges;
    //all edges will have a flow of 1
    protected int edgeCap = 1;
    //    private HashMap<UUID, Integer> eventMap;
    // private ArrayList<TrackRelation> trackRelationsList;
    protected int countX;
    protected final int secondsToDaysConstant = 60 * 60 & 24;


    public BaseUpperStage(ITrackIndexer trackIndexer, IPositionPredictor positionPredictor, Configuration configuration, int maxFrameSkip) {
        this.trackIndexer = trackIndexer;
        this.positionPredictor = positionPredictor;
        this.maxFrameSkip = configuration.getInt("maxFrameSkip");
        this.sameMean = configuration.getDouble("sameMean");
        this.sameStdDev = configuration.getDouble("sameStdDev");
        this.diffMean = configuration.getDouble("diffMean");
        this.diffStdDev = configuration.getDouble("diffStdDev");
        this.regionDivisor = configuration.getInt("regionDivisor");
        this.regionDimension = configuration.getInt("regionDimension");
//        this.eventMap = new HashMap<UUID, Integer>();
        this.countX = 0;
        // this.histRanges = new double[histRangesVec.size()];
    }

    public ArrayList<ITrack> process() {

        ArrayList<ITrack> tracks = this.trackIndexer.getAll();
        HashMap<UUID, Integer> eventMap = new HashMap<>();
        //for each track that we got back in the list we will find the potential
        //matches for that track and link it to the one with the highest probability of
        //being a match.
        ArrayList<TrackRelation> relations = new ArrayList<>();
        for (ITrack track : tracks) {
            ArrayList<ITrack> allPotentialTracks = getAllPotentialTracks(track);

            eventMap.put(track.getFirst().getUUID(), countX++);
            for (ITrack potentialTrack : allPotentialTracks) {
                eventMap.put(potentialTrack.getFirst().getUUID(), countX++);
            }
            // create a relation of the track and the potential matches and
            // push it onto the vector of relations
            TrackRelation newRelation = new TrackRelation(track, allPotentialTracks);
            relations.add(newRelation);
        }


        linkRelatedTracks(relations, eventMap);

        return this.trackIndexer.getAll();
    }


    private ArrayList<ITrack> getAllPotentialTracks(ITrack track) {
        HashMap<UUID, ITrack> potentialTracks = new HashMap<>();
        //we get the event associated with the end of our current track as that is
        //the last frame of the track and has position information and image
        //information associated with it.
        IEvent event = track.getFirst();
        DateTime startSearch = event.getTimePeriod().getEnd();
        ArrayList<ITrack> potentials = getPotentialTracks(track, startSearch, event.getBBox());
        for (ITrack t : potentials) {
            potentialTracks.put(t.getFirst().getUUID(), t);
        }

        for (int i = 0; i < maxFrameSkip; i++) {
            ArrayList<ITrack> intermediateTracks = getPotentialTracks(track, startSearch, event.getBBox());
            for (ITrack t : intermediateTracks) {
                potentialTracks.put(t.getFirst().getUUID(), t);
            }
        }
        return (ArrayList<ITrack>) potentialTracks.values();
    }


    private ArrayList<ITrack> getPotentialTracks(ITrack track, DateTime startSearch, Rectangle2D rectangle) {
        IEvent event = track.getLast();
        if (track.size() > 3) {
            double span = event.getTimePeriod().toPeriod().getSeconds() / secondsToDaysConstant;
            DateTime endSearch = startSearch.plus(event.getTimePeriod().toPeriod().getMillis());
            Point2D[] searchArea = this.positionPredictor.getSearchRegion(rectangle, span);
            return this.trackIndexer.getTracksStartBetween(startSearch, endSearch, searchArea);
        } else {
            Interval period = track.get(track.indexOf(event)).getTimePeriod();
            DateTime endSearch = startSearch.plus(event.getTimePeriod().getStartMillis() - period.getStartMillis());
            double span = ((endSearch.minus(startSearch.getMillis())).getMillis() / 1000) / secondsToDaysConstant;
            float[] motionVect = trackMovement(track);
            Point2D[] searchArea = this.positionPredictor.getSearchRegion(rectangle, motionVect, span);
            return this.trackIndexer.getTracksStartBetween(startSearch, endSearch, searchArea);
        }
    }


//    private void process(ArrayList<TrackRelation> relations, HashMap<UUID, Integer> eventMap) {
//
//
//
//    }

    private void initializeDataStructuresForSSP(ITrack[] trackletArray, SimpleDirectedWeightedGraph graph, ArrayList<TrackRelation> relations, HashMap<UUID, Integer> eventMap) {
        int capacity = 1;
        for (TrackRelation trackRelation : relations) {
            ITrack track = trackRelation.track;
            int x = eventMap.get(trackRelation.track.getFirst().getUUID());
            trackletArray[x] = trackRelation.track;
            for (ITrack possibleSuccessor : trackRelation.relatedTracks) {
                int y = eventMap.get(possibleSuccessor.getFirst().getUUID());
                trackletArray[y] = possibleSuccessor;
                double probablity = prob(track, possibleSuccessor);
                int weightValue = -(int) Math.log(probablity) * multFactor;
                Edge edge = new Edge(edgeCap);
                Integer v1 = new Integer(x * 2 + 1);
                Integer v2 = new Integer(y * 2);
                graph.addEdge(v1, v2, edge);
                graph.setEdgeWeight(edge, weightValue);
            }
        }

        Integer source = (countX * 2);
        Integer sink = (countX * 2) + 1;

        //add edges from source to tracklets
        //and from their second node to sink
        for (int j = 0; j < countX; j++) {
            ITrack track = trackletArray[j];
            double Bi = getPoissonProb(track.getFirst());
            int obsCost = (int) (Math.log(Bi / (1.0 - Bi)) * multFactor);
            Integer v1 = j * 2;
            Integer v2 = j * 2 + 1;
            Edge firstEdge = new Edge(edgeCap);
            graph.addEdge(v1, v2, firstEdge);
            graph.setEdgeWeight(firstEdge, obsCost);

            double entPd = pEnter(track);
            int entP = -(int) (Math.log(entPd) * multFactor);
            v2 = new Integer(j * 2);
            Edge secondEdge = new Edge(edgeCap);
            graph.addEdge(source, v2, secondEdge);
            graph.setEdgeWeight(secondEdge, entP);

            double exPd = pExit(track);
                /*printf("exP: %E \n", exPd);*/
            int exP = -(int) (Math.log(exPd) * multFactor);
            //cout << "exP: " << exP << endl;
            v1 = new Integer((j * 2) + 1);
            Edge thirdEdge = new Edge(edgeCap);
            graph.addEdge(v1, sink, thirdEdge);
            graph.setEdgeWeight(thirdEdge, exP);
        }
    }

    private void linkRelatedTracks(ArrayList<TrackRelation> relations, HashMap<UUID, Integer> eventMap) {
        int countX = 0;

        ITrack[] trackletArray = new ITrack[countX];
        SimpleDirectedWeightedGraph graph = new SimpleDirectedWeightedGraph(Edge.class);
        ;
        initializeDataStructuresForSSP(trackletArray, graph, relations, eventMap);
        int[] capacity = calculateCapacity(graph);
        int[] residualCapacity = calculateResidualCapacity(graph);

        //set source vertex
        Integer source = new Integer(countX * 2);
        //cout << "Source: " << source << endl;
        //set sink vertex;
        Integer sink = new Integer(countX * 2 + 1);


        SuccessiveShortestPaths ssp = new SuccessiveShortestPaths(graph);

        long cost = ssp.findFlowCost(source, sink);

        // get iterator for all vertices of the graph and process it up to the end iterator
        Set<Integer> vertices = graph.vertexSet();
        for (Integer vertex : vertices) {

            /*  If the source vertex then we don't need to process it.
                Similarly we don't want to process the first vertex added for a track fragment
                get all the edges going out of the current vertex and process them */
            Set<Integer> relevantEdges = graph.outgoingEdgesOf(vertex);
            for (Integer target : relevantEdges) {
                //If the capacity was a non-zero number then it is on the cost graph and not the residual capacity
                //graph so we will process it.
                int eiCap = capacity[target];
                if (eiCap > 0) {
                    //if the target for this edge is the sink we don't want to process it
                    int y = target;
                    if (target.equals(sink)) continue;

                    //if residual capacity is 0 then we are using it and need to process it
                    int eiResidual = residualCapacity[target];
                    if ((eiCap - eiResidual) == 1) {
                        ITrack leftTrack = trackletArray[vertex / 2];
                        IEvent leftEvent = leftTrack.getLast();
                        ITrack rightTrack = trackletArray[target / 2];
                        IEvent rightEvent = rightTrack.getFirst();


                        //A final sanity check, to make sure it isn't the same event detection we are trying to
                        //attach.  This would create an infinite loop (no good). This is probably not needed.
                        if (!leftEvent.equals(rightEvent)) {
                            leftTrack.addAll(rightTrack);
                            trackletArray[target / 2] = null;
                        }
                    }
                }
            }
        }
    }

    protected int[] calculateResidualCapacity(SimpleDirectedWeightedGraph graph){
        //TODO: Implement this
        return new int[0];
    }

    private int[] calculateCapacity(SimpleDirectedWeightedGraph graph) {
        //TODO: Implement this
        return new int[0];
    }


    private double getPoissonProb(IEvent first) {
        //TODO: Implement this
        return 0;
    }

    private double prob(ITrack track, ITrack tmpTrack) {
        return 0;
    }

    private double pEnter(ITrack track) {
        return this.eventProb(track.getFirst(), 0);
    }

    private double pExit(ITrack track) {
        return this.eventProb(track.getLast(), 1);
    }

    private double eventProb(IEvent event, int idx) {

        //for each region in our array of regions, check if they are in the
        //search area for our next event.
        Point2D[] scaledSearchArea = new Point2D[4];
//
        Rectangle2D rectangle = new Rectangle2D();
        if (event.getType().equals("SG") || event.getType().equals("FL")) {
            Rectangle2D boundingBox = event.getBBox();
            int x = (int) boundingBox.getX() / this.regionDivisor;
            int y = (int) boundingBox.getY() / regionDivisor;
            int width = (int) boundingBox.getWidth() / regionDivisor;
            int height = (int) boundingBox.getHeight() / regionDivisor;
            scaledSearchArea[0] = new Point2D(x, y);
            scaledSearchArea[1] = new Point2D(x, y + height);
            scaledSearchArea[2] = new Point2D(x + width, y + height);
            scaledSearchArea[3] = new Point2D(x + width, y);
            rectangle.setRect(x, y, width, height);
        } else {
            for (Point2D point : event.getShape()) {
                rectangle.add(new Point2D(point.getX() / regionDivisor, point.getY() / regionDivisor));
            }
        }

        double returnValue = 0;
        double minVal = 0;
        boolean isSet = false;
        int count = 0;
//        //#pragma omp parallel for collapse(2) reduction(+:returnValue, count)
        for (int i = (int) rectangle.getX() - 2; i <= rectangle.getX() + rectangle.getWidth() + 2; i++) {
            for (int j = (int) rectangle.getY() - 2; j <= rectangle.getY() + rectangle.getHeight() + 2; j++) {
//                //if inside the search area and there are events associated with the location
                if (i >= regionDimension || j >= regionDimension || i < 0 || j < 0) continue;
                if (Utility.isInsideSearchArea(new Point2D(i, j), scaledSearchArea)) {

                    if (!isSet) {
                        minVal = this.pValues[i][j][idx];
                        isSet = true;
                    } else {
                        if (this.pValues[i][j][idx] < minVal) {
                            minVal = this.pValues[i][j][idx];
                        }
                    }
                    returnValue += this.pValues[i][j][idx];
                    count++;
                }
            }
        }

        //cout << "retCount: " << returnCount << endl;
        //printf("PentEx: %E \n", returnValue);
        //returnValue = returnValue / count;
        if (minVal > 0) {
            returnValue = minVal / this.pValMax[idx];
        } else {
            returnValue = returnValue / count;
            returnValue = returnValue / this.pValMax[idx];
        }

        //if we can't calculate a value, who knows what the probability is.  We'll just cal it 50/50.
        if (returnValue <= 0) {
            returnValue = 0.5;
        }

        return returnValue;
    }

    protected double PFrameGap(ITrack leftTrack, ITrack rightTrack) {
        DateTime leftTime = leftTrack.getLast().getTimePeriod().getEnd();
        DateTime rightTime = rightTrack.getFirst().getTimePeriod().getStart();
        Interval timePeriod = new Interval(rightTime, leftTime);
        int span = (int) (leftTrack.getLast().getTimePeriod().toDurationMillis() / 1000);
        int frameSkip = (int) (timePeriod.toDurationMillis() / 1000) / span;

        if (frameSkip > 0) {
            double pExitVal = pExit(leftTrack);
            double pFalseNeg = 1 - (pExitVal);
            for (int i = 0; i < frameSkip; i++) {
                pFalseNeg *= pFalseNeg;
            }
            return pFalseNeg;
        } else {

            return 1.0;
        }
    }

    protected double PAppearance(ITrack leftTrack, ITrack rightTrack) {
        Mat leftFrameHist = new Mat();
        Mat rightFrameHist = new Mat();
//
//        #pragma omp parallel sections
//        {
//            #pragma omp section
//            {
        getHist(leftFrameHist, leftTrack.getLast(), true);
//            }
//            #pragma omp section
//            {
        getHist(rightFrameHist, rightTrack.getFirst(), false);
//            }
//        }
//
//

        double compVal = compareHist(leftFrameHist, rightFrameHist, Imgproc.CV_COMP_BHATTACHARYYA);
//        //cout << "Hist dist: " << compVal << endl;
        double sameProb, diffProb;
//
//        //#pragma omp parallel sections
//        {
//            //#pragma omp section
//            {
        sameProb = calcNormProb(compVal, sameMean, sameStdDev);
//            }
//            //#pragma omp section
//            {
        diffProb = calcNormProb(compVal, diffMean, diffStdDev);
//            }
//        }
        return sameProb / (sameProb + diffProb);
    }

    private void getHist(Mat hist, IEvent evnt, boolean left) {

        if (evnt == null) {
            Mat m0 = new Mat(1, 1, CV_32FC(3));
            boolean uniform = true;
            boolean accumulate = false;
            //upper bound on ranges are exclusive


            int channelList[] = {0, 1, 2};
            int histSize = 15;
            int histSizes[] = {histSize, histSize, histSize};
            //TODO: Fix this
            // calcHist(m0, 1, channelList, new Mat(), hist, 3, histSizes, this.histRanges, uniform, accumulate);
        } else {
            try {


                //Mat m;
                //TODO: Fix this
                ArrayList<ArrayList<ArrayList<Double>>> paramsVect = new ArrayList<>();//= this.db.getImageParam(evnt, left);

                Mat m = new Mat(paramsVect.size(), paramsVect.get(0).size(), CV_32FC(10));

                for (int i = 0; i < 10; i++) {
                    for (int x = 0; x < paramsVect.size(); x++) {
                        for (int y = 0; y < paramsVect.get(0).size(); y++) {
                            m.get(x, y)[i] = paramsVect.get(x).get(y).get(i);
                        }
                    }
                }

                boolean uniform = true;
                boolean accumulate = false;


                int histSize = 15;

                int[] channelList = new int[this.params.size()];
                int[] histSizes = new int[this.params.size()];
                for (int i = 0; i < this.params.size(); i++) {
                    channelList[i] = params.get(i);
                    histSizes[i] = histSize;
                }
                //TODO: Fix this
                // calcHist(m, 1, channelList,new Mat(), hist, this.params.size(), histSizes, this.histRanges, uniform, accumulate);
            } catch (Exception ex) {
                Mat m0 = new Mat(1, 1, CV_32FC(3));
                boolean uniform = true;
                boolean accumulate = false;
                //upper bound on ranges are exclusive

                int channelList[] = {0, 1, 2};
                int histSize = 15;
                int histSizes[] = {histSize, histSize, histSize};
                //TODO: Fix this
                // calcHist(m0, 1, channelList, new Mat(), hist, 3, histSizes, this.histRanges, uniform, accumulate);
            }
        }
    }

    private float[] trackMovement(ITrack track) {
        double xMovement = 0.0;
        double yMovement = 0.0;
        double totalTime = 0.0;
        int count = 0;

        IEvent event = track.getFirst();

        float[] motionNormMean = new float[2];

        for (IEvent currentEvent : track) {
            Point2D locationTwo = currentEvent.getLocation();
            Point2D locationOne = event.getLocation();
            xMovement += locationOne.getX() - locationTwo.getX();
            yMovement += locationOne.getY() - locationTwo.getY();

            double span;
            DateTime startSearch;
            DateTime endSearch;

            Interval timePeriod = event.getTimePeriod();
            startSearch = currentEvent.getTimePeriod().getEnd();
            endSearch = startSearch.plus(currentEvent.getTimePeriod().getStartMillis() - event.getTimePeriod().getStartMillis());
            span = ((endSearch.minus(startSearch.getMillis())).getMillis() / 1000) / secondsToDaysConstant;

            totalTime += span;
            event = currentEvent;
        }

        if (track.size() > 0) {
            double xMean = xMovement / count;
            double yMean = yMovement / count;
            double tMean = totalTime / count;
            float xMeanPerTime = (float) (xMean / tMean);
            float yMeanPerTime = (float) (yMean / tMean);
            //float val = (xMeanPerTime * xMeanPerTime) + (yMeanPerTime * yMeanPerTime);
            //val = sqrt(val);

            motionNormMean[0] = xMeanPerTime;
            motionNormMean[1] = yMeanPerTime;
        } else {
            motionNormMean[0] = 0;
            motionNormMean[1] = 0;
        }
        return motionNormMean;
    }

    private double calcNormProb(double x, double mean, double stdDev) {
        double val1 = normCDF(x - stdDev, mean, stdDev);
        double val2 = normCDF(x + stdDev, mean, stdDev);
        double val = val2 - val1;

        return val;
    }

    private double normCDF(double x, double mean, double stdDev) {
        double val = (x - mean) / (stdDev * Math.sqrt(2.0));
        val = 1.0 + Erf.erf(val);
        val = val * 0.5;
        return val;
    }

}
