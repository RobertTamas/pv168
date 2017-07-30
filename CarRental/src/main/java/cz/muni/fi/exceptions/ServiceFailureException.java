package cz.muni.fi.exceptions;

/**
 * Is used when manager cannot complete task.
 *
 * @author Robert Tamas
 */
public class ServiceFailureException extends RuntimeException {
    public ServiceFailureException(String s) {
        super(s);
    }

    public ServiceFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceFailureException(Throwable cause) {
        super(cause);
    }
}
