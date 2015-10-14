package edu.gsu.dmlab.databases;

import java.awt.geom.Rectangle2D;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.sql.DataSource;

import org.joda.time.Interval;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import edu.gsu.dmlab.databases.interfaces.IImageDBConnection;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;

public class ImageDBConnection implements IImageDBConnection {
	DataSource dsourc = null;
	LoadingCache<CacheKey, float[][][]> cache = null;
	int paramDownSample = 64;
	int paramDim = 10;
	HashFunction hashFunct = Hashing.md5();

	private class CacheKey {
		String key;
		IEvent event;
		int wavelength;
		boolean leftSide;
		HashCode hc = null;

		public CacheKey(IEvent event, boolean leftSide, int wavelength,
						HashFunction hashFunct) {
			StringBuilder evntName = new StringBuilder();
			evntName.append(event.getUUID());
			evntName.append(wavelength);
			if (leftSide) {
				evntName.append("T");
			} else {
				evntName.append("F");
			}
			this.key = evntName.toString();
			this.hc = hashFunct.newHasher().putString(this.key, Charsets.UTF_8)
					.hash();
			this.event = event;
			this.leftSide = leftSide;
			this.wavelength = wavelength;
		}

		@Override
		public void finalize() {
			this.hc = null;
			this.key = null;
			this.event = null;
		}

		public IEvent getEvent() {
			return this.event;
		}

		public boolean isLeftSide() {
			return this.leftSide;
		}

		public int getWavelength() {
			return this.wavelength;
		}

		public String getKey() {
			return key;
		}

		@Override
		public int hashCode() {
			return hc.asInt();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CacheKey) {
				CacheKey val = (CacheKey) obj;
				return val.getKey().compareTo(this.key) == 0;
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			System.out.println("toString");
			return this.key;
		}
	}

	public ImageDBConnection(DataSource dsourc, int maxCacheSize) {
		if (dsourc == null)
			throw new IllegalArgumentException(
					"DataSource cannot be null in HomeMySQL_DBConnection constructor.");
		this.dsourc = dsourc;
		System.out.println("MaxCacheSize:" + maxCacheSize);
		this.cache = CacheBuilder.newBuilder().maximumSize(maxCacheSize)
				.build(new CacheLoader<CacheKey, float[][][]>() {
					public float[][][] load(CacheKey key) {
						return fetchImageParams(key);
					}
				});
	}

	@Override
	public float[][][] getImageParam(IEvent event, int wavelength,
									 boolean leftSide) {
		CacheKey key = new CacheKey(event, leftSide, wavelength, this.hashFunct);
		float[][][] returnValue = this.cache.getUnchecked(key);
		key = null;
		return returnValue;
	}

	@Override
	public Mat getFirstImage(Interval period, int wavelength)
			throws SQLException {
		Connection con = null;
		Mat ret_img = null;
		try {
			con = this.dsourc.getConnection();
			con.setAutoCommit(true);

			// for pulling out the year and month for the table name to select
			// from
			Calendar calStart = Calendar.getInstance();
			calStart.setTimeInMillis(period.getStartMillis());

			String fileIdStatmentString = "SELECT id FROM files_"
					+ calStart.get(Calendar.YEAR);
			if ((calStart.get(Calendar.MONTH) + 1) < 10) {
				fileIdStatmentString += "0"
						+ (calStart.get(Calendar.MONTH) + 1);
			} else {
				fileIdStatmentString += "" + (calStart.get(Calendar.MONTH) + 1);
			}
			fileIdStatmentString += " WHERE (((startdate BETWEEN ? AND ?) OR (enddate BETWEEN ? AND ?)) AND (wavelength = ?))"
					+ " ORDER BY startdate;";

			PreparedStatement file_id_prep_stmt = con
					.prepareStatement(fileIdStatmentString);

			// the starttime and endtime of the query interval
			Timestamp startTime = new Timestamp(period.getStartMillis());
			Timestamp endTime = new Timestamp(period.getEndMillis());

			file_id_prep_stmt.setTimestamp(1, startTime);
			file_id_prep_stmt.setTimestamp(2, endTime);
			file_id_prep_stmt.setTimestamp(3, startTime);
			file_id_prep_stmt.setTimestamp(4, endTime);
			file_id_prep_stmt.setInt(5, wavelength);

			ResultSet res = file_id_prep_stmt.executeQuery();

			if (res.next()) {

				int frameId = res.getInt(1);
				String queryString2 = "SELECT image_file FROM image_files_"
						+ calStart.get(Calendar.YEAR);
				if ((calStart.get(Calendar.MONTH) + 1) < 10) {
					queryString2 += "0" + (calStart.get(Calendar.MONTH) + 1);
				} else {
					queryString2 += "" + (calStart.get(Calendar.MONTH) + 1);
				}
				queryString2 += " WHERE file_id = ? ;";
				PreparedStatement img_prep_stmt = con
						.prepareStatement(queryString2);
				img_prep_stmt.setInt(1, frameId);

				ResultSet imgRslts = img_prep_stmt.executeQuery();

				if (imgRslts.next()) {
					Blob blob = imgRslts.getBlob(1);

					Mat img_data = new MatOfByte(blob.getBytes(1,
							(int) blob.length()));
					ret_img = Highgui.imdecode(img_data, Highgui.IMREAD_COLOR);
				}
			}

		} finally {
			if (con != null) {
				con.close();
			}
		}
		return ret_img;
	}

	private float[][][] fetchImageParams(CacheKey key) {
		float[][][] retVal = null;

		String queryString = this.buildQueryString(key);
		Timestamp startTime = new Timestamp(key.getEvent().getTimePeriod()
				.getStartMillis());
		Timestamp endTime = new Timestamp(key.getEvent().getTimePeriod()
				.getEndMillis());

		Rectangle2D.Double tmpBbox = key.getEvent().getBBox();

		Rectangle2D.Double rec = new Rectangle2D.Double((tmpBbox.x / this.paramDownSample) - 4,
				(tmpBbox.y / this.paramDownSample) - 4,
				(tmpBbox.width / this.paramDownSample) + 8,
				(tmpBbox.height / this.paramDownSample) + 8);

		retVal = new float[(int)rec.height][(int)rec.width][this.paramDim];

		int tryCount = 0;
		boolean executed = false;
		while (!executed && tryCount < 3) {
			try {
				Connection con = null;
				try {
					con = this.dsourc.getConnection();
					con.setAutoCommit(true);

					PreparedStatement param_prep_stmt = con
							.prepareStatement(queryString);
					param_prep_stmt.setTimestamp(1, startTime);
					param_prep_stmt.setTimestamp(2, endTime);
					param_prep_stmt.setTimestamp(3, startTime);
					param_prep_stmt.setTimestamp(4, endTime);
					param_prep_stmt.setInt(5, key.getWavelength());
					param_prep_stmt.setInt(6, (int)rec.x);
					param_prep_stmt.setInt(7, (int)rec.x + (int)rec.height - 1);
					param_prep_stmt.setInt(8, (int)rec.y);
					param_prep_stmt.setInt(9, (int)rec.y + (int)rec.width - 1);

					ResultSet imgParamRslts = param_prep_stmt.executeQuery();
					while (imgParamRslts.next()) {
						int x = imgParamRslts.getInt("x");
						x = x - (int)rec.x;

						int y = imgParamRslts.getInt("y");
						y = y - (int)rec.y;

						for (int i = 1; i < 11; i++) {
							retVal[x][y][i - 1] = imgParamRslts.getFloat("p"
									+ i);
						}
					}
					executed = true;

				} catch (SQLException ex) {
					ex.printStackTrace();
				} finally {
					if (con != null) {
						con.close();
					}
				}
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
			tryCount++;
		}
		return retVal;
	}

	private String buildQueryString(CacheKey key) {
		// for constructing the table names from the year and month of
		// the event
		Calendar calStart = Calendar.getInstance();
		Interval period = key.getEvent().getTimePeriod();
		calStart.setTimeInMillis(period.getStartMillis());
		String calStartYear = "" + calStart.get(Calendar.YEAR);
		String calStartMonth;
		if ((calStart.get(Calendar.MONTH) + 1) < 10) {
			calStartMonth = "0" + (calStart.get(Calendar.MONTH) + 1);
		} else {
			calStartMonth = "" + (calStart.get(Calendar.MONTH) + 1);
		}
		StringBuilder queryString = new StringBuilder();
		queryString
				.append("SELECT x, y, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10 ");
		queryString.append("FROM(SELECT file_id FROM ( SELECT id FROM files_");
		queryString.append(calStartYear);
		queryString.append(calStartMonth);
		queryString
				.append(" WHERE(((startdate BETWEEN ? AND ?) OR (enddate BETWEEN ? AND ?))");
		queryString.append(" AND (wavelength = ?)) ORDER BY startdate");
		if (key.isLeftSide()) {
			queryString.append(" DESC) AS t1 ");
		} else {
			queryString.append(" ASC) AS t1 ");
		}
		queryString.append("INNER JOIN image_params_");
		queryString.append(calStartYear);
		queryString.append(calStartMonth);
		queryString.append(" ON image_params_");
		queryString.append(calStartYear);
		queryString.append(calStartMonth);
		queryString.append(".file_id = t1.id ");
		queryString.append("LIMIT 1) AS t2 LEFT JOIN ");
		queryString.append("image_params_");
		queryString.append(calStartYear);
		queryString.append(calStartMonth);
		queryString.append(" ON image_params_");
		queryString.append(calStartYear);
		queryString.append(calStartMonth);
		queryString.append(".file_id = t2.file_id ");
		queryString
				.append("WHERE (x BETWEEN ? AND ? ) AND (y BETWEEN ? AND ?);");
		return queryString.toString();
	}
}