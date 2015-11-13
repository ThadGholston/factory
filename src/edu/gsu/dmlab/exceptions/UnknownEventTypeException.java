package edu.gsu.dmlab.exceptions;

/**
 * Created by thad on 10/24/15.
 */
@SuppressWarnings("serial")
public class UnknownEventTypeException extends Exception {
    public UnknownEventTypeException(String s) {
        super(s);
    }
}
