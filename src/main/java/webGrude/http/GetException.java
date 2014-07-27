package webGrude.http;


public class GetException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2629365861826320507L;

	public GetException(Exception e, String get) {
		super("Error while getting "+get,e);
	}

}
