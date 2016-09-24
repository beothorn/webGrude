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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.reflect.TypeToken;

import webGrude.annotations.AfterPageLoad;
import webGrude.annotations.Page;
import webGrude.annotations.Selector;
import webGrude.elements.Instantiator;
import webGrude.elements.Link;
import webGrude.elements.WrongTypeForField;
import webGrude.http.BrowserClient;
import webGrude.http.GetException;
import webGrude.http.SimpleHttpClientImpl;

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
 *
 * @author beothorn
 * @see webGrude.annotations.Page
 * @see webGrude.annotations.Selector
 */
public class Browser {

    private static BrowserClient webClient;
    private static String currentPageUrl;
    private static String currentPageContents;

    /**
     * Loads content from url from the Page annotation on pageClass onto an instance of pageClass.
     *
     * @param <T>       A instance of the class with a {@literal @}Page annotantion
     * @param pageClass A class with a {@literal @}Page annotantion
     * @param params    Optional, if the pageClass has a url with parameters
     * @return The class instantiated and with the fields with the
     * {@literal @}Selector annotation populated.
     * @throws webGrude.http.GetException When calling get on the BrowserClient raises an exception
     */
    public static <T> T get(final Class<T> pageClass, final String... params) {
        cryIfNotAnnotated(pageClass);
        try {
            final String pageUrl = pageClass.getAnnotation(Page.class).value();
            return loadPage(pageUrl, pageClass, Arrays.asList(params));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * Loads content from given url onto an instance of pageClass.
     *
     * @param <T>       A instance of the class with a {@literal @}Page annotantion
     * @param pageUrl
     * @param pageClass A class with a {@literal @}Page annotantion
     * @param params    Optional, if the pageClass has a url with parameters
     * @return The class instantiated and with the fields with the
     * {@literal @}Selector annotation populated.
     * @throws webGrude.http.GetException          When calling get on the BrowserClient raises an exception
     * @throws webGrude.elements.WrongTypeForField When a field have a type incompatible with the page html, example a <p>foo</p> on a float field
     * @throws webGrude.TooManyResultsException    When a field maps to a type but the css selector returns more than one element
     */
    public static <T> T get(final String pageUrl, final Class<T> pageClass, final String... params) {
        cryIfNotAnnotated(pageClass);
        return loadPage(pageUrl, pageClass, Arrays.asList(params));
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
        if (!pageClass.isAnnotationPresent(Page.class)) {
            throw new RuntimeException("To be mapped from a page, the class must be annotated  @" + Page.class.getSimpleName());
        }
    }

    private static String loadPage(final String pageUrl, final String... params) {
        if (webClient == null) {
            setWebClient(new SimpleHttpClientImpl());
        }
        try {
            return webClient.get(pageUrl);
        } catch (final Exception e) {
            throw new GetException(e, pageUrl);
        }
    }

    private static <T> T loadPage(
        final String pageUrl,
        final Class<T> pageClass,
        final List<String> params
    ){

        final List<String> formattedParams = params.stream()
            .map(Browser::encodeOrThrow)
            .collect(Collectors.toList());

        Browser.currentPageUrl = MessageFormat.format(pageUrl, formattedParams.toArray());

        final String pageContents = loadPage(Browser.currentPageUrl);
        currentPageContents = pageContents;


        final Document parse = Jsoup.parse(pageContents);

        final T pageObjectInstance = loadDomContents(parse, pageClass);

        Arrays.asList(pageClass.getDeclaredMethods()).stream()
            .filter(m -> m.getAnnotation(AfterPageLoad.class) != null)
            .forEach(m -> Browser.invokeOrThrow(m, pageObjectInstance));

        return pageObjectInstance;
    }

    private static <T> void invokeOrThrow(final Method method, final T instance) {
        try {
            method.invoke(instance);
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    private static String encodeOrThrow(final String p) {
        try {
            return URLEncoder.encode(p, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T loadDomContents(
        final Element node,
        final Class<T> clazz
    ){
        try {
            return internalLoadDomContents(node, clazz);
        } catch (TooManyResultsException | WrongTypeForField e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T internalLoadDomContents(final Element node, final Class<T> clazz)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        final Constructor<T> constructor;
        try{
            constructor = clazz.getDeclaredConstructor();
        }catch(final java.lang.NoSuchMethodException e){
            throw new RuntimeException("If your class is an inner class, perhaps you should declare 'public static class' instead of 'public class'", e);
        }
        constructor.setAccessible(true);
        final T newInstance = constructor.newInstance();

        if (clazz.getAnnotation(Selector.class) == null && clazz.getAnnotation(Page.class) == null) {
            return newInstance;
        }

        final Field[] declaredFields = clazz.getDeclaredFields();
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

    private static <T> void solveUnanotatedFieldOfAnnotatedType(
            final Element node,
            final T newInstance,
            final Field f,
            final Class<?> fieldClass
    ) throws IllegalAccessException, InstantiationException {
        final String cssQuery = fieldClass.getAnnotation(Selector.class).value();
        final Element selectedNode = getFirstOrNullOrCryIfMoreThanOne(node, cssQuery);
        if (selectedNode == null) {
            return;
        }
        final Document innerHtml = Jsoup.parse(selectedNode.html());
        f.setAccessible(true);
        f.set(newInstance, loadDomContents(innerHtml, fieldClass));
    }

    private static Element getFirstOrNullOrCryIfMoreThanOne(
       final Element node,
       final String cssQuery
    ){
        final Elements elements = node.select(cssQuery);
        final int size = elements.size();
        if (size > 1) {
            throw new TooManyResultsException(cssQuery, size);
        }
        if (size == 0) {
            return null;
        }
        return elements.first();
    }

    private static <T> void solveAnnotatedField(
        final Element node,
        final T newInstance,
        final Field f,
        final Class<?> fieldClass
    ) throws IllegalAccessException, InstantiationException {
        if (fieldClass.equals(java.util.List.class)) {
            solveAnnotatedListField(node, newInstance, f);
        } else {
            solveAnnotatedFieldWithMappableType(node, newInstance, f, fieldClass);
        }
    }

    private static <T> void solveAnnotatedFieldWithMappableType(
        final Element node,
        final T newInstance,
        final Field f,
        final Class<?> fieldClass
    ) throws IllegalAccessException {
        final Selector selectorAnnotation = f.getAnnotation(Selector.class);
        final String cssQuery = selectorAnnotation.value();
        final Element selectedNode = getFirstOrNullOrCryIfMoreThanOne(node, cssQuery);
        if (selectedNode == null) return;

        if (Instantiator.typeIsVisitable(fieldClass)) {
            final Class<?> visitableGenericClass = TypeToken.of(f.getGenericType()).resolveType(Link.class.getTypeParameters()[0]).getRawType();
            f.setAccessible(true);
            f.set(newInstance, Instantiator.visitableForNode(selectedNode, visitableGenericClass, Browser.currentPageUrl));
            return;
        }

        if (typeIsKnown(fieldClass)) {
            f.setAccessible(true);
            f.set(newInstance, instanceForNode(selectedNode, selectorAnnotation, fieldClass));
            return;
        }

        throw new RuntimeException("Can't convert html to class " + fieldClass.getName() + "\n" +
                "The field type must be a class with " + Page.class.getSimpleName() + " annotation or one of these types:\n" +
                List.class.getCanonicalName() + "\n" +
                String.class.getCanonicalName() + "\n" +
                Integer.class.getCanonicalName() + "\n" +
                Float.class.getCanonicalName() + "\n" +
                Boolean.class.getCanonicalName() + "\n" +
                Link.class.getCanonicalName() + "\n" +
                Element.class.getCanonicalName() + "\n"+
                Date.class.getCanonicalName() + "\n"
        );
    }

    private static <T> void solveAnnotatedListField(
        final Element node,
        final T newInstance,
        final Field f
    ) throws IllegalAccessException, InstantiationException {
        final Type genericType = f.getGenericType();
        final Selector selector = f.getAnnotation(Selector.class);
        final String cssQuery = selector.value();
        final Elements nodes = node.select(cssQuery);
        final Type type = ((ParameterizedType) genericType).getActualTypeArguments()[0];
        if (type instanceof ParameterizedType) {
            f.setAccessible(true);
            f.set(newInstance, populateListOfLinks(nodes, (ParameterizedType) type));
            return;
        }

        final Class<?> listClass = (Class<?>) type;
        f.setAccessible(true);
        f.set(newInstance, populateList(nodes, selector, listClass));
    }

    private static <T> void solveListOfAnnotatedType(
        final Element node,
        final T newInstance,
        final Field f
    ) throws IllegalAccessException, InstantiationException {
        final Type genericType = f.getGenericType();
        final Class<?> listClass = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        final Selector selectorAnnotation = listClass.getAnnotation(Selector.class);
        if (selectorAnnotation == null) return;

        final String cssQuery = selectorAnnotation.value();
        final Elements nodes = node.select(cssQuery);
        f.setAccessible(true);
        f.set(newInstance, populateList(nodes, selectorAnnotation, listClass));
    }

    private static <T> List<T> populateList(
        final Elements nodes,
        final Selector selector,
        final Class<T> clazz
    ) throws InstantiationException, IllegalAccessException {
        final ArrayList<T> newInstanceList = new ArrayList<>();
        for (final Element node : nodes) {
            if (typeIsKnown(clazz)) {
                newInstanceList.add(instanceForNode(node, selector, clazz));
            } else {
                newInstanceList.add(loadDomContents(node, clazz));
            }
        }
        return newInstanceList;
    }

    private static <T> ArrayList<Link<T>> populateListOfLinks(
        final Elements nodes,
        final ParameterizedType paraType
    ) throws InstantiationException, IllegalAccessException {
        final ArrayList<Link<T>> newInstanceList = new ArrayList<>();
        for (final Element node : nodes) {
            final Class<?> clazz = (Class<?>) paraType.getActualTypeArguments()[0];
            final Link<T> link = Instantiator.visitableForNode(node, clazz, Browser.currentPageUrl);
            newInstanceList.add(link);
        }
        return newInstanceList;
    }

}
