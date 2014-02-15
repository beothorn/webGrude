package webGrude;

import static webGrude.elements.Instantiator.instanceForNode;
import static webGrude.elements.Instantiator.typeIsKnown;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import webGrude.annotations.PageURL;
import webGrude.annotations.Selector;
import webGrude.http.SimpleHttpClient;
import webGrude.http.SimpleHttpClientImpl;

public class Browser {
	
	private static final SimpleHttpClient webClient = new SimpleHttpClientImpl();

	public static <T> T open(final Class<T> annotatedClass) {
		try {
			return internalLoad(annotatedClass);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T internalLoad(final Class<T> annotatedClass) throws ClientProtocolException, IOException, InstantiationException, IllegalAccessException{
		
		if (!annotatedClass.isAnnotationPresent(PageURL.class)) throw new RuntimeException("Ops, you forgot to add the @"+PageURL.class.getSimpleName());
		
		final String pageUrl = annotatedClass.getAnnotation(PageURL.class).value();
		final String page = webClient.get(pageUrl);	
		final Document parse = Jsoup.parse(page);
		
		return loadDomContents(parse, annotatedClass);
	}

	private static <T> List<T> populate(final Elements nodes, final Class<T> classs) throws InstantiationException, IllegalAccessException{
		final ArrayList<T> newInstanceList = new ArrayList<T>();
		final Iterator<Element> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			final Element node = iterator.next();
			if(typeIsKnown(classs)){
				newInstanceList.add(instanceForNode(node,classs));
			}else{
				newInstanceList.add(loadDomContents(node, classs));
			}
		}
		return newInstanceList;
	}
	
	private static <T> T loadDomContents(final Element node, final Class<T> classs) throws InstantiationException, IllegalAccessException{
		final T newInstance = classs.newInstance();
		
		if(classs.getAnnotation(Selector.class) == null && classs.getAnnotation(PageURL.class) == null)
			return newInstance;
		
		final Field[] declaredFields = classs.getDeclaredFields();
		for (final Field f : declaredFields) {
			final Class<?> fieldClass = f.getType();
			
			if(fieldClass.equals(java.util.List.class) && f.getAnnotation(Selector.class) == null){
				final Type genericType = f.getGenericType();
				final Class<?> listClass = (Class<?>) ((ParameterizedType)genericType).getActualTypeArguments()[0];
				
				if(listClass.getAnnotation(Selector.class) != null){
					final Elements nodes = node.select(listClass.getAnnotation(Selector.class).value());
					f.set(newInstance, populate(nodes, listClass));
				}
			}
			
			if(f.getAnnotation(Selector.class) != null){
				if(fieldClass.equals(java.util.List.class)){
					final Type genericType = f.getGenericType();
					final Class<?> listClass = (Class<?>) ((ParameterizedType)genericType).getActualTypeArguments()[0];
					final Elements nodes = node.select(f.getAnnotation(Selector.class).value());
					f.set(newInstance, populate(nodes, listClass));
				}else{
					final Element first = node.select(f.getAnnotation(Selector.class).value()).first();
					if(typeIsKnown(fieldClass)){
						f.set(newInstance, instanceForNode(first,fieldClass));
					}else{
						throw new RuntimeException(
								"Can't convert html to class "+fieldClass.getName()+"\n"
								+ "The Selector annotation should be on the class file, not on the field.");
					}
				}
			}
			
			if(fieldClass.getAnnotation(Selector.class) != null){							
				f.set(newInstance, loadDomContents(node, fieldClass));
			}
			
		}
		return newInstance;
	}
	
}
