package edu.gsu.dmlab.indexes.interfaces;

import edu.gsu.dmlab.ObjectFactory;
import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.geometry.Rectangle2D;
import org.apache.commons.configuration.ConfigurationException;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thad on 10/11/15.
 */
public abstract class AbsMatIndexer<T extends IBaseDataType> implements IIndexer<T> {
    protected ArrayList<T>[][] searchSpace;
    protected ArrayList<T> objectList;
    protected int regionDivisor;
    protected int regionDimension;

    public AbsMatIndexer(ArrayList<T> objectList) throws ConfigurationException {
        this.regionDimension = ObjectFactory.getConfiguration().getInt("regionDimension");
        searchSpace = new ArrayList[regionDimension][regionDimension];
        sortList(objectList);
        this.objectList = objectList;
    }

    private void sortList(ArrayList<T> list) {
        list.sort((o1, o2) -> o2.compareTime(o2));
    }

    protected abstract void buildIndex();

    @Override
    public ArrayList<T> getAll(){
        return objectList;
    }

    @Override
    public DateTime getFirstTime() {
        return objectList.get(0).getTimePeriod().getStart();
    }

    @Override
    public DateTime getLastTime() {
        return objectList.get(objectList.size() - 1).getTimePeriod().getEnd();
    }

    @Override
    public abstract ArrayList<T> filterOnInterval(Interval timePeriod);
    /*{
        ArrayList<T> results = new ArrayList<>();
        for(T obj: (ArrayList<T>) objectList){
            if (obj.intersects(timePeriod)){
                results.add(obj);
            }
            if (obj.isAfter(timePeriod)){
                break;
            }
        }
        return results;
    }*/

    @Override
    public ArrayList<T> filterOnIntervalAndLocation(Interval timePeriod, Rectangle2D boundingBox){
        ConcurrentHashMap<UUID, T> results = new ConcurrentHashMap<>();
        for (int x = (int)boundingBox.getMinX(); x < (int)boundingBox.getMaxX(); x++){
            for (int y = (int)boundingBox.getMinY(); y < (int)boundingBox.getMaxY(); y++){
                for (T object: searchSpace[x][y]){
                    if (object.getTimePeriod().overlaps(timePeriod)){
                        results.put(object.getUUID(), object);
                    }
                }
            }
        }
        return (ArrayList<T>) results.values();
    }

}
