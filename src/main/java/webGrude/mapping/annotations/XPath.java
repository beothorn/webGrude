package webGrude.mapping.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a field should be mapped using an XPath expression instead of a CSS selector.
 * <p>
 * This annotation can be used in combination with {@code @Selector} to enable XPath-based node selection.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface XPath {

    /**
     * The XPath expression to use for selecting the element(s).
     *
     * @return the XPath query string
     */
    String value() default "";
}