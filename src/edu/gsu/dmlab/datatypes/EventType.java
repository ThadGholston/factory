package edu.gsu.dmlab.datatypes;

/**
 * Created by thad on 10/24/15.
 */
public enum EventType {
    ACTIVE_REGION(0), CORONAL_HOLE(1), FILAMENT(2), SIGMOID(3), SUNSPOT(4), EMERGING_FLUX(5), FLARE(6);
    private final int id;
    EventType(int id) { this.id = id; }
    public int getValue() { return id; }
}
