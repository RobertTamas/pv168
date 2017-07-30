package cz.muni.fi.exceptions;

/**
 * Is used when entity is not valid.
 *
 * @author Robert Tamas
 */
public class IllegalEntityException extends RuntimeException {
    public IllegalEntityException() {
    }

    public IllegalEntityException(String s) {
        super(s);
    }

    public IllegalEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
