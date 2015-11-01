package edu.gsu.dmlab.indexes.interfaces;

import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.geometry.Rectangle2D;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thad on 10/11/15. Edited by Dustin Kempton on 10/27/15
 */
public abstract class AbsMatIndexer<T extends IBaseDataType> {
	protected ArrayList<IBaseDataType>[][] searchSpace;
	protected ArrayList<T> objectList;
	protected int regionDivisor;
	protected int regionDimension;

	@SuppressWarnings("unchecked")
	public AbsMatIndexer(ArrayList<T> objectList, int regionDimension,
			int regionDivisor) throws IllegalArgumentException {
		if (objectList == null)
			throw new IllegalArgumentException("Object List cannot be null");
		if (regionDimension < 1)
			throw new IllegalArgumentException(
					"Region Dimension cannot be less than 1");
		if (regionDivisor < 1)
			throw new IllegalArgumentException(
					"Region Divisor cannot be less than 1");

		this.regionDimension = regionDimension;
		this.regionDivisor = regionDivisor;
		this.searchSpace = new ArrayList[regionDimension][regionDimension];
		for (int x = 0; x < this.regionDimension; x++) {
			for (int y = 0; y < this.regionDimension; y++) {
				this.searchSpace[x][y] = new ArrayList<IBaseDataType>();
			}
		}
		this.sortList(objectList);
		this.objectList = objectList;
	}

	private void sortList(ArrayList<T> list) {
		list.sort((o1, o2) -> o2.compareTime(o2));
	}

	protected abstract void buildIndex();

	public ArrayList<T> getAll() {
		return objectList;
	}

	public DateTime getFirstTime() {
		return objectList.get(0).getTimePeriod().getStart();
	}

	public DateTime getLastTime() {
		return objectList.get(objectList.size() - 1).getTimePeriod().getEnd();
	}

	public abstract ArrayList<T> filterOnInterval(Interval timePeriod);

	/*
	 * { ArrayList<T> results = new ArrayList<>(); for(T obj: (ArrayList<T>)
	 * objectList){ if (obj.intersects(timePeriod)){ results.add(obj); } if
	 * (obj.isAfter(timePeriod)){ break; } } return results; }
	 */

	@SuppressWarnings("unchecked")
	public ArrayList<T> filterOnIntervalAndLocation(Interval timePeriod,
			Rectangle2D boundingBox) {
		ConcurrentHashMap<UUID, IBaseDataType> results = new ConcurrentHashMap<>();
		for (int x = (int) boundingBox.getMinX(); x < (int) boundingBox
				.getMaxX(); x++) {
			for (int y = (int) boundingBox.getMinY(); y < (int) boundingBox
					.getMaxY(); y++) {
				for (IBaseDataType object : searchSpace[x][y]) {
					if (object.getTimePeriod().overlaps(timePeriod)) {
						results.put(object.getUUID(), object);
					}
				}
			}
		}

		Collection<IBaseDataType> coll = results.values();
		ArrayList<IBaseDataType> list = new ArrayList<IBaseDataType>();
		list.addAll(0, coll);
		return (ArrayList<T>) list;
	}

}
