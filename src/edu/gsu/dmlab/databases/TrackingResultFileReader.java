/**
 * File:TrackingResultFileReader.java reads the tracking results from a 
 * file and produces a set of tracks based upon the information in that file.
 *   
 * @author Dustin Kempton
 * @version 05/14/2015 
 * @Owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab.databases;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.exceptions.UnknownEventTypeException;
import edu.gsu.dmlab.geometry.Point2D;
import org.joda.time.Interval;

import edu.gsu.dmlab.conversion.CoordinateSystemConverter;
import edu.gsu.dmlab.datatypes.GenericEvent;
import edu.gsu.dmlab.datatypes.Track;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;

public class TrackingResultFileReader {
	int span;
	SimpleDateFormat formatter = null;
	BufferedReader in;

	public TrackingResultFileReader(BufferedReader in, int span) {
		if (in == null)
			throw new IllegalArgumentException("fileLocation cannot be null in GenericEvent constructor.");
		this.in = in;
		this.span = span;
		this.formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public ITrack[] getTracks(String type) throws IOException, UnknownEventTypeException {

		ArrayList<ITrack> tmpList = new ArrayList<>();

		try {
			String line;

			int lastTrackId = 0;
			int count = 0;
			IEvent lastEvent = null;
			ITrack track = null;
			while ((line = this.in.readLine()) != null) {
				String[] lineSplit = line.split("\t");
				if (lineSplit.length > 6) {
					String eventTypeString, startTimeString, hpc_coord_string, hpc_bbox_string, hpc_ccode_string;
					int trackId = Integer.parseInt(lineSplit[0]);

					startTimeString = lineSplit[1];
					eventTypeString = lineSplit[3];
					hpc_coord_string = lineSplit[4];
					hpc_bbox_string = lineSplit[5];
					hpc_ccode_string = lineSplit[6];

					hpc_coord_string = this.removeBrackets(hpc_coord_string);
					hpc_bbox_string = this.removeBrackets(hpc_bbox_string);

					Point2D tmp_coord = this.getPoint(hpc_coord_string);

					Polygon hpc_ccode = null;
					if (!hpc_ccode_string.isEmpty()) {
						hpc_ccode_string = this.removeBrackets(hpc_ccode_string);
						hpc_ccode = this.getPoly(hpc_ccode_string);
					} else {
						hpc_ccode = this.getPoly(hpc_bbox_string);
					}

					Date startDate = this.formatter.parse(startTimeString);

					// the * 1000 is to convert seconds into milliseconds
					Interval range = new Interval(startDate.getTime(), startDate.getTime() + (this.span * 1000));

					EventType eventType = null;
					switch (eventTypeString) {
					case "AR":
						eventType = EventType.ACTIVE_REGION;
						break;
					case "CH":
						eventType = EventType.CORONAL_HOLE;
						break;
					case "EF":
						eventType = EventType.EMERGING_FLUX;
						break;
					case "FI":
						eventType = EventType.FILAMENT;
						break;
					case "FL":
						eventType = EventType.FLARE;
						break;
					case "SG":
						eventType = EventType.SIGMOID;
						break;
					case "SS":
						eventType = EventType.SUNSPOT;
						break;
					default:
						throw new UnknownEventTypeException("Unrecognized event type: " + eventTypeString);
					}

					//TODO: this is fucked fix it!
					IEvent ev = new GenericEvent(count++, range, tmp_coord, hpc_ccode.getBounds(), hpc_ccode,
							eventType);
					if (trackId == lastTrackId) {
						if (track == null) {
							track = new Track(ev);
						} 
						Interval tmpRange = lastEvent.getTimePeriod();
						Interval newRange = tmpRange.withEndMillis(range.getEndMillis());
						// new Interval(
						// tmpRange.getStartMillis(),
						// range.getStartMillis());
						lastEvent.updateTimePeriod(newRange);
						lastEvent = ev;
					} else {
						tmpList.add(track);
						track = null;
						lastEvent = ev;
						lastTrackId = trackId;
					}
				} else {
					System.out.println("Split is not correct.");
				}

			}

		} catch (ParseException e) {
			System.out.println("Failed to parse starttime?");
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
		}
		ITrack[] retVect = new ITrack[tmpList.size()];
		tmpList.toArray(retVect);
		return retVect;
	}

	/**
	 * getPoly: returns a objectList of 2D points extracted from the input
	 * string the objectList of points are assumed to create a polygon, they are
	 * not tested
	 * 
	 * @param pointsString
	 *            :the string to extract the points from
	 * @return :returns the objectList of 2D points
	 */
	private Polygon getPoly(String pointsString) {
		String[] pointsStrings = pointsString.split(",");
		String xy;
		ArrayList<Integer> xPoints = new ArrayList<Integer>();
		ArrayList<Integer> yPoints = new ArrayList<Integer>();
		for (int i = 0; i < pointsStrings.length; i++) {
			xy = pointsStrings[i];
			Point2D pnt = this.getPoint(xy);
			xPoints.add((int) pnt.x);
			yPoints.add((int) pnt.y);
		}
		int[] xArr = new int[xPoints.size()];
		int[] yArr = new int[yPoints.size()];
		for (int i = 0; i < xArr.length; i++) {
			xArr[i] = xPoints.get(i);
			yArr[i] = yPoints.get(i);
		}

		Polygon poly = new Polygon(xArr, yArr, xArr.length);
		return poly;
	}

	/**
	 * getPoint :returns a 2D point from the input string
	 * 
	 * @param xy
	 *            :input string containing x and y coordinate
	 * @return :the 2D point extracted from the string
	 */
	private Point2D getPoint(String xy) {
		int spaceIdx = xy.indexOf(' ');
		double x = Double.parseDouble(xy.substring(0, spaceIdx));
		double y = Double.parseDouble(xy.substring(spaceIdx));
		return (Point2D) CoordinateSystemConverter.convertHPCToPixXY(new Point2D(x, y));
	}

	/**
	 * removeBrackets : used to remove anything preceeding or following a ( or a
	 * )
	 * 
	 * @param in
	 *            : the string to trim up
	 * @return : the trimmed string
	 */
	private String removeBrackets(String in) {
		int begin = in.indexOf('(');
		int end = in.lastIndexOf(')');
		while (begin >= 0 || end >= 0) {
			in = in.substring(begin + 1, end);
			begin = in.indexOf('(');
			end = in.lastIndexOf(')');
		}
		return in;
	}

	/*
	 * private Rectangle getRect(Point2D[] poly) { double minX, minY, maxX,
	 * maxY;
	 * 
	 * Point2D point = poly[0]; // Point2i point = this->convertToPixXY(pointf);
	 * minX = point.x; maxX = point.x; minY = point.y; maxY = point.y;
	 * 
	 * for (int i = 1; i < poly.length; i++) { // Point2d pointf =
	 * iterator.operator *(); point = poly[i]; if (point.x < minX) minX =
	 * point.x; if (point.x > maxX) maxX = point.x; if (point.y < minY) minY =
	 * point.y; if (point.y > maxY) maxY = point.y; }
	 * 
	 * Rectangle2D rec = new Rectangle2D(); rec.x = (int) minX; rec.y = (int)
	 * minY; rec.height = (int) (maxY - minY); rec.width = (int) (maxX - minX);
	 * return rec; }
	 */

}
