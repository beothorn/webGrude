package webGrude.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a field to be mapped to a html element.
 * <p>
 * A field annotated with this will receive the value correspondin to it's css
 * selector when the webgrude Browser loads a page. This field <b>must</b> be in
 * a class annotated with <i>{@literal @}Page</i>.
 * The field type can be any class with the <i>{@literal @}Page</i> or one of
 * the following types (or its primitive):
 * <ul>
 * <li>String</li>
 * <li>Float</li>
 * <li>Integer</li>
 * <li>Boolean</li>
 * <li>webGrude.elements.Link</li>
 * <li>org.jsoup.nodes.Element</li>
 * </ul>
 * Or a List of any of these types.<br>
 * You can also set the field as an attribute value instead of it's text using
 * the attr annotation value. See the examples on how to do that.
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
 * @author beothorn
 * @see webGrude.annotations.Page
 * @see webGrude.elements.Link
 * @see webGrude.Browser#open(String, Class, String...)
 * @see webGrude.Browser#open(Class, String...)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Selector {
	String value();
    String attr() default "";
} 