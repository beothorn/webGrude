package webToJava;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;

import webToJava.annotations.PageURL;
import webToJava.annotations.Selector;
import webToJava.elements.Link;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Browser {
	
	private static final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);

	public static <T> T open(final Class<T> annotatedClass) {
		try {
			return internalLoad(annotatedClass);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T internalLoad(final Class<T> annotatedClass)
			throws FailingHttpStatusCodeException, MalformedURLException,
			IOException, InstantiationException, IllegalAccessException {
		
		if (!annotatedClass.isAnnotationPresent(PageURL.class)) throw new RuntimeException("Ops, you forgot to add the @"+PageURL.class.getSimpleName());
		
		final String pageUrl = annotatedClass.getAnnotation(PageURL.class).value();
		final HtmlPage page = webClient.getPage(pageUrl);
		
		
		final Field[] declaredFields = annotatedClass.getDeclaredFields();
		final T newInstance = annotatedClass.newInstance();
		for (final Field f : declaredFields) {
			final Class<?> type = f.getType();
			if (f.isAnnotationPresent(Selector.class)) {
				if(type.equals(Link.class)){
					final String querySelector = f.getAnnotation(Selector.class).value();					
					final DomNode linkElement = page.querySelector(querySelector);
					
					
					final Link link = new Link();
					link.href = linkElement.asText();
					
					f.set(newInstance, link);
				}
			}else{
				f.set(newInstance, load(type, page));
			}
		}
		return newInstance;
	}

	private static <T> T load(final Class<T> type, final HtmlPage page) throws InstantiationException, IllegalAccessException {
		if (!type.isAnnotationPresent(Selector.class)) return null;
		final String selector = type.getAnnotation(Selector.class).value();
		final DomNode querySelector = page.querySelector(selector);
		
		final Field[] declaredFields = type.getDeclaredFields();
		final T newInstance = type.newInstance();
		for (final Field f : declaredFields) {
			final Class<?> ftype = f.getType();
			if (f.isAnnotationPresent(Selector.class)) {
			}else{
				if(ftype.equals(java.util.List.class)){
					final Type genericType = f.getGenericType();
					final Class<?> listClass = (Class<?>) ((ParameterizedType)genericType).getActualTypeArguments()[0];
					final String fquerySelector = listClass.getAnnotation(Selector.class).value();					
					final DomNode domNode = querySelector.querySelectorAll(fquerySelector).get(0);
					System.out.println(domNode.asText());
					
					
				}
			}
		}
		
		
		return null;
	}

}
