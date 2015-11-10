package edu.gsu.dmlab.tracking.interfaces;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;

public interface IAppearanceModel {
	public double calcProbAppearance(ITrack leftTrack, ITrack rightTrack);
}
