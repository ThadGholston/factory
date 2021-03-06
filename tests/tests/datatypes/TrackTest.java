package tests.datatypes;

import edu.gsu.dmlab.datatypes.GenericEvent;
import edu.gsu.dmlab.datatypes.Track;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Created by thad on 9/19/15.
 * 
 */
public class TrackTest {



    @Test
    public void GetStartTimeMillis_TrackWithOneEvent_() throws Exception {
        IEvent mockedEvent = mock(GenericEvent.class);
        long startTimeMillis = 111111111;
        long endTimeMillis = 333333333;
        Interval time = new Interval(startTimeMillis, endTimeMillis);
        when(mockedEvent.getTimePeriod()).thenReturn(time);
        Track track = new Track((mockedEvent));
        Assert.assertEquals(track.getStartTimeMillis(), startTimeMillis);
    }

    @Test
    public void GetEndTimeMillis_TrackWithOneEvent_() throws Exception {
        IEvent mockedEvent = mock(GenericEvent.class);
        long startTimeMillis = 11111111;
        long endTimeMillis = 33333333;
        when(mockedEvent.getTimePeriod()).thenReturn(new Interval(startTimeMillis, endTimeMillis));
        Track track = new Track((mockedEvent));
        Assert.assertEquals(track.getEndTimeMillis(), endTimeMillis);
    }

    @Test
    public void GetEndTimeMillis_TrackCreatedWithEvents_() {
        IEvent mockedEvent1 = mock(GenericEvent.class);
        long startTimeMillisOne = 11111111;
        long endTimeMillisOne = 33333333;
        long startTimeMillisTwo = 11111111;
        long endTimeMillisTwo = 33333333;
        when(mockedEvent1.getTimePeriod()).thenReturn(new Interval(startTimeMillisOne, endTimeMillisOne));
        IEvent mockedEvent2 = mock(GenericEvent.class);
        IEvent mockedEvent3 = mock(GenericEvent.class);
        when(mockedEvent3.getTimePeriod()).thenReturn(new Interval(startTimeMillisTwo, endTimeMillisTwo));
        Track track = new Track(mockedEvent1);
        track.add(mockedEvent2);
        track.add(mockedEvent3);
        Assert.assertEquals(track.getEndTimeMillis(), endTimeMillisTwo);
    }

    @Test
    public void GetEvents_TrackCreatedWith3Events_ArrayIsSameAsList() throws Exception {
        IEvent mockedEvent1 = mock(GenericEvent.class);
        IEvent mockedEvent2 = mock(GenericEvent.class);
        IEvent mockedEvent3 = mock(GenericEvent.class);
        Track track = new Track(mockedEvent1);
        track.add(mockedEvent2);
        track.add(mockedEvent3);
        IEvent[] events = track.getEvents();
        Assert.assertArrayEquals(events, new IEvent[]{mockedEvent1, mockedEvent2, mockedEvent3});
    }

    @Test
    public void GetFirst_TrackCreatedWith3Events_ReturnsFirstTrackSuccessfully() throws Exception {
        IEvent mockedEvent1 = mock(GenericEvent.class);
        IEvent mockedEvent2 = mock(GenericEvent.class);
        IEvent mockedEvent3 = mock(GenericEvent.class);
        Track track = new Track(mockedEvent1);
        track.add(mockedEvent2);
        track.add(mockedEvent3);
        Assert.assertEquals(track.getFirst(), mockedEvent1);
    }

    @Test
    public void GetLast_TrackCreatedWith3Events_() throws Exception {
        IEvent mockedEvent1 = mock(GenericEvent.class);
        IEvent mockedEvent2 = mock(GenericEvent.class);
        IEvent mockedEvent3 = mock(GenericEvent.class);
        Track track = new Track(mockedEvent1);
        track.add(mockedEvent2);
        track.add(mockedEvent3);
        Assert.assertEquals(track.getLast(), mockedEvent3);
    }

    @Test
    public void GetTimePeriod_TrackContainsEvents_TimePeriodFromFirstToLast() throws Exception {
        IEvent mockedEvent0 = mock(GenericEvent.class);
        long startTimeMillis1 = 11111111;
        long endTimeMillis1 = 33333333;
        when(mockedEvent0.getTimePeriod()).thenReturn(new Interval(startTimeMillis1, endTimeMillis1));
        IEvent mockedEvent4 = mock(GenericEvent.class);
        long startTimeMillis2 = 44444444;
        long endTimeMillis2 = 55555555;
        when(mockedEvent4.getTimePeriod()).thenReturn(new Interval(startTimeMillis2, endTimeMillis2));
        IEvent mockedEvent1 = mock(GenericEvent.class);
        IEvent mockedEvent2 = mock(GenericEvent.class);
        IEvent mockedEvent3 = mock(GenericEvent.class);
        Track track = new Track(mockedEvent0);
        track.add(mockedEvent1);
        track.add(mockedEvent2);
        track.add(mockedEvent3);
        track.add(mockedEvent4);
        track.getTimePeriod();
        Assert.assertEquals(track.getTimePeriod(), new Interval(startTimeMillis1, endTimeMillis2));
    }

    @Test
    public void GetTimePeriod_AlteredTrack_ReturnsCorrectInterval() throws Exception {
    	fail("Not Implemented");
    }

    @Test
    public void CompareTime_OldTrackYoungTrack_() {
    	fail("Not Implemented");
    }

    @Test
    public void CompareTime_YoungTrackYoungTrack_() {
    	fail("Not Implemented");
    }

    @Test
    public void CompareTime_YoungTrackOldTrack_() {
    	fail("Not Implemented");
    }

    @Test
    public void CompareTime_TrackWithNoEvents_() {
    	fail("Not Implemented");
    }


}