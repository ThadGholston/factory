package edu.gsu.dmlab.factory.interfaces;

import java.util.ArrayList;
import java.util.concurrent.RecursiveAction;

import edu.gsu.dmlab.datatypes.interfaces.IBaseDataType;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.indexes.interfaces.IEventIndexer;
import edu.gsu.dmlab.indexes.interfaces.ITrackIndexer;

public interface IIndexFactory {
	
	public IEventIndexer getEventIndexer(ArrayList<IEvent> regionalList);
	public ITrackIndexer getTrackIndexer(ArrayList<IEvent> regionalList);
	public RecursiveAction getBaseObjectAreaSort(ArrayList<IBaseDataType>[][] area, int startX,
			int startY, int sizeX, int sizeY);
	

}
