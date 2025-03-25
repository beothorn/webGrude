package webGrude.mapping;

public class TooManyResultsException extends RuntimeException {

    public TooManyResultsException(final String cssQuery, final int size) {
        super("The query '" + cssQuery + "' should return one result but returned " + size + ". For more than one result a list should be used as the field type.");
    }

}
