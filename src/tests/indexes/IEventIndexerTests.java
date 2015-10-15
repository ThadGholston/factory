package tests.indexes;

import edu.gsu.dmlab.geometry.Point2D;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by thad on 10/4/15.
 */
public class IEventIndexerTests {

    @Test
    public void GetExpectedChangePerFrame_NoFrames_Successful() {
    }

    @Test
    public void GetEventsBetween_NoTracks_Fails() {
    }

    @Test
    public void GetEventsBetween_IntervalIsNull_Fails() {
    }

    @Test
    public void GetEventsBetween_IntervalIsBefore_Fails() {
    }

    @Test
    public void GetEventsBetween_IntervalIsAfter_Fails() {
    }

    @Test
    public void GetEventsBetween_IntervalInTheMiddle_Success() {
    }

    @Test
    public void GetEventsInNeighborhood__NoTracks_Fails() {
    }

    @Test
    public void GetEventsInNeighborhood_IntervalIsNull_Fails() {
    }

    @Test
    public void GetEventsInNeighborhood_IntervalIsBefore_Fails() {
    }

    @Test
    public void GetEventsInNeighborhood_IntervalIsAfter_Fails() {
    }

    @Test
    public void GetEventsInNeighborhood_IntervalInTheMiddle_Success() {
    }

    @Test
    public void GetEventsInNeighborhood_SearchAreaContainsNoEvents_ReturnsNoEvents() {
    }

    @Test
    public void GetEventsInNeighborhood_SearchAreaContainsEvents_ReturnsEvents() {
    }

    @Test
    public void GetEventsInNeighborhood_SearchAreawithinMultiple_Success() {
    }

    @Test
    public void GetFirstTime_EmptyListOfTracks_Failure() {
    }

    @Test
    public void GetFirstTime_ContainsTracks_ReturnsFirstDate() {
    }


    @Test
    public void GetLastTime_EmptyListOfTracks_Failure() {

    }

    @Test
    public void GetLastTime_ContainsTracks_ReturnsLastDate() {
    }
}
