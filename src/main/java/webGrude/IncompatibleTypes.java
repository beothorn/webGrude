package webGrude;

import org.jsoup.nodes.Element;

import webGrude.annotations.Selector;

public class IncompatibleTypes extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 751653411036904421L;

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
            "\n}",e);
    }
}
