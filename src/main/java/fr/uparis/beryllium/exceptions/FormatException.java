package fr.uparis.beryllium.exceptions;

/**
 * Custom Exception used when the file doesn't follow
 * a specific format. Example: (Station1,Station2,Line)
 */
public class FormatException extends Exception {

    /**
     * Used when a file doesn't follow an intended format
     * 
     * @param message the message to print out
     */
    public FormatException(String message) {
        super(message);
    }
}
