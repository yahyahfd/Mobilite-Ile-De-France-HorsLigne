package fr.uparis.beryllium.exceptions;
/**
 * Custom Exception used when the file doesn't follow
 * a specific format. Example: (Station1,Station2,Line)
*/
public class FormatException extends Exception {
    /**
     * Constructor for the FormatException class
     * @see Exception
     */
    public FormatException(String message) {
        super(message);
    }
}