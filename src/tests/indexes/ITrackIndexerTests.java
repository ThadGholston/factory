package tests.indexes;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;
import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * Created by thad on 10/4/15.
 */

import org.junit.Test;

public class ITrackIndexerTests {


    @Test
    public void GetTracksStartBetween() {
        //DateTime begin, DateTime end, Point2D[] searchArea
    }

    @Test
    public void GetTracksEndBetween() {
        //DateTime begin, DateTime end, Point2D[] searchArea
    }

    @Test
    public void GetAll_FullList_ReturnAllTracks() {
        //
    }

    @Test
    public void GetAll_TracksMergedTogether_ReturnUpdatedTracks() {
        //
    }

    @Test
    public void GetBetween_IntervalIsBefore_ReturnNoTracks() {
        //DateTime start, DateTime end
    }

    @Test
    public void GetBetween__TracksMergedTogether_IntervalIsBefore_ReturnNoTracks() {
        //DateTime start, DateTime end
    }
    //    _TracksMergedTogether_

    @Test
    public void GetBetween_IntervalIsAfter_ReturnNoTracks() {
        //DateTime start, DateTime end
    }

    @Test
    public void GetBetween__TracksMergedTogether_IntervalIsAfter_ReturnNoTracks() {
        //DateTime start, DateTime end
    }

    @Test
    public void GetBetween_IntervalIsInTheMiddle_ReturnTracks() {
        //DateTime start, DateTime end
    }

    @Test
    public void GetBetween__TracksMergedTogether_IntervalIsInTheMiddle_ReturnTracks() {
        //DateTime start, DateTime end
    }

    @Test
     public void GetBetween_OutSideOfSearchArea_ReturnTracks() {
        //DateTime start, DateTime end, Point2D[] searchArea
    }

    @Test
    public void GetBetween_TracksMergedTogether_OutSideOfSearchArea_ReturnTracks() {
        //DateTime start, DateTime end, Point2D[] searchArea
    }

    @Test
    public void GetBetween_InsideOfSearchArea_ReturnTracks() {
        //DateTime start, DateTime end, Point2D[] searchArea
    }


    @Test
    public void GetBetween_TracksMergedTogether_InsideOfSearchArea_ReturnTracks() {
        //DateTime start, DateTime end, Point2D[] searchArea
    }
    @Test
    public void GetFirstTime_Empty_ReturnNoTime() {
    }

    @Test
    public void GetLastTime_TracksMergedTogether__Empty_ReturnsNoTime() {
    }

    @Test
    public void GetLastTime_Empty_ReturnsNoTime() {
    }

    @Test
    public void GetFirstTime_TracksMergedTogether__Empty_ReturnNoTime() {
    }
}
