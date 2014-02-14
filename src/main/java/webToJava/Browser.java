package webToJava;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import webToJava.annotations.PageURL;
import webToJava.annotations.Selector;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;

public class Browser {
	
	private static final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);

	public static <T> T open(final Class<T> annotatedClass) {
		try {
			return internalLoad(annotatedClass);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static List populate(final DomNode node, final Class classs) throws InstantiationException, IllegalAccessException{
		final ArrayList newInstanceList = new ArrayList();
		newInstanceList.add(classs.newInstance());
		return newInstanceList;
	}
	
	private static <T> T loadDomContents(final DomNode node, final Class<T> classs) throws InstantiationException, IllegalAccessException{
		final T newInstance = classs.newInstance();
		
		if(classs.getAnnotation(Selector.class) == null && classs.getAnnotation(PageURL.class) == null)
			return newInstance;
		
		final Field[] declaredFields = classs.getDeclaredFields();
		for (final Field f : declaredFields) {
			final Class<?> fieldClass = f.getType();
			
			if(fieldClass.equals(java.util.List.class)){
				final Type genericType = f.getGenericType();
				final Class<?> listClass = (Class<?>) ((ParameterizedType)genericType).getActualTypeArguments()[0];
				
				if(listClass.getAnnotation(Selector.class) != null){
					f.set(newInstance, populate(node, listClass));
				}
			}
			
			if(f.getAnnotation(Selector.class) != null){
				if(fieldClass.equals(java.util.List.class)){
					final Type genericType = f.getGenericType();
					final Class<?> listClass = (Class<?>) ((ParameterizedType)genericType).getActualTypeArguments()[0];
					
					f.set(newInstance, populate(node, listClass));
				}else{				
					f.set(newInstance, fieldClass.newInstance());
				}
			}
			
			if(fieldClass.getAnnotation(Selector.class) != null){							
				f.set(newInstance, loadDomContents(node, fieldClass));
			}
			
		}
		return newInstance;
	}
	
	private static <T> T internalLoad(final Class<T> annotatedClass)
			throws FailingHttpStatusCodeException, MalformedURLException,
			IOException, InstantiationException, IllegalAccessException {
		
		if (!annotatedClass.isAnnotationPresent(PageURL.class)) throw new RuntimeException("Ops, you forgot to add the @"+PageURL.class.getSimpleName());
		
		final String pageUrl = annotatedClass.getAnnotation(PageURL.class).value();
		final DomNode page = null;//webClient.getPage(pageUrl);		
		
		return loadDomContents(page, annotatedClass);
		
		/*
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
		*/
	}
	/*
	private static <T> T load(final Class<T> type, final DomNode page) throws InstantiationException, IllegalAccessException {
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
*/
}
