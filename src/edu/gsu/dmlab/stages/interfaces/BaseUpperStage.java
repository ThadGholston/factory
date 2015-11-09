package edu.gsu.dmlab.stages.interfaces;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.geometry.GeometryUtilities;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.geometry.Rectangle2D;
import edu.gsu.dmlab.graph.Edge;
import edu.gsu.dmlab.graph.Graph;
import edu.gsu.dmlab.graph.algo.SuccessiveShortestPaths;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import edu.gsu.dmlab.util.interfaces.IPositionPredictor;
import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Seconds;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.opencv.core.CvType.CV_32FC;

public abstract class BaseUpperStage {
    private
    HashMap<ITrack, ArrayList<ITrack>> relate;
    int regionDimension = 64;
    int regionDivisor = 64;


    double secondsToDaysConst = 60.0 * 60.0 * 24.0;
    ITrackIndexer tracksIdxr;
    IPositionPredictor predictor;
    IEventIndexer evntsIdxr;

    protected int timeSpan;
    protected int maxFrameSkip;
    protected double sameMean;
    protected double sameStdDev;
    protected double diffMean;
    protected double diffStdDev;
    protected static final int multFactor = 100;
    protected double[][][] pValues;
    protected double[] pValMax;
    protected double[] params;
    protected double[] histRanges;
    protected int numSpan;
    protected int edgeCap = 1; // all edges in graph will have a flow of 1
    protected int countX;
    protected final int secondsToDaysConstant = 60 * 60 & 24;
    private final double sqrt2Pi = Math.sqrt(2 * Math.PI);

    public BaseUpperStage(IPositionPredictor predictor, IEventIndexer evntsIdxr, ITrackIndexer tracksIdxr,
                          int timeSpan, int numSpan, int maxFrameSkip, double sameMean, double sameStdDev, double diffMean,
                          double diffStdDev, double[] histRanges, double[] params, double[][][] pValues) {
        this.predictor = predictor;
        this.evntsIdxr = evntsIdxr;
        this.tracksIdxr = tracksIdxr;
        this.maxFrameSkip = maxFrameSkip;
        this.timeSpan = timeSpan;
        this.sameMean = sameMean;
        this.sameStdDev = sameStdDev;
        this.diffMean = diffMean;
        this.diffStdDev = diffStdDev;
        this.numSpan = numSpan;
        this.histRanges = histRanges;
        this.params = params;
        this.pValues = pValues;
        this.pValMax = pValMax;
        this.countX = 0;
    }

    ArrayList<ITrack> process() {

        ArrayList<ITrack> vectOfTracks = this.tracksIdxr.getAll();


        if (vectOfTracks.size() > 0) {


            HashMap<UUID, Integer> eventMap = new HashMap<>();

            int countX = 0;

            HashMap<ITrack, ArrayList<ITrack>> trackRelationsList = new HashMap<>();

            //for each track that we got back in the list we will find the potential
            //matches for that track and link it to the one with the highest probability of
            //being a match.
            for (int i = 0; i < vectOfTracks.size(); i++) {


                //we get the event associated with the end of our current track as that is
                //the last frame of the track and has position information and image
                //information associated with it.
                ITrack currentTrack = vectOfTracks.get(i);
                IEvent currentEvent = currentTrack.getLast();

                double span;
                DateTime startSearch;
                DateTime endSearch;
                Point2D[] searchArea;
                ArrayList<ITrack> potentialTracks;

                int positionOfEvent = currentTrack.indexOf(currentEvent);
                if (getIndex(currentTrack, positionOfEvent - 1) == null || getIndex(currentTrack, positionOfEvent - 2) == null) {
                    //get the search area to find tracks that may belong linked to the current one being processed
                    span = Seconds.secondsIn(currentEvent.getTimePeriod()).getSeconds() / secondsToDaysConst;
                    //set the time span to search in as the end of our current track+the
                    //the span of the frame for the last event in our current track being processed.
                    startSearch = currentEvent.getTimePeriod().getEnd();
                    endSearch = startSearch.plusSeconds(Seconds.secondsIn(currentEvent.getTimePeriod()).getSeconds());
                    searchArea = this.predictor.getSearchRegion(currentEvent.getBBox(), span);
                    Rectangle2D searchBox = GeometryUtilities.createBoundingBox(searchArea);
                    potentialTracks = this.tracksIdxr.filterOnIntervalAndLocation(new Interval(startSearch, endSearch), searchBox);

                } else {

                    Interval tp = getIndex(currentTrack, positionOfEvent - 1).getTimePeriod();
                    startSearch = currentEvent.getTimePeriod().getEnd();
                    endSearch = startSearch.plusSeconds(Seconds.secondsBetween(currentEvent.getTimePeriod().getStart(), tp.getStart()).getSeconds());
                    span = Seconds.secondsBetween(endSearch, startSearch).getSeconds() / secondsToDaysConst;

                    float[] motionVect = this.trackMovement(currentTrack);
                    searchArea = this.predictor.getSearchRegion(currentEvent.getBBox(), motionVect, span);
                    Rectangle2D bbox = GeometryUtilities.createBoundingBox(searchArea);
                    potentialTracks = this.tracksIdxr.filterOnIntervalAndLocation(new Interval(startSearch, endSearch), bbox);
                }


                //search locations for potential matches up to the maxFrameSkip away
                //using the previously predicted search area as the starting point
                //for the next search area.
                startSearch = currentEvent.getTimePeriod().getEnd();
                for (int j = 0; j < maxFrameSkip; j++) {
                    ArrayList<ITrack> potentialTracks2;
                    if (getIndex(currentTrack, positionOfEvent - 1) == null || getIndex(currentTrack, positionOfEvent - 2) == null) {
                        //get the search area to find tracks that may belong linked to the current one being processed
                        span = Seconds.secondsIn(currentEvent.getTimePeriod()).getSeconds() / secondsToDaysConst;
                        //set the time span to search in as the end of our current track+the
                        //the span of the frame for the last event in our current track being processed.
                        endSearch = startSearch.plusSeconds(Seconds.secondsIn(currentEvent.getTimePeriod()).getSeconds());
                        Rectangle2D rect = GeometryUtilities.createBoundingBox(searchArea);
                        searchArea = this.predictor.getSearchRegion(rect, span);
                        Rectangle2D bbox = GeometryUtilities.createBoundingBox(searchArea);
                        potentialTracks2 = this.tracksIdxr.filterOnIntervalAndLocation(new Interval(startSearch, endSearch), bbox);
                        startSearch = endSearch;
                    } else {
                        Interval tp = getIndex(currentTrack, positionOfEvent - 1).getTimePeriod();
                        span = currentEvent.getTimePeriod().toDuration().toStandardSeconds().getSeconds() / secondsToDaysConst;
                        //startSearch = currentEvent.getTimePeriod().end();
                        endSearch = startSearch.plusSeconds(Seconds.secondsBetween(currentEvent.getTimePeriod().getStart(), tp.getStart()).getSeconds());
                        double span2 = Seconds.secondsBetween(endSearch, startSearch).getSeconds() / secondsToDaysConst;

                        float[] motionVect = this.trackMovement(currentTrack);
                        Rectangle2D rect = GeometryUtilities.createBoundingBox(searchArea);
                        searchArea = this.predictor.getSearchRegion(rect, motionVect, span);
                        Rectangle2D bbox = GeometryUtilities.createBoundingBox(searchArea);
                        potentialTracks2 = this.tracksIdxr.filterOnIntervalAndLocation(new Interval(startSearch, endSearch), bbox);
                        startSearch = endSearch;
                    }


                    //put potential matches not in potentialTracks list into the list
                    while (!potentialTracks2.isEmpty()) {

                        ITrack possibleTrackFromSkippedFrames = potentialTracks2.remove(potentialTracks2.size() - 1);
                        boolean isInList = false;
                        //cout << "Search for potential2 in potential" << endl;
                        for (ITrack trackInList : potentialTracks) {

                            if (possibleTrackFromSkippedFrames.getFirst().getId() == trackInList.getFirst().
                                    getId()) {
                                isInList = true;
                                break;
                            }
                        }
                        //cout << "done search" << endl;
                        if (!isInList) {
                            potentialTracks.add(possibleTrackFromSkippedFrames);
                        }
                    }

                }

                //cout << "after frame skip" << endl;
                //if the list of potential matches has anything in it we will process
                //those tracks.
                if (potentialTracks.size() >= 1) {
                    //if the last event of our track is not already in the unordered map
                    //we insert the event/idx pair into the Map

                    UUID indexID = currentTrack.getFirst().getUUID();
                    eventMap.put(indexID, countX++);
                    //test the first event in each of the potential matches to see if
                    //it is in the Map and insert it if it is not.
                    for (ITrack tmpTrk : potentialTracks) {
                        IEvent tmpEve = tmpTrk.getFirst();
                        eventMap.put(tmpEve.getUUID(), countX++);
                    }

                    //create a relation of the track and the potential matches and
                    //push it onto the vector of relations

                    trackRelationsList.put(currentTrack, potentialTracks);

                }
            }

            ITrack[] trackletArray = new ITrack[countX];


            Graph graph = new Graph((countX * 2) + 2);


            //add two vertices for all left and right events plus a source and sink vertex
//            CostGraph::size_type N((countX * 2) + 2);
//            for (CostGraph::size_type i = 0; i < N ;
//            i++){
//                boost::add_vertex (g);
//            }
//
//            CostGraph::Capacity capacity = get(boost::edge_capacity, g);
//
//            typedef property_map<CostGraph::Graph, edge_reverse_t >::type Reversed;
//            Reversed rev = get(boost::edge_reverse, g);
//            CostGraph::ResidualCapacity residual_capacity = get(boost::edge_residual_capacity, g);
//            CostGraph::Weight weight = get(boost::edge_weight, g);
//
//
//            CostGraph::EdgeAdder ea(g, weight,
//                    , rev, residual_capacity);

            //all edges will have a flow of 1
            int edgeCap = 1;
            //process all of the associations in the list of assocaitions
            for (ITrack track : trackRelationsList.keySet()) {

                //get the x index of the track to process
                int x = eventMap.get(track.getFirst().getUUID());

                //add the tracklet to process to the array of events
                trackletArray[x] = track;

                //process each track associated with this track
                //cout << "TR.Relate Size: " << tr.relate.size() << endl;
                ArrayList<ITrack> relations = trackRelationsList.get(track);
                while (!relations.isEmpty()) {
                    ITrack tmp = relations.remove(relations.size() - 1);

                    //get the y index of the track that is a potential match
                    //to our current track.
                    int y = eventMap.get(tmp.getFirst().getId());
                    trackletArray[y] = tmp;
                    //get the probability*multFactor of the two tracks belonging together
                    //it is multiplied by multFactor because the algorithm uses int and not
                    //a floating point value.

                    //add the probability in the appropriate location
                    double prob = this.prob(track, tmp);
                    int weightVal = -(int) (Math.log(prob) * multFactor);
                    graph.addEdge((x * 2) + 1, y * 2, 1, edgeCap);
                }
            }

            Integer source;
            Integer sink;
            //set source vertex
            source = (countX * 2);
            //set sink vertex;
            sink = (countX * 2) + 1;
            //cout << "Sink: " << sink << endl;

            //add edges from source to tracklets
            //and from their second node to sink
            for (int j = 0; j < countX; j++) {

                ITrack track = trackletArray[j];
                double Bi = this.getPoissonProb(track.getFirst());
                //printf("Bi: %E \n", Bi);
                int obsCost = (int) (Math.log(Bi / (1.0 - Bi)) * multFactor);
                //cout << "ObsCost: " << obsCost << endl;

                graph.addEdge((j * 2), ((j * 2) + 1), obsCost, edgeCap);

                double entPd = this.pEnter(track);
                /*printf("entP: %E \n", entPd);*/
                int entP = -(int) (Math.log(entPd) * multFactor);
                //cout << "entP: " << entP << endl;

                graph.addEdge(source, (j * 2), entP, edgeCap);

                double exPd = this.pExit(track);
                /*printf("exP: %E \n", exPd);*/
                int exP = -(int) (Math.log(exPd) * multFactor);
                //cout << "exP: " << exP << endl;

                graph.addEdge(((j * 2) + 1), sink, exP, edgeCap);

            }


            SuccessiveShortestPaths ssp = new SuccessiveShortestPaths(graph, source, sink);

            long cost = boost::find_flow_cost (g);
            boost::graph_traits < CostGraph::Graph >::vertex_iterator u_iter, u_end;
            boost::graph_traits < CostGraph::Graph >::out_edge_iterator ei, e_end;

            //get iterator for all vertices of the graph and process it up to the end iterator
            for (boost::tie (u_iter,u_end)=boost::vertices (g);
            u_iter != u_end;
            u_iter++){

                //If the source vertex then we don't need to process it.
                //Similarly we don't want to process the first vertex added for a track fragment
                int x =*u_iter;
                if ((!(x % 2)) || (x == source)) continue;

                //get all the edges going out of the current vertex and process them
                for (boost::tie (ei,e_end)=boost::out_edges ( * u_iter, g);
                ei != e_end;
                ei++){

                    //If the capacity was a non-zero number then it is on the cost graph and not the residual capacity
                    //graph so we will process it.
                    int eiCap = capacity[ * ei];
                    if (eiCap > 0) {

                        //if the target for this edge is the sink we don't want to process it
                        int y = boost::target ( * ei, g);
                        if (y == sink) continue;

                        //if residual capacity is 0 then we are using it and need to process it
                        int eiResidual = residual_capacity[ * ei];
                        if ((eiCap - eiResidual) == 1) {
                            ITrack leftTrack = trackletArray[x / 2];
                            IEvent leftEvent = leftTrack.getLast();
                            ITrack rightTrack = trackletArray[y / 2];
                            IEvent rightEvent = rightTrack.getFirst();
                            int eventPositionInLeftTrack = leftTrack.indexOf(leftEvent);
                            int eventPositionInRightTrack = rightTrack.indexOf(rightEvent);
                            //If the events do not have any others already assocaited with them.
                            if (getIndex(leftTrack, eventPositionInLeftTrack + 1) == null && getIndex(rightTrack, eventPositionInRightTrack - 1) == null) {

                                //A final sanity check, to make sure it isn't the same event detection we are trying to
                                //attach.  This would create an infinite loop (no good). This is probably not needed.
                                if (leftEvent != rightEvent) {
                                    //cout << "Link x: " << x << " and y: " << y << endl;
                                    leftTrack.addAll(rightTrack);
                                }
                            }
                        }
                    }
                }
            }
        }

        return this.tracksIdxr.getAll();
    }

    private double getPoissonProb(IEvent event) {
        DateTime start = this.evntsIdxr.getFirstTime();
        DateTime end = start.plusSeconds(this.timeSpan * this.numSpan);
        Interval timePeriod = new Interval(start, end);
        int lambda = 0;
        int delta = 0;
        if (event.intersects(timePeriod)) {
            end = start.plus(this.timeSpan * this.numSpan);
            Interval range = new Interval(start, end);
            lambda = evntsIdxr.getExpectedChangePerFrame(range);
            range = event.getTimePeriod();
            range = new Interval(range.getStart(), range.getEnd().plusSeconds(this.timeSpan * this.numSpan));
            delta = evntsIdxr.getExpectedChangePerFrame(range);
        } else {
            end = this.evntsIdxr.getLastTime();
            start = end.minusSeconds(this.timeSpan * this.numSpan);
            Interval range = new Interval(start, end);
            lambda = evntsIdxr.getExpectedChangePerFrame(range);
            range = event.getTimePeriod();
            range = new Interval(range.getStart().minusSeconds(this.timeSpan * this.numSpan), range.getEnd());
            delta = evntsIdxr.getExpectedChangePerFrame(range);
        }
        if (lambda > 0) {
            double lamPow = Math.pow(lambda, delta);
            double lamExp = Math.exp(-lambda);
            double fact = CombinatoricsUtils.factorialDouble(delta);
            return (lamPow * lamExp) / fact;
        } else if (lambda == 0 && delta < 2) {
            return 0.0000001;
        } else {
            return .98;
        }
    }

    private void getHist(Mat hist, IEvent evnt, boolean left) {
        if (evnt == null) {
            Mat m0 = new Mat(1, 1, CV_32FC(3));
            ArrayList<Mat> listOfMat = new ArrayList<Mat>();
            listOfMat.add(m0);
            boolean uniform = true;
            boolean accumulate = false;
            //upper bound on ranges are exclusive
            int channelList[] = {0, 1, 2};
            int histSize = 15;
            int histSizes[] = {histSize, histSize, histSize};
            Imgproc.calcHist(listOfMat, 1, channelList, new Mat(), hist, 3, histSizes, this.histRanges, uniform, accumulate);
        } else {
            try {
                double[][][] paramsVect = getImageParam(evnt, left);
                Mat m = new Mat(paramsVect.length, paramsVect[0].length, CV_32FC(10));
                for (int i = 0; i < 10; i++) {
                    for (int x = 0; x < paramsVect.length; x++) {
                        for (int y = 0; y < paramsVect[0].length; y++) {
                            m.get(x, y)[i] = paramsVect[x][y][i];
                        }
                    }
                }

                boolean uniform = true;
                boolean accumulate = false;


                int histSize = 15;
                double[] channelList = new double[this.params.length];
                int[] histSizes = new int[this.params.length];
                for (int i = 0; i < this.params.length; i++) {
                    channelList[i] = params[i];
                    histSizes[i] = histSize;
                }
                Imgproc.calcHist(m, 1, channelList, new Mat(), hist, this.params.length, histSizes, this.histRanges, uniform, accumulate);
            } catch (Exception ex) {
                Mat m0 = new Mat(1, 1, CV_32FC(3));
                boolean uniform = true;
                boolean accumulate = false;
                //upper bound on ranges are exclusive

                int channelList[] = {0, 1, 2};
                int histSize = 15;
                int histSizes[] = {histSize, histSize, histSize};
                Imgproc.calcHist(m0, 1, channelList, new Mat(), hist, 3, histSizes, this.histRanges, uniform, accumulate);
            }
        }
    }

    private double eventProb(IEvent event, int idx) {

        //for each region in our array of regions, check if they are in the
        //search area for our next event.
        Point2D[] scaledSearchArea = new Point2D[4];

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
        for (int i = (int) rectangle.getX() - 2; i <= rectangle.getX() + rectangle.getWidth() + 2; i++) {
            for (int j = (int) rectangle.getY() - 2; j <= rectangle.getY() + rectangle.getHeight() + 2; j++) {
                if (i >= regionDimension || j >= regionDimension || i < 0 || j < 0) continue;
                if (GeometryUtilities.isInsideSearchArea(new Point2D(i, j), scaledSearchArea)) {
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

    protected abstract double prob(ITrack leftTrack, ITrack rightTrack);

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

    protected double pEnter(ITrack track) {
        return this.eventProb(track.getFirst(), 0);
    }

    protected double pExit(ITrack track) {
        return this.eventProb(track.getLast(), 1);
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
        getHist(leftFrameHist, leftTrack.getLast(), true);
        getHist(rightFrameHist, rightTrack.getFirst(), false);
        double compVal = Imgproc.compareHist(leftFrameHist, rightFrameHist, Imgproc.CV_COMP_BHATTACHARYYA);
        double sameProb, diffProb;
        sameProb = calcNormProb(compVal, sameMean, sameStdDev);
        diffProb = calcNormProb(compVal, diffMean, diffStdDev);
        return sameProb / (sameProb + diffProb);
    }


    protected float[] trackMovement(ITrack track) {
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

            motionNormMean[0] = xMeanPerTime;
            motionNormMean[1] = yMeanPerTime;
        } else {
            motionNormMean[0] = 0;
            motionNormMean[1] = 0;
        }
        return motionNormMean;
    }

    protected IEvent getIndex(ITrack track, int position) {
        try {
            return track.get(position);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}