package fr.uparis.beryllium.exceptions;

/**
 * Custom Exception used when we decide to 'quit'
 * in the terminal mode
 */
public class QuitException extends Exception {

    /**
     * Used when we 'quit' in the terminal mode
     */
    public QuitException() {
        super("Good bye !  :)");
    }

}
