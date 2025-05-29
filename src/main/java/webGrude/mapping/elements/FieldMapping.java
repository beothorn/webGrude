package webGrude.mapping.elements;

import webGrude.mapping.annotations.Selector;
import webGrude.mapping.annotations.XPath;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

public class FieldMapping {
    private final String value;
    private final String attr;
    private final String format;
    private final String locale;
    private final String defValue;
    private final boolean isXpath;

    public FieldMapping(String value, String attr, String format, String locale, String defValue, boolean isXpath) {
        this.value = value;
        this.attr = attr;
        this.format = format;
        this.locale = locale;
        this.defValue = defValue;
        this.isXpath = isXpath;
    }

    public static FieldMapping from(Selector s) {
        return new FieldMapping(
            s.value(),
            s.attr(),
            s.format(),
            s.locale(),
            s.defValue(),
            false
        );
    }

    public static FieldMapping from(XPath x) {
        return new FieldMapping(
                x.value(),
                null,
                null,
                null,
                null,
                true
        );
    }

    public static Optional<FieldMapping> from(AnnotatedElement annotatedElement) {
        final Selector selector = annotatedElement.getAnnotation(Selector.class);
        if (selector != null) {
            return Optional.of(FieldMapping.from(selector));
        }
        final XPath xPath = annotatedElement.getAnnotation(XPath.class);
        if (xPath != null) {
            return Optional.of(FieldMapping.from(xPath));
        }
        return Optional.empty();
    }

    public String attr() {
        return attr;
    }

    public String format() {
        return format;
    }

    public String locale() {
        return locale;
    }

    public String defValue() {
        return defValue;
    }

    public String value() {
        return value;
    }

    public boolean useXpath() {
        return isXpath;
    }
}
