package edu.gsu.dmlab.stages.interfaces;

import java.util.ArrayList;

import edu.gsu.dmlab.datatypes.interfaces.ITrack;

public interface IProcessingStage {
	ArrayList<ITrack> process();
}
