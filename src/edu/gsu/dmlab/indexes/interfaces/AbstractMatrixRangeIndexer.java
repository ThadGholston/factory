package edu.gsu.dmlab.indexes.interfaces;

import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;

/**
 * Created by thad on 10/11/15.
 */
public class AbstractMatrixRangeIndexer<T extends IBaseDataType> implements IIndexer<T> {

    protected ArrayList<T> list;
    protected TList[][] matrix;
    public double regionDivisor;

    public AbstractMatrixRangeIndexer(ArrayList<T> list) {
        sortList(list);
        this.list = list;
        index();
    }

    private void sortList(ArrayList<T> list) {
        list.sort((o1, o2) -> o2.compareTime(o2));
    }

    protected void index() {
        throw new NotImplementedException();
    }

    @Override
    public ArrayList<T> getBetween(DateTime start, DateTime end) {
        ArrayList<T> out = new ArrayList<>();
        Interval interval = new Interval(start, end);
        for (T t: list){
            if (t.getTimePeriod().overlaps(interval)){
                out.add(t);
            }
        }
        return out;
    }

    @Override
    public ArrayList<T> getBetween(DateTime start, DateTime end, Point2D[] searchArea) {
        ArrayList<T> searchResults = new ArrayList<>();

        return searchResults;
    }

    @Override
    public DateTime getFirstTime() {
        return list.get(0).getTimePeriod().getStart();
    }

    @Override
    public DateTime getLastTime() {
        return list.get(list.size() - 1).getTimePeriod().getEnd();
    }



    protected class TList extends ArrayList<T> {
    }

}
