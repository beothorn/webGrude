package webGrude.mapping.annotations;

import webGrude.OkHttpBrowser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a field to be mapped using a CSS selector.
 * <p>
 * A field annotated with this will receive the value corresponding to its CSS
 * selector when mapped.
 * The field can be one of the following types (or its primitive counterpart):
 * <ul>
 *   <li>String</li>
 *   <li>Float</li>
 *   <li>Integer</li>
 *   <li>Boolean</li>
 *   <li>{@code webGrude.mapping.elements.Link}</li>
 *   <li>{@code org.jsoup.nodes.Element}</li>
 * </ul>
 * Or a {@code List} of any of these types.
 * <p>
 * You can also extract an attribute's value instead of the element's text using
 * the {@code attr} attribute. Special values for {@code attr} include:
 * {@code "html"} (inner HTML) and {@code "outerHtml"} (entire HTML of the element).
 *
 * @author beothorn
 * @see webGrude.mapping.annotations.Page
 * @see webGrude.mapping.elements.Link
 * @see OkHttpBrowser#get(Class, String...)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Selector {

    /**
     * The CSS selector used to locate the element(s).
     *
     * @return the CSS selector string
     */
    String value();

    /**
     * The attribute to extract from the selected element.
     * If empty, the text content is used.
     * Special values include "html" and "outerHtml".
     *
     * @return the attribute name or empty for text content
     */
    String attr() default "";

    /**
     * A formatting pattern (e.g., for dates or numbers) to apply when mapping the value.
     *
     * @return the format string
     */
    String format() default "";

    /**
     * The locale to use for formatting and parsing.
     * Should be in the form of a language tag, e.g., "en-US".
     *
     * @return the locale string
     */
    String locale() default "";

    /**
     * The default value to assign to the field if the selector does not match anything.
     *
     * @return the default value
     */
    String defValue() default "";
}
