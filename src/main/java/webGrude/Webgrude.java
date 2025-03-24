package webGrude;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import webGrude.annotations.AfterPageLoad;
import webGrude.annotations.Page;
import webGrude.annotations.Selector;
import webGrude.annotations.Selectors;
import webGrude.elements.Instantiator;
import webGrude.elements.Link;
import webGrude.elements.WrongTypeForField;

import java.lang.reflect.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static webGrude.elements.Instantiator.instanceForNode;
import static webGrude.elements.Instantiator.typeIsKnown;

public class Webgrude {

    public <T> T map(
            final String pageContents,
            final Class<T> pageClass
    ) {
        final String url = url(pageClass);
        return map(pageContents, pageClass, url);
    }

    public <T> T map(
        final String pageContents,
        final Class<T> pageClass,
        final String baseUrl
    ) {
        final Document parse = Jsoup.parse(pageContents);

        final T pageObjectInstance = loadDomContents(baseUrl, parse, pageClass);

        Arrays.stream(pageClass.getDeclaredMethods())
                .filter(m -> m.getAnnotation(AfterPageLoad.class) != null)
                .forEach(m -> invokeOrThrow(m, pageObjectInstance));

        return pageObjectInstance;
    }

    public String url(final Class<?> pageClass, final String... urlTemplateParams) {
        final String pageUrlTemplate = pageClass.getAnnotation(Page.class).value();
        final List<String> formattedParams = Arrays.stream(urlTemplateParams)
                .map(Webgrude::encodeOrThrow)
                .toList();

        return MessageFormat.format(pageUrlTemplate, formattedParams.toArray());
    }

    private <T> T loadDomContents(
            final String baseUrl,
            final Element node,
            final Class<T> clazz
    ){
        try {
            return internalLoadDomContents(baseUrl, node, clazz);
        } catch (TooManyResultsException | WrongTypeForField e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T internalLoadDomContents(
        final String baseUrl,
        final Element node,
        final Class<T> clazz
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
                solveListOfAnnotatedType(baseUrl, node, newInstance, f);
            }

            if (f.getAnnotation(Selectors.class) != null) {
                solveRepeatableAnnotatedFieldWithMappableType(baseUrl, node, newInstance, f, fieldClass);
            }

            if (f.getAnnotation(Selector.class) != null) {
                solveAnnotatedField(baseUrl, node, newInstance, f, fieldClass);
            }

            if (fieldClass.getAnnotation(Selector.class) != null) {
                solveUnannotatedFieldOfAnnotatedType(baseUrl, node, newInstance, f, fieldClass);
            }

        }
        return newInstance;
    }

    private <T> void solveUnannotatedFieldOfAnnotatedType(
        final String baseUrl,
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
        f.set(newInstance, loadDomContents(baseUrl, innerHtml, fieldClass));
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

    private <T> void solveRepeatableAnnotatedFieldWithMappableType(
            final String baseUrl,
            final Element node,
            final T newInstance,
            final Field f,
            final Class<?> fieldClass
    ) throws IncompatibleTypes {
        final Selectors selectorsAnnotation = f.getAnnotation(Selectors.class);
        for (final Selector selectorAnnotation : selectorsAnnotation.value()) {
            final String cssQuery = selectorAnnotation.value();

            final Element selectedNode = getFirstOrNullOrCryIfMoreThanOne(node, cssQuery);
            if (selectedNode == null) continue;

            if (Instantiator.typeIsVisitable(fieldClass)) {
                Type genericType = f.getGenericType();

                if (genericType instanceof ParameterizedType outerType) {
                    Type innerType = outerType.getActualTypeArguments()[0];
                    if (innerType instanceof ParameterizedType innerParamType) {
                        Type deepestType = innerParamType.getActualTypeArguments()[0];

                        if (deepestType instanceof Class<?> clazz) {
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
                    f.set(newInstance, instanceForNode(
                            selectedNode,
                            selectorAnnotation,
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
            final Class<?> fieldClass
    ) throws IllegalAccessException, InstantiationException {
        if (fieldClass.equals(java.util.List.class)) {
            solveAnnotatedListField(baseUrl, node, newInstance, f);
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
        final Selector selectorAnnotation = f.getAnnotation(Selector.class);
        final String cssQuery = selectorAnnotation.value();
        final Element selectedNode = getFirstOrNullOrCryIfMoreThanOne(node, cssQuery);
        if (selectedNode == null) return;

        if (Instantiator.typeIsVisitable(fieldClass)) {
            Type genericType = f.getGenericType();

            if (genericType instanceof ParameterizedType parameterizedType) {
                Type actualType = parameterizedType.getActualTypeArguments()[0];

                if (actualType instanceof Class<?> clazz) {
                    f.setAccessible(true);
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

    private <T> void solveAnnotatedListField(
            final String baseUrl,
            final Element node,
            final T newInstance,
            final Field f
    ) throws IllegalAccessException {
        final Type genericType = f.getGenericType();
        final Selector selector = f.getAnnotation(Selector.class);
        final String cssQuery = selector.value();
        final Elements nodes = node.select(cssQuery);
        final Type type = ((ParameterizedType) genericType).getActualTypeArguments()[0];
        if (type instanceof ParameterizedType) {
            f.setAccessible(true);
            f.set(newInstance, populateListOfLinks(baseUrl, nodes, (ParameterizedType) type));
            return;
        }

        final Class<?> listClass = (Class<?>) type;
        f.setAccessible(true);
        f.set(newInstance, populateList(baseUrl, nodes, selector, listClass));
    }

    private <T> void solveListOfAnnotatedType(
        final String baseUrl,
        final Element node,
        final T newInstance,
        final Field f
    ) throws IllegalAccessException {
        final Type genericType = f.getGenericType();
        final Class<?> listClass = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        final Selector selectorAnnotation = listClass.getAnnotation(Selector.class);
        if (selectorAnnotation == null) return;

        final String cssQuery = selectorAnnotation.value();
        final Elements nodes = node.select(cssQuery);
        f.setAccessible(true);
        f.set(newInstance, populateList(baseUrl, nodes, selectorAnnotation, listClass));
    }

    private <T> List<T> populateList(
        final String baseUrl,
        final Elements nodes,
        final Selector selector,
        final Class<T> clazz
    ) {
        final ArrayList<T> newInstanceList = new ArrayList<>();
        for (final Element node : nodes) {
            if (typeIsKnown(clazz)) {
                newInstanceList.add(instanceForNode(node, selector, clazz));
            } else {
                newInstanceList.add(loadDomContents(baseUrl, node, clazz));
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