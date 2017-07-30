package cz.muni.fi.exceptions;

/**
 * Is used when parameters of entities are not valid.
 *
 * @author Robert Tamas
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String s) {
        super(s);
    }
}
