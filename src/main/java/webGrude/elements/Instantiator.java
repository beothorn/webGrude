package webGrude.elements;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

import webGrude.annotations.Selector;

@SuppressWarnings("rawtypes")
public class Instantiator {

    public static boolean typeIsKnown(final Class c) {
        return c.equals(String.class) ||
                c.equals(Integer.class) || c.getSimpleName().equals("int") ||
                c.equals(Float.class) || c.getSimpleName().equals("float") ||
                c.equals(Boolean.class) || c.getSimpleName().equals("boolean") ||
                c.equals(Link.class) || c.equals(Element.class) || c.equals(List.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T instanceForNode(
        final Element node,
        final String attribute,
        final String regex,
        final Class<T> c
    ) {
        String value;

        try {
            if (c.equals(Element.class)) {
                return (T) node;
            }

            if (attribute != null && !attribute.isEmpty()) {
                if (attribute.equals("html")) {
                    value = node.html();
                } else if (attribute.equals("outerHtml")) {
                    value = node.outerHtml();
                } else {
                    value = node.attr(attribute);
                }
            } else {
                value = node.text();
            }

            if(regex != null && !regex.equals(Selector.NOREGEX) ){
                final Pattern p = Pattern.compile(regex);
                final Matcher matcher = p.matcher(value);
                matcher.find();
                value = matcher.group(1);
            }

            if (c.equals(String.class)) {
                return (T) value;
            }

            if (c.equals(Integer.class) || c.getSimpleName().equals("int")) {
                return (T) Integer.valueOf(value);
            }

            if (c.equals(Float.class) || c.getSimpleName().equals("float")) {
                return (T) Float.valueOf(value);
            }

            if (c.equals(Boolean.class) || c.getSimpleName().equals("boolean")) {
                return (T) Boolean.valueOf(value);
            }

        } catch (final Exception e) {
            throw new WrongTypeForField(node, attribute, c, e);
        }

        return (T) value;
    }

    public static boolean typeIsVisitable(final Class<?> fieldClass) {
        return fieldClass.equals(Link.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T visitableForNode(final Element node, final Class c, final String currentPageUrl) {
        return (T) new Link<T>(node, c, currentPageUrl);
    }

}
