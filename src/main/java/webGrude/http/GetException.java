package webGrude.http;


public class GetException extends RuntimeException {

    public GetException(Exception e, String get) {
        super("Error while getting " + get, e);
    }

    public GetException(String error) {
        super("Error while getting " + error);
    }

}
