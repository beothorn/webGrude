package webToJava;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;

import webToJava.annotations.PageURL;
import webToJava.annotations.QuerySelector;
import webToJava.elements.Link;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class PageLoader {
	
	private static final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);

	public static <T> T load(final Class<T> annotatedClass) {
		try {
			return internalLoad(annotatedClass);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T internalLoad(final Class<T> annotatedClass)
			throws FailingHttpStatusCodeException, MalformedURLException,
			IOException, InstantiationException, IllegalAccessException {
		
		if (!annotatedClass.isAnnotationPresent(PageURL.class)) throw new RuntimeException("Ops, you forgot to add the @PageURL");
		final T newInstance = annotatedClass.newInstance();
		
		
		final String pageUrl = annotatedClass.getAnnotation(PageURL.class).value();
		final HtmlPage page = webClient.getPage(pageUrl);
		
		for (final Field f : annotatedClass.getDeclaredFields()) {
			if (f.isAnnotationPresent(QuerySelector.class)) {
				final Class<?> type = f.getType();
				if(type.equals(Link.class)){
					final String querySelector = f.getAnnotation(QuerySelector.class).value();					
					final DomNode linkElement = page.querySelector(querySelector);
					
					
					final Link link = new Link();
					link.href = linkElement.asText();
					
					f.set(newInstance, link);
				}
			}
		}
		return newInstance;
	}

}
