package webGrude.elements;

import org.jsoup.nodes.Element;

public class WrongTypeForField extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 3360356069227731575L;

    public WrongTypeForField(Element node, String attribute, @SuppressWarnings("rawtypes") Class c, Exception e) {
        super("Element can't be mapped to attribute " + attribute + " with type " + c.getTypeName() + "\n"
                + "Element contents:\n" + node.html(), e);
    }

}
