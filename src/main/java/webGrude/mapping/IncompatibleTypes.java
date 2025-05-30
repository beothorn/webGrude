package webGrude.mapping;

import org.jsoup.nodes.Element;
import webGrude.mapping.annotations.Selector;

/**
 * Exception thrown when a selected node cannot be mapped to the expected field type,
 * indicating incompatible types between the HTML element and the annotated field.
 */
public class IncompatibleTypes extends Exception {

    /**
     * Constructs a new IncompatibleTypes exception with details about the mapping failure.
     *
     * @param newInstance the object instance being populated
     * @param selectedNode the HTML element selected during parsing
     * @param selectorAnnotation the annotation used to select the element
     * @param fieldClass the expected type of the field
     * @param e the underlying exception that caused the type incompatibility
     */
    public IncompatibleTypes(
            final Object newInstance,
            final Element selectedNode,
            final Selector selectorAnnotation,
            final Class<?> fieldClass,
            final Exception e
    ){
        super(
                "{\n"+
                        "\tInstance type: "+newInstance.getClass().getName()+
                        "\n\tField Type: "+fieldClass+
                        "\n\tNode: "+selectedNode+
                        "\n\tAnnotation: "+selectorAnnotation+
                        "\n}", e);
    }
}