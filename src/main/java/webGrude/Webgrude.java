package webGrude;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import webGrude.mapping.IncompatibleTypes;
import webGrude.mapping.TooManyResultsException;
import webGrude.mapping.annotations.*;
import webGrude.mapping.elements.FieldMapping;
import webGrude.mapping.elements.Instantiator;
import webGrude.mapping.elements.Link;
import webGrude.mapping.elements.WrongTypeForField;

import java.lang.reflect.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.text.MessageFormat;
import java.util.*;

import static webGrude.mapping.elements.Instantiator.instanceForNode;
import static webGrude.mapping.elements.Instantiator.typeIsKnown;

public class Webgrude {

    /***
     * Maps a Html string to a class with {@literal @}Selector annotations
     * @param pageContents
     * @param pageClass
     * @return the page instance
     * @param <T> The page class
     */
    public <T> T map(
            final String pageContents,
            final Class<T> pageClass
    ) {
        final String url = url(pageClass);
        return map(pageContents, pageClass, url);
    }

    /***
     * Maps a Html string to a class with {@literal @}Selector annotations
     * @param pageContents
     * @param pageClass
     * @param baseUrl Used to populate links with the full href
     * @return the page instance
     * @param <T> The page class
     */
    public <T> T map(
        final String pageContents,
        final Class<T> pageClass,
        final String baseUrl
    ) {
        XML xmlAnnotation = pageClass.getAnnotation(XML.class);

        ParseFormat format = ParseFormat.HTML;

        if (xmlAnnotation != null) {
            format = ParseFormat.XML;
        }

        final Document parse = format.equals(ParseFormat.HTML) ?
                Jsoup.parse(pageContents) : Jsoup.parse("<root>" + pageContents + "</root>", Parser.xmlParser());

        final T pageObjectInstance = loadContents(baseUrl, parse, pageClass, format);

        Arrays.stream(pageClass.getDeclaredMethods())
                .filter(m -> m.getAnnotation(AfterPageLoad.class) != null)
                .forEach(m -> invokeOrThrow(m, pageObjectInstance));

        return pageObjectInstance;
    }

    /***
     * Gets the url from a class with a {@literal @}Page annotation
     * @param pageClass A class with a {@literal @}Page annotation and an url value
     * @param urlTemplateParams The values to replace the tokens.
     * @return The url with the replaced tokens
     */
    public String url(final Class<?> pageClass, final String... urlTemplateParams) {
        Page pageAnnotation = pageClass.getAnnotation(Page.class);
        final String pageUrlTemplate = pageAnnotation != null ? pageAnnotation.value() : "";

        return MessageFormat.format(pageUrlTemplate, Arrays.stream(urlTemplateParams)
                .map(Webgrude::encodeOrThrow).toArray());
    }

    private <T> T loadContents(
            final String baseUrl,
            final Element node,
            final Class<T> clazz,
            final ParseFormat parseFormat
    ){
        try {
            return internalLoadContents(baseUrl, node, clazz, parseFormat);
        } catch (TooManyResultsException | WrongTypeForField e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T internalLoadContents(
        final String baseUrl,
        final Element node,
        final Class<T> clazz,
        final ParseFormat parseFormat
    ) throws InstantiationException, IllegalAccessException, InvocationTargetException, IncompatibleTypes {
        final Constructor<T> constructor;
        try{
            constructor = clazz.getDeclaredConstructor();
        }catch(final java.lang.NoSuchMethodException e){
            throw new RuntimeException("If your class is an inner class, perhaps you should declare 'public static class' instead of 'public class'", e);
        }
        constructor.setAccessible(true);
        final T newInstance = constructor.newInstance();

        final Field[] declaredFields = clazz.getDeclaredFields();
        for (final Field f : declaredFields) {
            final Class<?> fieldClass = f.getType();

            if (fieldClass.equals(java.util.List.class) && f.getAnnotation(Selector.class) == null) {
                solveListOfAnnotatedType(baseUrl, node, newInstance, f, parseFormat);
            }

            if (f.getAnnotation(Selectors.class) != null) {
                solveRepeatableAnnotatedFieldWithMappableType(baseUrl, node, newInstance, f, fieldClass);
            }

            if (hasMappingAnnotation(f)) {
                solveAnnotatedField(baseUrl, node, newInstance, f, fieldClass, parseFormat);
            }

            if (hasMappingAnnotation(fieldClass)) {
                solveUnannotatedFieldOfAnnotatedType(baseUrl, node, newInstance, f, fieldClass, parseFormat);
            }

        }
        return newInstance;
    }

    private static boolean hasMappingAnnotation(Field f) {
        return f.getAnnotation(Selector.class) != null || f.getAnnotation(XPath.class) != null;
    }

    private static boolean hasMappingAnnotation(Class<?> fieldClass) {
        return fieldClass.getAnnotation(Selector.class) != null || fieldClass.getAnnotation(XPath.class) != null;
    }

    private static boolean isXPath(final Class<?> fieldClass) {
        return fieldClass.getAnnotation(XPath.class) != null;
    }

    private static boolean isXPath(final Field f) {
        return f.getAnnotation(XPath.class) != null;
    }

    private static String getQuery(Class<?> fieldClass) {
        final Selector selector = fieldClass.getAnnotation(Selector.class);
        if (selector != null) return selector.value();
        final XPath xpath = fieldClass.getAnnotation(XPath.class);
        if (xpath != null) return xpath.value();
        throw new InvalidParameterException("Class has no annotation Selector or XPath");
    }

    private static String getQuery(Field f) {
        final Selector selector = f.getAnnotation(Selector.class);
        if (selector != null) return selector.value();
        final XPath xpath = f.getAnnotation(XPath.class);
        if (xpath != null) return xpath.value();
        throw new InvalidParameterException("Class has no annotation Selector or XPath");
    }

    private <T> void solveUnannotatedFieldOfAnnotatedType(
        final String baseUrl,
        final Element node,
        final T newInstance,
        final Field f,
        final Class<?> fieldClass,
        final ParseFormat parseFormats
    ) throws IllegalAccessException {
        boolean isXPath = isXPath(fieldClass);
        final String query = getQuery(fieldClass);

        final Element selectedNode = getFirstOrNullOrCryIfMoreThanOne(node, isXPath, query);
        if (selectedNode == null) {
            return;
        }

        if (parseFormats.equals(ParseFormat.HTML)) {
            final Document doc = Jsoup.parse(selectedNode.html());
            f.setAccessible(true);
            f.set(newInstance, loadContents(baseUrl, doc, fieldClass, ParseFormat.HTML));
        }

        if (parseFormats.equals(ParseFormat.XML)) {
            final Document doc = Jsoup.parse("<root>" + selectedNode.html() + "</root>", Parser.xmlParser());
            f.setAccessible(true);
            f.set(newInstance, loadContents(baseUrl, doc, fieldClass, ParseFormat.XML));
        }

    }

    private static Element getFirstOrNullOrCryIfMoreThanOne(
            final Element node,
            final boolean isXpath,
            final String query
    ){
        final Elements elements = (isXpath) ? node.selectXpath("/root" + query) : node.select(query);
        final int size = elements.size();
        if (size > 1) {
            throw new TooManyResultsException(query, size);
        }
        if (size == 0) {
            return null;
        }
        return elements.first();
    }

    private <T> void solveRepeatableAnnotatedFieldWithMappableType(
            final String baseUrl,
            final Element node,
            final T newInstance,
            final Field f,
            final Class<?> fieldClass
    ) throws IncompatibleTypes {
        final Selectors selectorsAnnotation = f.getAnnotation(Selectors.class);
        for (final Selector selectorAnnotation : selectorsAnnotation.value()) {
            final String query = selectorAnnotation.value();

            final Element selectedNode = getFirstOrNullOrCryIfMoreThanOne(node, false , query);
            if (selectedNode == null) continue;

            if (Instantiator.typeIsVisitable(fieldClass)) {
                Type genericType = f.getGenericType();

                if (genericType instanceof ParameterizedType) {
                    final ParameterizedType outerType = (ParameterizedType) genericType;
                    final Type innerType = outerType.getActualTypeArguments()[0];
                    if (innerType instanceof ParameterizedType) {
                        final ParameterizedType innerParamType = (ParameterizedType) innerType;
                        final Type deepestType = innerParamType.getActualTypeArguments()[0];

                        if (deepestType instanceof Class<?>) {
                            final Class<?> clazz = (Class<?>) deepestType;
                            f.setAccessible(true);
                            try{
                                f.set(newInstance, Instantiator.visitableForNode(this, selectedNode, clazz, baseUrl));
                                return;
                            } catch (final IllegalAccessException e) {
                                throw new IncompatibleTypes(newInstance, selectedNode, selectorAnnotation, fieldClass, e);
                            }
                        }
                    }
                }
                throw new RuntimeException("Could not get generic for list");
            }

            if (typeIsKnown(fieldClass)) {
                f.setAccessible(true);
                try{
                    final Optional<FieldMapping> maybeFieldMapping = FieldMapping.from(f);
                    final FieldMapping fieldMapping = maybeFieldMapping.orElseThrow();
                    f.set(newInstance, instanceForNode(
                            selectedNode,
                            fieldMapping,
                            fieldClass
                    ));
                } catch (final IllegalArgumentException | IllegalAccessException e){
                    throw new IncompatibleTypes(newInstance, selectedNode, selectorAnnotation, fieldClass, e);
                }
                return;
            }

            throwException(fieldClass);
        }

    }

    private static void throwException(Class<?> fieldClass) {
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

    private <T> void solveAnnotatedField(
            final String baseUrl,
            final Element node,
            final T newInstance,
            final Field f,
            final Class<?> fieldClass,
            final ParseFormat parseFormat
    ) throws IllegalAccessException {
        if (fieldClass.equals(java.util.List.class)) {
            solveAnnotatedListField(baseUrl, node, newInstance, f, parseFormat);
        } else {
            solveAnnotatedFieldWithMappableType(baseUrl, node, newInstance, f, fieldClass);
        }
    }

    private <T> void solveAnnotatedFieldWithMappableType(
            final String baseUrl,
            final Element node,
            final T newInstance,
            final Field f,
            final Class<?> fieldClass
    ) throws IllegalAccessException {
        Optional<FieldMapping> maybeFieldMapping = FieldMapping.from(f);

        if (maybeFieldMapping.isEmpty()) return;

        final FieldMapping fieldMapping = maybeFieldMapping.get();

        final Element selectedNode = getFirstOrNullOrCryIfMoreThanOne(node, fieldMapping.useXpath(), fieldMapping.value());
        if (selectedNode == null) return;

        if (Instantiator.typeIsVisitable(fieldClass)) {
            final Type genericType = f.getGenericType();

            if (genericType instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType) genericType;
                final Type actualType = parameterizedType.getActualTypeArguments()[0];

                if (actualType instanceof Class<?>) {
                    final Class<?> clazz = (Class<?>) actualType;
                    f.setAccessible(true);
                    if (!f.canAccess(newInstance)) {
                        throw new RuntimeException("Can't access field " + f.getName());
                    }
                    try{
                        f.set(newInstance, Instantiator.visitableForNode(this, selectedNode, clazz, baseUrl));
                        return;
                    } catch (final IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            throwException(fieldClass);
        }

        if (typeIsKnown(fieldClass)) {
            f.setAccessible(true);
            if (!f.canAccess(newInstance)) {
                throw new RuntimeException("Can't access field " + f.getName());
            }
            f.set(newInstance, instanceForNode(selectedNode, fieldMapping, fieldClass));
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

    private <T> void solveAnnotatedListField(
            final String baseUrl,
            final Element node,
            final T newInstance,
            final Field f,
            final ParseFormat parseFormat
    ) throws IllegalAccessException {
        final Type genericType = f.getGenericType();
        Optional<FieldMapping> maybeFieldMapping = FieldMapping.from(f);
        maybeFieldMapping.ifPresent( fieldMapping -> {
            final String value = fieldMapping.value();
            final Elements nodes = fieldMapping.useXpath() ? node.selectXpath("/root" + value) : node.select(value);
            final Type type = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            if (type instanceof ParameterizedType) {
                f.setAccessible(true);
                try {
                    f.set(newInstance, populateListOfLinks(baseUrl, nodes, (ParameterizedType) type));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            final Class<?> listClass = (Class<?>) type;
            f.setAccessible(true);
            try {
                f.set(newInstance, populateList(baseUrl, nodes, fieldMapping, listClass, parseFormat));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T> void solveListOfAnnotatedType(
        final String baseUrl,
        final Element node,
        final T newInstance,
        final Field f,
        final ParseFormat parseFormat
    ) {
        final Type genericType = f.getGenericType();
        final Class<?> listClass = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        Optional<FieldMapping> maybeFieldMapping = FieldMapping.from(listClass);

        maybeFieldMapping.ifPresent( fieldMapping -> {
            final String query = fieldMapping.value();
            final Elements nodes = fieldMapping.useXpath() ? node.selectXpath("/root" + query) : node.select(query);
            f.setAccessible(true);
            try {
                f.set(newInstance, populateList(baseUrl, nodes, fieldMapping, listClass, parseFormat));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T> List<T> populateList(
        final String baseUrl,
        final Elements nodes,
        final FieldMapping fieldMapping,
        final Class<T> clazz,
        final  ParseFormat parseFormat
    ) {
        final ArrayList<T> newInstanceList = new ArrayList<>();
        for (final Element node : nodes) {
            if (typeIsKnown(clazz)) {
                newInstanceList.add(instanceForNode(node, fieldMapping, clazz));
            } else {
                newInstanceList.add(loadContents(baseUrl, node, clazz, parseFormat));
            }
        }
        return newInstanceList;
    }

    private <T> ArrayList<Link<T>> populateListOfLinks(
            final String baseUrl,
            final Elements nodes,
            final ParameterizedType paraType
    ) {
        final ArrayList<Link<T>> newInstanceList = new ArrayList<>();
        for (final Element node : nodes) {
            final Class<?> clazz = (Class<?>) paraType.getActualTypeArguments()[0];
            final Link<T> link = Instantiator.visitableForNode(this, node, clazz, baseUrl);
            newInstanceList.add(link);
        }
        return newInstanceList;
    }

    private <T> void invokeOrThrow(final Method method, final T instance) {
        try {
            method.invoke(instance);
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    private static String encodeOrThrow(final String p) {
        return URLEncoder.encode(p, StandardCharsets.UTF_8);
    }
}