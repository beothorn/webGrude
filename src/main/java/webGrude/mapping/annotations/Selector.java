package webGrude.mapping.annotations;

import webGrude.OkHttpBrowser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a field to be mapped using a css selector.
 * <p>
 * A field annotated with this will receive the value corresponding to it's css
 * selector when mapped.
 * The field can be any of the following types (or its primitive):
 * <ul>
 * <li>String</li>
 * <li>Float</li>
 * <li>Integer</li>
 * <li>Boolean</li>
 * <li>webGrude.mapping.elements.Link</li>
 * <li>org.jsoup.nodes.Element</li>
 * </ul>
 * Or a List of any of these types.<br>
 * You can also set the field as an attribute value instead of it's text using
 * the attr annotation value. html and outerHtml are also valid values for attr.
 * For example:
 * <p>
 * A field mapping a link with id foo
 * </p>
 * <pre>
 * {@code @Selector("#foo") String fooText;}
 * </pre>
 * <p>
 * A field mapping a link with id foo but receiving the href value
 * </p>
 * <pre>
 * {@code @Selector(value = "#foo", attr="href") String fooText;}
 * </pre>
 * <p>
 * A field mapping a link with id foo but receiving the href value
 * </p>
 * <pre>
 * {@code @Selector(value = "#foo", attr="href") String fooText;}
 * </pre>
 * <p>
 * A field mapping a the inner html code of a link with id foo
 * </p>
 * <pre>
 * {@code @Selector(value = "#foo", attr="html") String fooHtml;}
 * </pre>
 * <p>
 * A field mapping a the outer html code of a link with id foo
 * </p>
 * <pre>
 * {@code @Selector(value = "#foo", attr="outerHtml") String fooOuterHtml;}
 * </pre>
 *
 * @author beothorn
 * @see webGrude.mapping.annotations.Page
 * @see webGrude.mapping.elements.Link
 * @see OkHttpBrowser#get(Class, String...)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Selector {
    String value();
    String attr() default "";
    String format() default "";
    String locale() default "";
    String defValue() default "";
}
