package edu.gsu.dmlab.tracking.interfaces;

import edu.gsu.dmlab.datatypes.interfaces.IEvent;

public interface ILocationProbCal {

	public double calcProb(IEvent ev);
}
