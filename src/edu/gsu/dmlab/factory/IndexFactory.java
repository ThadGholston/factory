/**
 * 
 */
package edu.gsu.dmlab.factory;

import java.util.ArrayList;
import java.util.concurrent.RecursiveAction;

import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.factory.interfaces.IIndexFactory;
import edu.gsu.dmlab.indexes.ForkAreaSort;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;

/**
 * @author Dustin
 *
 */
public class IndexFactory implements IIndexFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gsu.dmlab.factory.interfaces.IIndexFactory#getEventIndexer(java.util
	 * .ArrayList)
	 */
	@Override
	public IEventIndexer getEventIndexer(ArrayList<IEvent> regionalList) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gsu.dmlab.factory.interfaces.IIndexFactory#getTrackIndexer(java.util
	 * .ArrayList)
	 */
	@Override
	public ITrackIndexer getTrackIndexer(ArrayList<IEvent> regionalList) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.gsu.dmlab.factory.interfaces.IIndexFactory#getBaseObjectAreaSort(
	 * java.util.ArrayList[][], int, int, int)
	 */
	@Override
	public RecursiveAction getBaseObjectAreaSort(
			ArrayList<IBaseDataType>[][] area, int startX, int startY, int size) {
		return new ForkAreaSort(area, startX, startY, size);
	}

}
