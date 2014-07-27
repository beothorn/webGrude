package webGrude;

public class TooManyResultsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3960224299384172905L;

	public TooManyResultsException(String cssQuery, int size) {
		super("The query '"+cssQuery+"' should return one result but returned "+size+". For more than one result a list should be used as the field type.");
	}

}
