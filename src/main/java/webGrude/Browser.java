package webGrude;

import static webGrude.elements.Instantiator.instanceForNode;
import static webGrude.elements.Instantiator.typeIsKnown;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import webGrude.annotations.Page;
import webGrude.annotations.Selector;
import webGrude.elements.Instantiator;
import webGrude.elements.Link;
import webGrude.http.SimpleHttpClient;
import webGrude.http.SimpleHttpClientImpl;

import com.google.common.reflect.TypeToken;

public class Browser {

	private static final SimpleHttpClient webClient = new SimpleHttpClientImpl();
	private static String currentPageUrl;

	public static <T> T open(final Class<T> pageClass) {
		cryIfNotAnnotated(pageClass);
		try {
			final String pageUrl = pageClass.getAnnotation(Page.class).value();
			return loadPage(pageUrl, pageClass);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T open(final String pageUrl, final Class<T> pageClass) {
		cryIfNotAnnotated(pageClass);
		try {
			return loadPage(pageUrl, pageClass);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> void cryIfNotAnnotated(final Class<T> pageClass) {
		if (!pageClass.isAnnotationPresent(Page.class))
			throw new RuntimeException("Ops, you forgot to add the @" + Page.class.getSimpleName());
	}

	private static <T> T loadPage(final String pageUrl, final Class<T> pageClass) throws MalformedURLException, IOException, ClientProtocolException, InstantiationException, IllegalAccessException {
		Browser.currentPageUrl = pageUrl;
		final URL url = new URL(pageUrl);
		final String protocol = url.getProtocol();
		final Document parse;
		if (protocol.equals("file")) {
			parse = Jsoup.parse(new File(url.getPath()), "UTF-8");
		} else {
			final String page = webClient.get(pageUrl);
			parse = Jsoup.parse(page);
		}

		return loadDomContents(parse, pageClass);
	}

	private static <T> T loadDomContents(final Element node, final Class<T> classs) throws InstantiationException, IllegalAccessException {
		final T newInstance = classs.newInstance();

		if (classs.getAnnotation(Selector.class) == null && classs.getAnnotation(Page.class) == null)
			return newInstance;

		final Field[] declaredFields = classs.getDeclaredFields();
		for (final Field f : declaredFields) {
			final Class<?> fieldClass = f.getType();

			if (fieldClass.equals(java.util.List.class) && f.getAnnotation(Selector.class) == null) {
				solveListOfAnnotatedType(node, newInstance, f);
			}

			if (f.getAnnotation(Selector.class) != null) {
				solveAnnotatedField(node, newInstance, f, fieldClass);
			}

			if (fieldClass.getAnnotation(Selector.class) != null) {
				solveUnanotatedFieldOfAnnotatedType(node, newInstance, f, fieldClass);
			}

		}
		return newInstance;
	}

	private static <T> void solveUnanotatedFieldOfAnnotatedType(final Element node, final T newInstance, final Field f, final Class<?> fieldClass) throws IllegalAccessException, InstantiationException {
		final String cssQuery = fieldClass.getAnnotation(Selector.class).value();
		final Element selectedNode = getOnlyOneOrCry(node, cssQuery);
		final Document innerHtml = Jsoup.parse(selectedNode.html());
		f.set(newInstance, loadDomContents(innerHtml, fieldClass));
	}

	private static Element getOnlyOneOrCry(final Element node, final String cssQuery) {
		final Elements elements = node.select(cssQuery);
		final int size = elements.size();
		if(size != 1){
			throw new RuntimeException("The query '"+cssQuery+"' should return one result but returned "+size);
		}
		final Element selectedNode = elements.first();
		return selectedNode;
	}

	private static <T> void solveAnnotatedField(final Element node, final T newInstance, final Field f, final Class<?> fieldClass) throws IllegalAccessException, InstantiationException {
		if (fieldClass.equals(java.util.List.class)) {
			solveAnnotatedListField(node, newInstance, f);
		} else {
			solveAnnotatedFieldWithMappableType(node, newInstance, f, fieldClass);
		}
	}

	private static <T> void solveAnnotatedFieldWithMappableType(final Element node, final T newInstance, final Field f, final Class<?> fieldClass) throws IllegalAccessException {
		final Selector selectorAnnotation = f.getAnnotation(Selector.class);
		final String cssQuery = selectorAnnotation.value();
		final Element selectedNode = getOnlyOneOrCry(node, cssQuery);
		
		if (Instantiator.typeIsVisitable(fieldClass)) {
			final Class<?> visitableGenericClass = TypeToken.of(f.getGenericType()).resolveType(Link.class.getTypeParameters()[0]).getRawType();
			f.set(newInstance, Instantiator.visitableForNode(selectedNode, visitableGenericClass, Browser.currentPageUrl));
		}else{			
			if (typeIsKnown(fieldClass)) {
				f.set(newInstance, instanceForNode(selectedNode, fieldClass));
			} else {
				throw new RuntimeException("Can't convert html to class " + fieldClass.getName() + "\n" + "The Selector annotation should be on the class file, not on the field.");
			}
		}
	}

	private static <T> void solveAnnotatedListField(final Element node, final T newInstance, final Field f) throws IllegalAccessException, InstantiationException {
		final Type genericType = f.getGenericType();
		final Class<?> listClass = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
		final String cssQuery = f.getAnnotation(Selector.class).value();
		final Elements nodes = node.select(cssQuery);
		f.set(newInstance, populate(nodes, listClass));
	}

	private static <T> void solveListOfAnnotatedType(final Element node, final T newInstance, final Field f) throws IllegalAccessException, InstantiationException {
		final Type genericType = f.getGenericType();
		final Class<?> listClass = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];

		final Selector selectorAnnotation = listClass.getAnnotation(Selector.class);
		if (selectorAnnotation != null) {
			final String cssQuery = selectorAnnotation.value();
			final Elements nodes = node.select(cssQuery);
			f.set(newInstance, populate(nodes, listClass));
		}
	}

	private static <T> List<T> populate(final Elements nodes, final Class<T> classs) throws InstantiationException, IllegalAccessException {
		final ArrayList<T> newInstanceList = new ArrayList<T>();
		final Iterator<Element> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			final Element node = iterator.next();
			if (typeIsKnown(classs)) {
				newInstanceList.add(instanceForNode(node, classs));
			} else {
				newInstanceList.add(loadDomContents(node, classs));
			}
		}
		return newInstanceList;
	}

}
