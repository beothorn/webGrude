package webGrude.mapping.annotations;

import webGrude.OkHttpBrowser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class to be mapped from HTML.
 * <p>
 * The value is the URL that will be prepended to links. The value is optional.<br>
 * It's also possible to parameterize the URL using the tokens {0}, {1}, etc.
 * </p>
 *
 * <p>Example usage:</p>
 *
 * <pre>{@code
 * @Page
 * class Foo {
 *     // ...
 * }
 * }</pre>
 *
 * <pre>{@code
 * Webgrude webgrude = new Webgrude();
 * Foo foo = webgrude.map(contents, Foo.class, url);
 * }</pre>
 *
 * <p>Or:</p>
 *
 * <pre>{@code
 * @Page("http://www.example.com")
 * class Foo {
 *     // ...
 * }
 * }</pre>
 *
 * <p>With URL parameters:</p>
 *
 * <pre>{@code
 * @Page("http://www.example.com/{0}/foo={1}/bar")
 * class Foo {
 *     // ...
 * }
 * }</pre>
 *
 * <pre>{@code
 * OkHttpBrowser.get(Foo.class, "toReplace0", "toReplace1");
 * }</pre>
 *
 * <p>You can also define the URL during the browser open call:</p>
 *
 * <pre>{@code
 * @Page
 * class Foo {
 *     // ...
 * }
 *
 * OkHttpBrowser.get("http://www.example.com", Foo.class);
 * }</pre>
 *
 * @author beothorn
 * @see OkHttpBrowser#get(Class, String...)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Page {
    /**
     * The url to visit and prepend to links.
     * @return the url
     */
    String value() default "";
}