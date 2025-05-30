package webGrude.mapping;

import org.jsoup.select.Elements;

/**
 * Thrown when a CSS selector matches more than one element but the corresponding field
 * is not a collection type.
 * <p>
 * This exception is intended to alert the developer that a single-value field (e.g., {@code String})
 * is annotated with a selector that yields multiple elements in the HTML document.
 * In such cases, the field should be defined as a {@code List} of the appropriate type.
 */
public class TooManyResultsException extends RuntimeException {

    /**
     * Constructs a new TooManyResultsException with details about the selector and matched elements.
     *
     * @param cssQuery the CSS selector used to find the elements
     * @param size     the number of elements matched
     * @param elements the matched elements
     */
    public TooManyResultsException(
            final String cssQuery,
            final int size,
            final Elements elements
    ) {
        super("The query '" + cssQuery + "' should return one result but returned "
                + size + ". For more than one result a list should be used as the field type."
                + elements);
    }
}