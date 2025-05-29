package webGrude.mapping.elements;

import org.jsoup.nodes.Element;
import webGrude.Webgrude;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
public class Instantiator {

    public static boolean typeIsKnown(final Class c) {
        return c.equals(String.class) ||
            c.equals(Integer.class) || c.getSimpleName().equals("int") ||
            c.equals(Float.class) || c.getSimpleName().equals("float") ||
            c.equals(Boolean.class) || c.getSimpleName().equals("boolean") ||
            c.equals(Link.class) ||
            c.equals(Element.class) ||
            c.equals(List.class) ||
            c.equals(Date.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T instanceForNode(
        final Element node,
        final FieldMapping fieldMapping,
        final Class<T> c
    ) {
        final String attribute = fieldMapping.attr();
        final String format = fieldMapping.format();
        final String locale = fieldMapping.locale();
        final String defValue = fieldMapping.defValue();

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

            if(!c.equals(Date.class) && format != null && !format.isEmpty()){
                final Pattern p = Pattern.compile(format);
                final Matcher matcher = p.matcher(value);
                final boolean found = matcher.find();
                if(found){
                    value = matcher.group(1);
                    if(value.isEmpty()){
                        value = defValue;
                    }
                }else{
                    value = defValue;
                }
            }

            if (c.equals(String.class)) {
                return (T) value;
            }

            if (c.equals(Date.class)) {
                Locale loc = getLocale(locale);
                final DateFormat df = new SimpleDateFormat(format, loc);
                return (T) df.parse(value);
            }

            if (c.equals(Integer.class) || c.getSimpleName().equals("int")) {
                return (T) Integer.valueOf(value);
            }

            if (c.equals(Float.class) || c.getSimpleName().equals("float")) {
                Locale loc = getLocale(locale);
                final NumberFormat nf = NumberFormat.getInstance(loc);
                Number number = nf.parse(value);
                return (T) Float.valueOf(number.floatValue());
            }

            if (c.equals(Boolean.class) || c.getSimpleName().equals("boolean")) {
                return (T) Boolean.valueOf(value);
            }
        } catch (final Exception e) {
            throw new WrongTypeForField(node, attribute, c, e);
        }

        return (T) value;
    }

    private static Locale getLocale(String locale) {
        Locale loc = Locale.getDefault();
        if(locale != null && !locale.isEmpty()){

            if (locale.contains("_")) {
                String[] parts = locale.split("_");
                loc = new Locale.Builder()
                        .setLanguage(parts[0])
                        .setRegion(parts[1])
                        .build();
            } else {
                loc = new Locale.Builder()
                        .setLanguage(locale)
                        .build();
            }
        }
        return loc;
    }

    public static boolean typeIsVisitable(final Class<?> fieldClass) {
        return fieldClass.equals(Link.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T visitableForNode(
            final Webgrude pageToClassMapper,
            final Element node,
            final Class c,
            final String currentPageUrl
    ) {
        return (T) new Link<T>(pageToClassMapper, node, c, currentPageUrl);
    }

}
