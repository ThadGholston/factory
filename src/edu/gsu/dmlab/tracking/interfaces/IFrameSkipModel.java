package edu.gsu.dmlab.tracking.interfaces;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;

public interface IFrameSkipModel {
	public double getSkipProb(ITrack leftTrack, ITrack rightTrack);
}
