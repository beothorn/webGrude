package webGrude.http;

/**
 * An interface for visiting links and retrieving their content.
 * <p>
 * Implementations of this interface define how to fetch the content of a given URL,
 * it can be an http fetch or a custom way.
 */
public interface LinkVisitor {

    /**
     * Visits the provided URL and returns its contents as a string.
     *
     * @param href the URL to visit
     * @return the contents of the page at the given URL
     */
    String visitLink(final String href);
}