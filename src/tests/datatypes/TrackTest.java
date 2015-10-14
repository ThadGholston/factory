package tests.datatypes;

import edu.gsu.dmlab.datatypes.GenaricEvent;
import edu.gsu.dmlab.datatypes.Track;
import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 * Created by thad on 9/19/15.
 */
public class TrackTest {



    @Test
    public void GetStartTimeMillis_TrackWithOneEvent_() throws Exception {
        IEvent mockedEvent = mock(GenaricEvent.class);
        long startTimeMillis = 111111111;
        long endTimeMillis = 33333333;
        when(mockedEvent.getTimePeriod()).thenReturn(new Interval(startTimeMillis, endTimeMillis));
        Track track = new Track((mockedEvent));
        Assert.assertEquals(track.getStartTimeMillis(), startTimeMillis);
    }

    @Test
    public void GetEndTimeMillis_TrackWithOneEvent_() throws Exception {
        IEvent mockedEvent = mock(GenaricEvent.class);
        long startTimeMillis = 111111111;
        long endTimeMillis = 33333333;
        when(mockedEvent.getTimePeriod()).thenReturn(new Interval(startTimeMillis, endTimeMillis));
        Track track = new Track((mockedEvent));
        Assert.assertEquals(track.getEndTimeMillis(), endTimeMillis);
    }

    @Test
    public void GetEndTimeMillis_TrackCreatedWithEvents_() {
        IEvent mockedEvent1 = mock(GenaricEvent.class);
        long startTimeMillisOne = 111111111;
        long endTimeMillisOne = 33333333;
        long startTimeMillisTwo = 111111111;
        long endTimeMillisTwo = 33333333;
        when(mockedEvent1.getTimePeriod()).thenReturn(new Interval(startTimeMillisOne, endTimeMillisOne));
        IEvent mockedEvent2 = mock(GenaricEvent.class);
        IEvent mockedEvent3 = mock(GenaricEvent.class);
        when(mockedEvent3.getTimePeriod()).thenReturn(new Interval(startTimeMillisTwo, endTimeMillisTwo));
        Track track = new Track(mockedEvent1);
        track.add(mockedEvent2);
        track.add(mockedEvent3);
        Assert.assertEquals(track.getEndTimeMillis(), endTimeMillisTwo);
    }

    @Test
    public void GetEvents_TrackCreatedWith3Events_ArrayIsSameAsList() throws Exception {
        IEvent mockedEvent1 = mock(GenaricEvent.class);
        IEvent mockedEvent2 = mock(GenaricEvent.class);
        IEvent mockedEvent3 = mock(GenaricEvent.class);
        Track track = new Track(mockedEvent1);
        track.add(mockedEvent2);
        track.add(mockedEvent3);
        IEvent[] events = track.getEvents();
        Assert.assertArrayEquals(events, new IEvent[]{mockedEvent1, mockedEvent2, mockedEvent3});
    }

    @Test
    public void GetFirst_TrackCreatedWith3Events_ReturnsFirstTrackSuccessfully() throws Exception {
        IEvent mockedEvent1 = mock(GenaricEvent.class);
        IEvent mockedEvent2 = mock(GenaricEvent.class);
        IEvent mockedEvent3 = mock(GenaricEvent.class);
        Track track = new Track(mockedEvent1);
        track.add(mockedEvent2);
        track.add(mockedEvent3);
        Assert.assertEquals(track.getFirst(), mockedEvent1);
    }

    @Test
    public void GetLast_TrackCreatedWith3Events_() throws Exception {
        IEvent mockedEvent1 = mock(GenaricEvent.class);
        IEvent mockedEvent2 = mock(GenaricEvent.class);
        IEvent mockedEvent3 = mock(GenaricEvent.class);
        Track track = new Track(mockedEvent1);
        track.add(mockedEvent2);
        track.add(mockedEvent3);
        Assert.assertEquals(track.getLast(), mockedEvent3);
    }

    @Test
    public void GetTimePeriod_TrackContainsEvents_TimePeriodFromFirstToLast() throws Exception {
        IEvent mockedEvent0 = mock(GenaricEvent.class);
        long startTimeMillis1 = 111111111;
        long endTimeMillis1 = 33333333;
        when(mockedEvent0.getTimePeriod()).thenReturn(new Interval(startTimeMillis1, endTimeMillis1));
        IEvent mockedEvent4 = mock(GenaricEvent.class);
        long startTimeMillis2 = 444444444;
        long endTimeMillis2 = 555555555;
        when(mockedEvent4.getTimePeriod()).thenReturn(new Interval(startTimeMillis2, endTimeMillis2));
        IEvent mockedEvent1 = mock(GenaricEvent.class);
        IEvent mockedEvent2 = mock(GenaricEvent.class);
        IEvent mockedEvent3 = mock(GenaricEvent.class);
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

    }

    @Test
    public void CompareTime_OldTrackYoungTrack_() {

    }

    @Test
    public void CompareTime_YoungTrackYoungTrack_() {

    }

    @Test
    public void CompareTime_YoungTrackOldTrack_() {

    }

    @Test
    public void CompareTime_TrackWithNoEvents_() {

    }


}