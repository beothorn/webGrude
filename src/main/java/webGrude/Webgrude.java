package webGrude;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import webGrude.mapping.TooManyResultsException;
import webGrude.mapping.annotations.*;
import webGrude.mapping.elements.FieldMapping;
import webGrude.mapping.elements.Link;
import webGrude.mapping.elements.WrongTypeForField;

import java.lang.reflect.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Core class responsible for mapping HTML or XML content to Java objects annotated with Webgrude annotations.
 * <p>
 * Use this to parse a page's contents and populate annotated fields with selected data.
 */
public class Webgrude {


    /**
     * Synthetic root tag used when parsing XML and HTML fragments so it always have a root.
     */
    public static final String ROOT_FAKE = "rootFake";

    private final boolean debug;


    /**
     * Creates a new Webgrude instance.
     */
    public Webgrude() {
        debug = false;
    }

    /**
     * Creates a new Webgrude instance.
     *
     * @param debug whether to enable debug logging
     */
    public Webgrude(boolean debug) {
        this.debug = debug;
    }

    /**
     * Maps an HTML string to a class with {@literal @}Selector annotations.
     *
     * @param pageContents the HTML content to be parsed
     * @param pageClass    the class with annotated fields to populate
     * @param <T>          the type of the page class
     * @return an instance of the page class populated with data from the HTML
     */
    public <T> T map(
            final String pageContents,
            final Class<T> pageClass
    ) {
        final String url = url(pageClass);
        return map(pageContents, pageClass, url);
    }

    /**
     * Maps an HTML string to a class with {@literal @}Selector annotations.
     *
     * @param pageContents the HTML content to be parsed
     * @param pageClass    the class with annotated fields to populate
     * @param baseUrl      base URL used to resolve relative links
     * @param <T>          the type of the page class
     * @return an instance of the page class populated with data from the HTML
     */
    public <T> T map(
        final String pageContents,
        final Class<T> pageClass,
        final String baseUrl
    ) {
        final boolean isXML = pageClass.getAnnotation(XML.class) != null;

        T pageObjectInstance;
        final Document doc;
        if (isXML) {
            doc = Jsoup.parse("<"+ROOT_FAKE+">" + pageContents + "</"+ROOT_FAKE+">", Parser.xmlParser());
        } else {
            doc = Jsoup.parse(pageContents);
        }
        try {
            pageObjectInstance = internalLoadContents(baseUrl, doc, pageClass);
        } catch (TooManyResultsException | WrongTypeForField e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

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

    private <T> T internalLoadContents(
        final String baseUrl,
        final Element node,
        final Class<T> clazz
    ) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        final T newInstance = getNewInstance(clazz);

        final Field[] declaredFields = clazz.getDeclaredFields();
        for (final Field field : declaredFields) {
            if (!FieldMapping.hasMappingAnnotation(field)) continue;

            final Class<?> fieldType = field.getType();

            logDebug("\nField '"+field.getName()+"' of type '"+ fieldType.getSimpleName() + "'");

            if (fieldType.equals(java.util.List.class)) {
                populateListField(baseUrl, node, newInstance, field);
            } else {
                populateField(baseUrl, node, newInstance, field);
            }
        }
        return newInstance;
    }

    private static <T> @NotNull T getNewInstance(Class<T> clazz) throws InstantiationException, IllegalAccessException,
            InvocationTargetException {
        final Constructor<T> constructor;
        try{
            constructor = clazz.getDeclaredConstructor();
        }catch(final NoSuchMethodException e){
            throw new RuntimeException("If your class is an inner class, perhaps you should declare " +
                    "'public static class' instead of 'public class'", e);
        }
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private Element getFirstOrNullOrCryIfMoreThanOne(
        final Element node,
        final boolean isXpath,
        final String query
    ){
        logDebug("\tLook for element, selector isXpath: '"+isXpath+"' selector: '"+query+"'");
        final Elements elements = (isXpath) ? node.selectXpath("/" + ROOT_FAKE + query) : node.select(query);
        final int size = elements.size();
        if (size > 1) {
            logDebug("\tToo many entries'"+size+ "', selector isXpath: '"+isXpath+"' selector: '"+query+"'");
            throw new TooManyResultsException(query, size, elements);
        }
        if (size == 0) {
            logDebug("\tNothing found, selector isXpath: '"+isXpath+"' selector: '"+query+"'");
            return null;
        }
        Element first = elements.first();
        logDebug("\tFound '"+ first.text()+ "', selector isXpath: '"+isXpath+"' selector: '"+query+"'");
        return first;
    }

    private static void throwException(Class<?> fieldClass) {
        throw new RuntimeException("Can't convert html to class " + fieldClass.getName() + "\n" +
                "The field type must be a class with Webgrude annotations or one of these types:\n" +
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

    private <T> void populateField(
        final String baseUrl,
        final Element node,
        final T newInstance,
        final Field field
    ) throws IllegalAccessException {
        final Class<?> fieldType = field.getType();
        logDebug("\tPopulate field of type '"+fieldType+"'");
        if (fieldType.equals(java.util.List.class)) {
            // Does it ever get here?
            solveAnnotatedListField(baseUrl, node, newInstance, field);
        } else {
            solveAnnotatedFieldWithMappableType(baseUrl, node, newInstance, field);
        }
    }

    private <T> void solveAnnotatedFieldWithMappableType(
            final String baseUrl,
            final Element node,
            final T newInstance,
            final Field field
    ) throws IllegalAccessException {
        Optional<FieldMapping> maybeFieldMapping = FieldMapping.from(field);
        logDebug("\tAnnotation presence is " + maybeFieldMapping.isPresent());
        if (maybeFieldMapping.isEmpty()) return;

        final FieldMapping fieldMapping = maybeFieldMapping.get();

        final Element selectedNode = getFirstOrNullOrCryIfMoreThanOne(
            node,
            fieldMapping.useXpath(),
            fieldMapping.value()
        );
        if (selectedNode == null) return;

        final Class<?> fieldType = field.getType();
        if (typeIsVisitable(fieldType)) {
            logDebug("\tType is visitable");
            final Type genericType = field.getGenericType();

            if (genericType instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType) genericType;
                final Type actualType = parameterizedType.getActualTypeArguments()[0];

                if (actualType instanceof Class<?>) {
                    final Class<?> clazz = (Class<?>) actualType;
                    field.setAccessible(true);
                    if (!field.canAccess(newInstance)) {
                        throw new RuntimeException("Can't access field " + field.getName());
                    }
                    try{
                        Object fieldValue = visitableForNode(
                            this,
                            selectedNode,
                            clazz,
                            baseUrl
                        );
                        logDebug("\t\tSet ('"+field.getName()+"' = '"+fieldValue+"')");
                        field.set(newInstance, fieldValue);
                        return;
                    } catch (final IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            throwException(fieldType);
        }

        if (typeIsKnown(fieldType)) {
            logDebug("\tType is known");
            field.setAccessible(true);
            if (!field.canAccess(newInstance)) {
                throw new RuntimeException("Can't access field " + field.getName());
            }
            Object value = instanceForNode(selectedNode, fieldMapping, fieldType, baseUrl);
            logDebug("\t\tSet ('"+field.getName()+"' = '"+value+"')");
            field.set(newInstance, value);
            return;
        }

        try {
            logDebug("\tType is unknown");
            Element element = new Element(ROOT_FAKE, baseUrl);
            element.appendChildren(selectedNode.children());
            Object o = internalLoadContents(baseUrl, element, fieldType);
            field.setAccessible(true);
            if (!field.canAccess(newInstance)) {
                logDebug("\tCan't access field " + field.getName());
                throw new RuntimeException("Can't access field " + field.getName());
            }
            logDebug("\tNew instance is '"+o+"'");
            logDebug("\t\tSet ('"+field.getName()+"' = '"+o+"')");
            field.set(newInstance, o);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    private <T> void solveAnnotatedListField(
        final String baseUrl,
        final Element node,
        final T newInstance,
        final Field field
    ) {
        final Type genericType = field.getGenericType();
        final Optional<FieldMapping> maybeFieldMapping = FieldMapping.from(field);
        maybeFieldMapping.ifPresent( fieldMapping -> {
            final String value = fieldMapping.value();
            final Elements nodes = fieldMapping.useXpath() ? node.selectXpath("/root" + value) : node.select(value);
            final Type type = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            if (type instanceof ParameterizedType) {
                field.setAccessible(true);
                try {
                    final ArrayList<Link<Object>> listOfLinks = populateListOfLinks(
                        baseUrl,
                        nodes,
                        (ParameterizedType) type
                    );
                    logDebug("\t\tSet ('"+field.getName()+"' = '"+listOfLinks+"')");
                    field.set(newInstance, listOfLinks);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            final Class<?> listClass = (Class<?>) type;
            field.setAccessible(true);
            try {
                List<?> valueList = populateList(baseUrl, nodes, fieldMapping, listClass);
                logDebug("\t\tSet ('"+field.getName()+"' = '"+valueList+"')");
                field.set(newInstance, valueList);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T> void populateListField(
        final String baseUrl,
        final Element node,
        final T newInstance,
        final Field field
    ) {
        final Class<?> listClass = getListClass(field);

        logDebug("\tPopulate list of '"+listClass.getName()+"´");

        final Optional<FieldMapping> maybeFieldMapping = FieldMapping.from(field);

        maybeFieldMapping.ifPresent(fieldMapping -> {
            final String query = fieldMapping.value();
            final Elements nodes = fieldMapping.useXpath() ? node.selectXpath("/"+ ROOT_FAKE + query) : node.select(query);
            field.setAccessible(true);
            try {
                List<?> value = populateList(baseUrl, nodes, fieldMapping, listClass);
                logDebug("\t\tSet ('"+field.getName()+"' = '"+value+"')");
                field.set(newInstance, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Nullable
    private static Class<?> getListClass(Field field) {
        final Type genericType = field.getGenericType();
        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];

        Class<?> listClass = null;
        if (actualTypeArgument instanceof Class) {
            listClass = (Class<?>) actualTypeArgument;
        }
        if (actualTypeArgument instanceof ParameterizedType){
            listClass = (Class<?>) ((ParameterizedType)actualTypeArgument).getRawType();
        }
        return listClass;
    }

    private <T> List<T> populateList(
        final String baseUrl,
        final Elements nodes,
        final FieldMapping fieldMapping,
        final Class<T> clazz
    ) {
        final ArrayList<T> newInstanceList = new ArrayList<>();
        for (final Element node : nodes) {
            if (typeIsKnown(clazz)) {
                logDebug("\t\tInstantiate known type");
                newInstanceList.add(instanceForNode(node, fieldMapping, clazz, baseUrl));
            } else {
                try {
                    logDebug("\t\tInstantiate unknown type '"+clazz.getName()+"'");
                    newInstanceList.add(internalLoadContents(baseUrl, node, clazz));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
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
            final Link<T> link = visitableForNode(this, node, clazz, baseUrl);
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

    /**
     * Checks whether the provided class is a type natively supported by Webgrude (String, Integer, Float, etc.).
     *
     * @param c the class to check
     * @return true if the class is known and directly mappable, false otherwise
     */
    public boolean typeIsKnown(final Class c) {
        return c.equals(String.class) ||
                c.equals(Integer.class) || c.getSimpleName().equals("int") ||
                c.equals(Float.class) || c.getSimpleName().equals("float") ||
                c.equals(Boolean.class) || c.getSimpleName().equals("boolean") ||
                c.equals(Link.class) ||
                c.equals(Element.class) ||
                c.equals(List.class) ||
                c.equals(Date.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T instanceForNode(
            final Element node,
            final FieldMapping fieldMapping,
            final Class<T> c,
            final String baseUrl
    ) {
        final String attribute = fieldMapping.attr();
        final String format = fieldMapping.format();
        final String locale = fieldMapping.locale();
        final String defValue = fieldMapping.defValue();

        String value;


        logDebug("\t\tNode instance type '"+c.getName()+"'");

        try {
            if (c.equals(Element.class)) {
                logDebug("\t\tElement created with content '"+node.text()+"'");
                return (T) node;
            }

            if (c.equals(Link.class)) {
                logDebug("\t\tElement created with content '"+node.text()+"'");
                return (T) new Link<>(
                    this,
                    node,
                    c,
                    baseUrl
                );
            }

            if (attribute != null && !attribute.isEmpty()) {
                if (attribute.equals("html")) {
                    value = node.html();
                    logDebug("\t\tUsing html '"+value+"'");
                } else if (attribute.equals("outerHtml")) {
                    value = node.outerHtml();
                    logDebug("\t\tUsing outerHtml '"+value+"'");
                } else {
                    value = node.attr(attribute);
                    logDebug("\t\tUsing text '"+value+"'");
                }
            } else {
                value = node.text();
                logDebug("\t\tUsing text '"+value+"'");
            }

            if(!c.equals(Date.class) && format != null && !format.isEmpty()){
                final Pattern p = Pattern.compile(format);
                final Matcher matcher = p.matcher(value);
                final boolean found = matcher.find();
                if(found){
                    value = matcher.group(1);
                    if(value.isEmpty()){
                        value = defValue;
                    }
                }else{
                    value = defValue;
                }
                logDebug("\t\tUsing date '"+value+"'");
            }

            if (c.equals(String.class)) {
                logDebug("\t\tFinal value string '"+value+"'");
                return (T) value;
            }

            if (c.equals(Date.class)) {
                Locale loc = getLocale(locale);
                final DateFormat df = new SimpleDateFormat(format, loc);
                logDebug("\t\tFinal value date '"+df.parse(value)+"'");
                return (T) df.parse(value);
            }

            if (c.equals(Integer.class) || c.getSimpleName().equals("int")) {
                logDebug("\t\tFinal value int '"+value+"'");
                return (T) Integer.valueOf(value);
            }

            if (c.equals(Float.class) || c.getSimpleName().equals("float")) {
                Locale loc = getLocale(locale);
                final NumberFormat nf = NumberFormat.getInstance(loc);
                Number number = nf.parse(value);
                logDebug("\t\tFinal value float '"+value+"'");
                return (T) Float.valueOf(number.floatValue());
            }

            if (c.equals(Boolean.class) || c.getSimpleName().equals("boolean")) {
                logDebug("\t\tFinal value boolean '"+value+"'");
                return (T) Boolean.valueOf(value);
            }
        } catch (final Exception e) {
            throw new WrongTypeForField(node, attribute, c, e);
        }

        logDebug("\t\tFinal value '"+value+"'");
        return (T) value;
    }

    private Locale getLocale(String locale) {
        Locale loc = Locale.getDefault();
        if(locale != null && !locale.isEmpty()){

            if (locale.contains("_")) {
                String[] parts = locale.split("_");
                loc = new Locale.Builder()
                        .setLanguage(parts[0])
                        .setRegion(parts[1])
                        .build();
            } else {
                loc = new Locale.Builder()
                        .setLanguage(locale)
                        .build();
            }
        }
        return loc;
    }

    private boolean typeIsVisitable(final Class<?> fieldClass) {
        return fieldClass.equals(Link.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T visitableForNode(
            final Webgrude pageToClassMapper,
            final Element node,
            final Class c,
            final String currentPageUrl
    ) {
        return (T) new Link<T>(pageToClassMapper, node, c, currentPageUrl);
    }

    private void logDebug(final String log) {
        if (debug) System.out.println(log);
    }
}