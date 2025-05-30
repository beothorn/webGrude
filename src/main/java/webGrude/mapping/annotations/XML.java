package webGrude.mapping.annotations;

import webGrude.OkHttpBrowser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class to be mapped from XML.
 * <p>
 * A class to map. <br>
 * </p>
 * <pre>
 * {@code @XML
 * class Foo}
 * </pre>
 * <pre>
 * {@code
 *  Webgrude webgrude = new Webgrude();
 *  Foo foo = webgrude.map(contents, Foo.class);}
 * </pre>
 * @author beothorn
 * @see OkHttpBrowser#get(Class, String...)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XML {
}
