package edu.gsu.dmlab.tracking.interfaces;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;

public interface IMotionModel {
	public double calcProbMotion(ITrack leftTrack, ITrack rightTrack);
}
