package webGrude.mapping.elements;

import org.jsoup.nodes.Element;

/**
 * Thrown when an HTML element cannot be mapped to the expected Java field type.
 * <p>
 * This typically indicates that the data in the element cannot be parsed or converted
 * into the specified field type (e.g., trying to parse text as a float).
 */
public class WrongTypeForField extends RuntimeException {

    /**
     * Constructs a new WrongTypeForField exception with context information.
     *
     * @param node      the HTML element that failed to be mapped
     * @param attribute the attribute or value source being mapped (e.g., "text", "href", "html")
     * @param c         the expected Java class type
     * @param e         the underlying exception that caused the failure
     */
    public WrongTypeForField(final Element node, final String attribute, @SuppressWarnings("rawtypes") final Class c, final Exception e) {
        super("Element can't be mapped to attribute " + attribute + " with type " + c.getTypeName() + "\n"
                + "Element contents:\n" + node.html(), e);
    }
}
