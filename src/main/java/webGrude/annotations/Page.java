package webGrude.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class to be mapped to a html.
 * <p>
 * A class with this annotation can be opened by the  webgrude Browser.
 * It's value is supposed to be the url that will be mapped to this class. 
 * The Value is optional, it's possible to load other url using the Browser 
 * with this same class.<br>
 * It's also possible to parameterize the url using the tokens {0}, {2}, ...
 * </p>
 * <p>
 * A class to map the example home page would be annotated as such <br>
 * </p>
 * <pre>
 * {@code @Page("http://www.example.com");};
 * </pre>
 * <p>
 * A class to load a url parameterized could be like this <br>
 * </p>
 * <pre>
 * {@code @Page("http://www.example.com/{0}/foo={1}/bar");};
 * </pre>
 * The url can be empty and defined on the Browser open call<br>
 * <pre>
 * {@code @Page};
 * </pre>
 * @author beothorn
 * @see webGrude.Browser#open(Class, String...)
 * @see webGrude.Browser#open(String, Class, String...)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Page {
	String value() default "";
} 