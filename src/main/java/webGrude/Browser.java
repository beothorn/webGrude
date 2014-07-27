package webGrude;

import static webGrude.elements.Instantiator.instanceForNode;
import static webGrude.elements.Instantiator.typeIsKnown;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import webGrude.annotations.AfterPageLoad;
import webGrude.annotations.Page;
import webGrude.annotations.Selector;
import webGrude.elements.Instantiator;
import webGrude.elements.Link;
import webGrude.elements.WrongTypeForField;
import webGrude.http.BrowserClient;
import webGrude.http.GetException;
import webGrude.http.SimpleHttpClientImpl;

import com.google.common.reflect.TypeToken;

/**
 * Instantiate a class with Page annotations from a web page or a html file.
 * <p>
 * Examples:
 * </p>
 * To load a class 
 * that is annotated as <i>{@literal @}Page("http://www.example.com")</i><br>
 * <pre>
 * {@code ExamplePage example = Browser.get(ExamplePage.class);};
 * </pre>
 * <br>
 * To load a class 
 * that is annotated as <i>{@literal @}Page</i> with another url<br>
 * <pre>
 * {@code ExamplePage example = Browser.get("www.foo.bar", ExamplePage.class);};
 * </pre>
 * <br>
 * To load a class 
 * that is annotated with 
 * a parameterized annotation {@code @Page("http://www.example.com/?name={0}&page={1}")}
 * <pre>
 * {@code ExamplePage example = Browser.get(ExamplePage.class, "john", "1");}
 * </pre>
 * @author beothorn
 * @see webGrude.annotations.Page
 * @see webGrude.annotations.Selector
 */
public class Browser {

    private static BrowserClient webClient;
    private static String currentPageUrl;
    private static String currentPageContents;

    /**
     *  Loads content from url from the Page annotation on pageClass onto an instance of pageClass.
     *
     * @param <T> A instance of the class with a {@literal @}Page annotantion
     * @param pageClass A class with a {@literal @}Page annotantion
     * @param params Optional, if the pageClass has a url with parameters
     * @return The class instantiated and with the fields with the
     * {@literal @}Selector annotation populated.
     * @throws webGrude.http.GetException When calling get on the BrowserClient raises an exception 
     */
    public static <T> T get(final Class<T> pageClass,final String... params) {
    	cryIfNotAnnotated(pageClass);
        try {
                final String pageUrl = pageClass.getAnnotation(Page.class).value();
                return loadPage(pageUrl, pageClass, params);
        } catch (final Exception e) {
                throw new RuntimeException(e);
        }
    }
    /***
     * 
     * Loads content from given url onto an instance of pageClass.
     * 
     * @param <T> A instance of the class with a {@literal @}Page annotantion
     * @param pageUrl
     * @param pageClass A class with a {@literal @}Page annotantion
     * @param params Optional, if the pageClass has a url with parameters
     * @return The class instantiated and with the fields with the
     * {@literal @}Selector annotation populated.
     * @throws webGrude.http.GetException When calling get on the BrowserClient raises an exception
     * @throws webGrude.elements.WrongTypeForField When a field have a type incompatible with the page html, example a <p>foo</p> on a float field
     * @throws webGrude.TooManyResultsException When a field maps to a type but the css selector returns more than one element
     */
    public static <T> T get(final String pageUrl, final Class<T> pageClass, final String... params) {
		cryIfNotAnnotated(pageClass);
		return loadPage(pageUrl, pageClass, params);
	}

    public static String getCurentUrl() {
        return currentPageUrl;
    }

    public static String getCurentPageContents() {
        return currentPageContents;
    }

    public static void setWebClient(final BrowserClient client) {
        webClient = client;
    }

	private static <T> void cryIfNotAnnotated(final Class<T> pageClass) {
		if (!pageClass.isAnnotationPresent(Page.class))
			throw new RuntimeException("To be mapped from a page, the class must be annotated  @" + Page.class.getSimpleName());
	}

    private static String loadPage(String pageUrl, final String... params){
        if(webClient == null)
            setWebClient(new SimpleHttpClientImpl());
        try{
        	return webClient.get(pageUrl);
        }catch(Exception e){
        	throw new GetException(e, pageUrl);
        }
    }

	private static <T> T loadPage(final String pageUrl, final Class<T> pageClass, final String... params){

        String[] formattedParams = new String[params.length];

        for(int i = 0 ; i < params.length; i++){
            try {
				formattedParams[i] = URLEncoder.encode(params[i],"UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
        }

        Browser.currentPageUrl = MessageFormat.format(pageUrl, (Object[])formattedParams);

		final Document parse;
		final String page = loadPage(Browser.currentPageUrl);
        currentPageContents = page;
		parse = Jsoup.parse(page);

        T t = loadDomContents(parse, pageClass);

        Method[] declaredMethods = pageClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if(declaredMethod.getAnnotation(AfterPageLoad.class) != null){
                try {
                    declaredMethod.invoke(t);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                	throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
            }
        }
        return t;
    }

	private static <T> T loadDomContents(final Element node, final Class<T> classs){
		try {
			return internalLoadDomContents(node, classs);
		}catch (TooManyResultsException e) {
			throw e;
		}catch (WrongTypeForField e) {
			throw e;
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T internalLoadDomContents(final Element node,
			final Class<T> classs) throws NoSuchMethodException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Constructor<T> constructor;
		constructor = classs.getDeclaredConstructor(new Class[0]);
		constructor.setAccessible(true);		
		final T newInstance = constructor.newInstance(new Object[0]);

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
		f.setAccessible(true);
		f.set(newInstance, loadDomContents(innerHtml, fieldClass));
	}

	private static Element getFirstOrNullOrCryIfMoreThanOne(final Element node, final String cssQuery) {
		final Elements elements = node.select(cssQuery);
		final int size = elements.size();
		if(size > 1){
			throw new TooManyResultsException(cssQuery, size);
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
            f.setAccessible(true);
            f.set(newInstance, Instantiator.visitableForNode(selectedNode, visitableGenericClass, Browser.currentPageUrl));
        }else{
            if (typeIsKnown(fieldClass)) {
                final String attribute = selectorAnnotation.attr();
                f.setAccessible(true);
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
        	f.setAccessible(true);
            f.set(newInstance, populateListOfLinks(nodes, attribute, (ParameterizedType)type));
        }else{
            final Class<?> listClass = (Class<?>) type;
            f.setAccessible(true);
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
			f.setAccessible(true);
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
            @SuppressWarnings("unchecked")
			Link<T> link = (Link<T>) Instantiator.visitableForNode(node, classs, Browser.currentPageUrl);
            newInstanceList.add(link);
        }
        return newInstanceList;
    }

}
