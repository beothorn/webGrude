package webGrude.mapping.elements;

import webGrude.mapping.annotations.Selector;
import webGrude.mapping.annotations.XPath;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

/**
 * Represents a mapping configuration for a field extracted from HTML or XML using annotations.
 */
public class FieldMapping {
    private final String value;
    private final String attr;
    private final String format;
    private final String locale;
    private final String defValue;
    private final boolean isXpath;

    /**
     * Constructs a FieldMapping instance.
     *
     * @param value     the selector or XPath value
     * @param attr      the attribute to extract
     * @param format    the format string to apply
     * @param locale    the locale used in formatting
     * @param defValue  the default value if no match is found
     * @param isXpath   true if XPath is used instead of a CSS selector
     */
    public FieldMapping(String value, String attr, String format, String locale, String defValue, boolean isXpath) {
        this.value = value;
        this.attr = attr;
        this.format = format;
        this.locale = locale;
        this.defValue = defValue;
        this.isXpath = isXpath;
    }

    /**
     * Creates a FieldMapping from a {@link Selector} annotation.
     *
     * @param s the Selector annotation
     * @return a new FieldMapping instance
     */
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

    /**
     * Creates a FieldMapping from a {@link XPath} annotation.
     *
     * @param x the XPath annotation
     * @return a new FieldMapping instance
     */
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

    /**
     * Attempts to create a FieldMapping from annotations present on an element.
     *
     * @param annotatedElement the element to inspect
     * @return an Optional containing the FieldMapping if found
     */
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

    /**
     * Checks if an element has a supported mapping annotation.
     *
     * @param annotatedElement the element to check
     * @return true if either Selector or XPath is present
     */
    public static boolean hasMappingAnnotation(AnnotatedElement annotatedElement) {
        final Selector selector = annotatedElement.getAnnotation(Selector.class);
        if (selector != null) {
            return true;
        }
        final XPath xPath = annotatedElement.getAnnotation(XPath.class);
        return xPath != null;
    }

    /**
     * @return the attribute name to extract
     */
    public String attr() {
        return attr;
    }

    /**
     * @return the format string to apply
     */
    public String format() {
        return format;
    }

    /**
     * @return the locale string used for formatting
     */
    public String locale() {
        return locale;
    }

    /**
     * @return the default value if no value is found
     */
    public String defValue() {
        return defValue;
    }

    /**
     * @return the selector or XPath value
     */
    public String value() {
        return value;
    }

    /**
     * @return true if XPath is used instead of a CSS selector
     */
    public boolean useXpath() {
        return isXpath;
    }
}