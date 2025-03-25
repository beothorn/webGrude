package webGrude.mapping.annotations;

import webGrude.OkHttpBrowser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a method to be called after the Browser maps the value to the class.
 * <p>
 * When the webgrude Browser finishes loading the values from the Html to the Page class fields this
 * method is called. If there is any values that need to be post processed this should be possible
 * by anottating a method with this annotation.
 *
 * @author beothorn
 * @see OkHttpBrowser#get(Class, String...)
 * @see OkHttpBrowser#get(String, Class, String...)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterPageLoad {
}
