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
import java.net.URLEncoder;
import java.text.MessageFormat;
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

    private static SimpleHttpClient webClient;
    private static String currentPageUrl;

	public static <T> T open(final Class<T> pageClass,final String... params) {
		cryIfNotAnnotated(pageClass);
		try {
			final String pageUrl = pageClass.getAnnotation(Page.class).value();
			return loadPage(pageUrl, pageClass, params);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

    public static String getCurentUrl() {
        return currentPageUrl;
    }

    public static void setWebClient(final SimpleHttpClient client) {
        webClient = client;
    }

    public static <T> T open(final String pageUrl, final Class<T> pageClass, final String... params) {
		cryIfNotAnnotated(pageClass);
		try {
			return loadPage(pageUrl, pageClass, params);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> void cryIfNotAnnotated(final Class<T> pageClass) {
		if (!pageClass.isAnnotationPresent(Page.class))
			throw new RuntimeException("Ops, you forgot to add the @" + Page.class.getSimpleName());
	}

    private static String loadPage(String pageUrl, final String... params){
        if(webClient == null)
            setWebClient(new SimpleHttpClientImpl());
        try {
            return webClient.get(pageUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	private static <T> T loadPage(final String pageUrl, final Class<T> pageClass, final String... params) throws MalformedURLException, IOException, ClientProtocolException,  IllegalAccessException {

        String[] formattedParams = new String[params.length];

        for(int i = 0 ; i < params.length; i++){
            formattedParams[i] = URLEncoder.encode(params[i],"UTF-8");
        }

        Browser.currentPageUrl = MessageFormat.format(pageUrl, formattedParams);

		final URL url = new URL(Browser.currentPageUrl);
		final String protocol = url.getProtocol();
		final Document parse;
		if (protocol.equals("file")) {
			parse = Jsoup.parse(new File(url.getPath()), "UTF-8");
		} else {
			final String page = loadPage(Browser.currentPageUrl);
			parse = Jsoup.parse(page);
		}

        try {
		    return loadDomContents(parse, pageClass);
        }catch(InstantiationException e){
            throw new RuntimeException("Maybe you forgot to write your internal class as 'public static'?", e);
        }
	}

	private static <T> T loadDomContents(final Element node, final Class<T> classs) throws IllegalAccessException, InstantiationException {

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
		final Element selectedNode = getFirstOrNullOrCryIfMoreThanOne(node, cssQuery);
        if(selectedNode == null) return;
		final Document innerHtml = Jsoup.parse(selectedNode.html());
		f.set(newInstance, loadDomContents(innerHtml, fieldClass));
	}

	private static Element getFirstOrNullOrCryIfMoreThanOne(final Element node, final String cssQuery) {
		final Elements elements = node.select(cssQuery);
		final int size = elements.size();
		if(size > 1){
			throw new RuntimeException("The query '"+cssQuery+"' should return one result but returned "+size+". For more than one result a list should be used as the field type.");
		}
        if(size == 0){
            return null;
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
		final Element selectedNode = getFirstOrNullOrCryIfMoreThanOne(node, cssQuery);
        if(selectedNode == null) return;

        if (Instantiator.typeIsVisitable(fieldClass)) {
            final Class<?> visitableGenericClass = TypeToken.of(f.getGenericType()).resolveType(Link.class.getTypeParameters()[0]).getRawType();
            f.set(newInstance, Instantiator.visitableForNode(selectedNode, visitableGenericClass, Browser.currentPageUrl));
        }else{
            if (typeIsKnown(fieldClass)) {
                final String attribute = selectorAnnotation.attr();
                f.set(newInstance, instanceForNode(selectedNode, attribute, fieldClass));
			} else {
				throw new RuntimeException("Can't convert html to class " + fieldClass.getName() + "\n" +
                        "The field type must be a class with "+Page.class.getSimpleName()+" annotation or one of these types:\n" +
                        List.class.getCanonicalName()+"\n"+
                        String.class.getCanonicalName()+"\n"+
                        Integer.class.getCanonicalName()+"\n"+
                        Float.class.getCanonicalName()+"\n"+
                        Boolean.class.getCanonicalName()+"\n"+
                        Link.class.getCanonicalName()+"\n"+
                        Element.class.getCanonicalName()+"\n"
                );
			}
		}
	}

	private static <T> void solveAnnotatedListField(final Element node, final T newInstance, final Field f) throws IllegalAccessException, InstantiationException {
		final Type genericType = f.getGenericType();
        final String cssQuery = f.getAnnotation(Selector.class).value();
        final String attribute = f.getAnnotation(Selector.class).attr();
        final Elements nodes = node.select(cssQuery);

        Type type = ((ParameterizedType) genericType).getActualTypeArguments()[0];
        if(type instanceof ParameterizedType){
            f.set(newInstance, populateListOfLinks(nodes, attribute, (ParameterizedType)type));
        }else{
            final Class<?> listClass = (Class<?>) type;
            f.set(newInstance, populateList(nodes, attribute, listClass));
        }
	}

	private static <T> void solveListOfAnnotatedType(final Element node, final T newInstance, final Field f) throws IllegalAccessException, InstantiationException {
		final Type genericType = f.getGenericType();
		final Class<?> listClass = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];

		final Selector selectorAnnotation = listClass.getAnnotation(Selector.class);
		if (selectorAnnotation != null) {
			final String cssQuery = selectorAnnotation.value();
            final String attribute = selectorAnnotation.attr();
			final Elements nodes = node.select(cssQuery);
			f.set(newInstance, populateList(nodes, attribute, listClass));
		}
	}

	private static <T> List<T> populateList(final Elements nodes, String attribute, final Class<T> classs) throws InstantiationException, IllegalAccessException {
		final ArrayList<T> newInstanceList = new ArrayList<T>();
		final Iterator<Element> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			final Element node = iterator.next();
			if (typeIsKnown(classs)) {
				newInstanceList.add(instanceForNode(node,attribute, classs));
			} else {
				newInstanceList.add(loadDomContents(node, classs));
			}
		}
		return newInstanceList;
	}

    private static <T> ArrayList<Link<T>> populateListOfLinks(final Elements nodes, String attribute, final ParameterizedType paraType) throws InstantiationException, IllegalAccessException {
        final ArrayList<Link<T>> newInstanceList = new ArrayList<Link<T>>();
        final Iterator<Element> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            final Element node = iterator.next();
            Class<?> classs = (Class<?>) paraType.getActualTypeArguments()[0];
            Link<T> link = (Link<T>) Instantiator.visitableForNode(node, classs, Browser.currentPageUrl);
            newInstanceList.add(link);
        }
        return newInstanceList;
    }

}
