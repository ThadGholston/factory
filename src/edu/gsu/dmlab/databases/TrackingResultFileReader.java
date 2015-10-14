/**
 * File:TrackingResultFileReader.java reads the tracking results from a 
 * file and produces a set of tracks based upon the information in that file.
 *   
 * @author Dustin Kempton
 * @version 05/14/2015 
 * @Owner Data Mining Lab, Georgia State University
 */
package edu.gsu.dmlab.databases;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.geometry.Rectangle2D;
import org.joda.time.Interval;

import edu.gsu.dmlab.conversion.CoordinateSystemConverter;
import edu.gsu.dmlab.datatypes.GenaricEvent;
import edu.gsu.dmlab.datatypes.Track;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;

public class TrackingResultFileReader {
	int span;
	String fileLocation;
	SimpleDateFormat formatter = null;

	public TrackingResultFileReader(String fileLocation, int span) {
		if (fileLocation == null)
			throw new IllegalArgumentException(
					"fileLocation cannot be null in GenaricEvet constructor.");
		this.fileLocation = fileLocation;
		this.span = span;
		this.formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public ITrack[] getTracks(String type) throws IOException {

		ArrayList<ITrack> tmpList = new ArrayList<ITrack>();

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(this.fileLocation
					+ File.separator + type + "DustinTracked.txt"));
			String line = null;

			int lastTrackId = 0;
			int count = 0;
			IEvent lastEvent = null;
			ITrack track = new Track();
			while ((line = in.readLine()) != null) {
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
					Point2D[] hpc_bbox_poly = this.getPoly(hpc_bbox_string);

					Point2D[] hpc_ccode = null;
					if (!hpc_ccode_string.isEmpty()) {
						hpc_ccode_string = this
								.removeBrackets(hpc_ccode_string);
						hpc_ccode = this.getPoly(hpc_ccode_string);
					} else {
						hpc_ccode = this.getPoly(hpc_bbox_string);
					}

					Date startDate = this.formatter.parse(startTimeString);

					// the * 1000 is to convert seconds into milliseconds
					Interval range = new Interval(startDate.getTime(),
							startDate.getTime() + (this.span * 1000));

					IEvent ev = new GenaricEvent(count++, range, tmp_coord,
							this.getRect(hpc_bbox_poly), hpc_ccode,
							eventTypeString);
					if (trackId == lastTrackId) {
						track.add(ev);
						Interval tmpRange = lastEvent.getTimePeriod();
						Interval newRange = tmpRange.withEndMillis(range
								.getEndMillis());
						// new Interval(
						// tmpRange.getStartMillis(),
						// range.getStartMillis());
						lastEvent.updateTimePeriod(newRange);
						lastEvent = ev;
					} else {
						tmpList.add(track);
						track = new Track();
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
			in.close();
		}
		ITrack[] retVect = new ITrack[tmpList.size()];
		tmpList.toArray(retVect);
		return retVect;
	}

	/**
	 * getPoly: returns a list of 2D points extracted from the input string the
	 * list of points are assumed to create a polygon, they are not tested
	 * 
	 * @param pointsString
	 *            :the string to extract the points from
	 * @return :returns the list of 2D points
	 */
	private Point2D[] getPoly(String pointsString) {
		String[] pointsStrings = pointsString.split(",");
		String xy;
		ArrayList<Point2D> pointsList = new ArrayList<Point2D>();
		for (int i = 0; i < pointsStrings.length; i++) {
			xy = pointsStrings[i];
			pointsList.add(this.getPoint(xy));
		}

		Point2D[] points = new Point2D[pointsList.size()];
		pointsList.toArray(points);
		return points;
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

	private Rectangle2D getRect(Point2D[] poly) {
		double minX, minY, maxX, maxY;

		Point2D point = poly[0];
		// Point2i point = this->convertToPixXY(pointf);
		minX = point.x;
		maxX = point.x;
		minY = point.y;
		maxY = point.y;

		for (int i = 1; i < poly.length; i++) {
			// Point2d pointf = iterator.operator *();
			point = poly[i];
			if (point.x < minX)
				minX = point.x;
			if (point.x > maxX)
				maxX = point.x;
			if (point.y < minY)
				minY = point.y;
			if (point.y > maxY)
				maxY = point.y;
		}

		Rectangle2D rec = new Rectangle2D();
		rec.x = (int) minX;
		rec.y = (int) minY;
		rec.height = (int) (maxY - minY);
		rec.width = (int) (maxX - minX);
		return rec;
	}

}
