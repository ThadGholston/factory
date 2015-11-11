package edu.gsu.dmlab.tracking.interfaces;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;

public interface IObsModel {
	public double getObsProb(IEvent ev);
}
