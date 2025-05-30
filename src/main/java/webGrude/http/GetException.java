package webGrude.http;

/**
 * Exception thrown when an error occurs during an HTTP GET operation.
 */
public class GetException extends RuntimeException {

    /**
     * Constructs a new GetException with a specified cause and the resource that was being fetched.
     *
     * @param e the underlying exception that caused the failure
     * @param get the resource being fetched when the error occurred
     */
    public GetException(Exception e, String get) {
        super("Error while getting " + get, e);
    }

    /**
     * Constructs a new GetException with a message describing the resource that failed to be fetched.
     *
     * @param error description of the error or resource
     */
    public GetException(String error) {
        super("Error while getting " + error);
    }
}