package webGrude.mapping.annotations;

import webGrude.OkHttpBrowser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class to be mapped from html.
 * <p>
 * The value is the url that wil be prepended to links.
 * The Value is optional.<br>
 * It's also possible to parameterize the url using the tokens {0}, {1} ...
 * </p>
 * <p>
 * A class to map, the example home page would be annotated as such <br>
 * </p>
 * <pre>
 * {@code @Page
 * class Foo}
 * </pre>
 * <pre>
 * <pre>
 * {@code
 *  Webgrude webgrude = new Webgrude();
 *  Foo foo = webgrude.map(contents, Foo.class, url);}
 * </pre>
 * <p>Or:</p>
 * {@code @Page("http://www.example.com")}
 * </pre>
 * <p>
 * A class to load a url parameterized could be like this <br>
 * </p>
 * <pre>
 * {@code @Page("http://www.example.com/{0}/foo={1}/bar")
 * class Foo}
 * </pre>
 * <pre>
 * {@code OkHttpBrowser.get(Foo.class, "toReplace0", "toReplace1");}
 * </pre>
 * The url can be empty and defined on the Browser open call<br>
 * <pre>
 * {@code @Page
 * class Foo}
 * </pre>
 * <pre>
 * {@code OkHttpBrowser.get("http://www.example.com", Foo.class);}
 * </pre>
 *
 * @author beothorn
 * @see OkHttpBrowser#get(Class, String...)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Page {
    String value() default "";
}
