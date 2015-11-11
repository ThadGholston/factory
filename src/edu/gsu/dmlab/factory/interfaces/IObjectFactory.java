package edu.gsu.dmlab.factory.interfaces;

import java.util.Collection;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;

public interface IObjectFactory {
	public ITrack getTrack(Collection<IEvent> events);
}
