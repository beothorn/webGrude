package webGrude;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import webGrude.http.GetException;
import webGrude.mapping.TooManyResultsException;
import webGrude.mapping.annotations.Page;
import webGrude.http.LinkVisitor;

import java.io.IOException;

/**
 * Instantiate a class with Selector annotations from a html String.
 * <p>
 * Examples:
 * </p>
 * To load a class
 * that is annotated as <i>{@literal @}Page("http://www.example.com")</i><br>
 * <pre>
 * {@code ExamplePage example = new OkHttpBrowser().get(ExamplePage.class);}
 * </pre>
 * <br>
 * To load a class
 * that is annotated as <i>{@literal @}Page</i> with another url<br>
 * <pre>
 * {@code ExamplePage example = new OkHttpBrowser().get("www.foo.bar", ExamplePage.class);}
 * </pre>
 * <br>
 * To load a class
 * that is annotated with
 * a parameterized annotation {@code @Page("http://www.example.com/?name={0}&page={1}")}
 * <pre>
 * {@code ExamplePage example = new OkHttpBrowser().get(ExamplePage.class, "john", "1");}
 * </pre>
 * Or, you can create a custom OkHttpClient Request.
 * <pre>
 * {@code ExamplePage example = new OkHttpBrowser().execute(request, ExamplePage.class);}
 * </pre>
 *
 * @author beothorn
 * @see webGrude.mapping.annotations.Page
 * @see webGrude.mapping.annotations.Selector
 */
public class OkHttpBrowser implements LinkVisitor {

    private OkHttpClient client;
    private final Webgrude webgrude = new Webgrude();

    /**
     * Creates a new instance of OkHttpBrowser using the default Webgrude and OkHttpClient.
     */
    public OkHttpBrowser() {
        // Default constructor
    }

    /**
     * Loads content from url from the Page annotation on pageClass onto an instance of pageClass.
     *
     * @param <T>       An instance of the class with {@literal @}Selector annotation
     * @param pageClass A class with a {@literal @}Selector annotation
     * @param params    Optional, if the pageClass has a url with parameters
     * @return The class instantiated and with the fields with the
     * {@literal @}Selector annotation populated.
     * @throws webGrude.http.GetException When calling get on the BrowserClient raises an exception
     */
    public <T> T get(final Class<T> pageClass, final String... params) {
        cryIfNotAnnotated(pageClass);
        final String url = webgrude.url(pageClass, params);
        return get(url, pageClass);
    }

    /***
     * Loads content from given url onto an instance of pageClass.
     *
     * @param <T>       An instance of the class with a {@literal @}Page annotation
     * @param url   The url to load.
     * @param pageClass A class with a {@literal @}Selector annotation
     * @return The class instantiated and with the fields with the
     * {@literal @}Selector annotation populated.
     * @throws webGrude.http.GetException          When calling get on the BrowserClient raises an exception
     * @throws webGrude.mapping.elements.WrongTypeForField When a field have a type incompatible with the page html, example a <p>foo</p> on a float field
     * @throws TooManyResultsException    When a field maps to a type but the css selector returns more than one element
     */
    public <T> T get(final String url, final Class<T> pageClass) {
        final String pageContent = getPage(url);
        return webgrude.map(pageContent, pageClass);
    }
    /***
     * Loads content from request onto an instance of pageClass.
     *
     * @param <T>       An instance of the class with a {@literal @}Selector annotation
     * @param request   A {@link okhttp3.Request} that will be executed.
     * @param pageClass A class with a {@literal @}Selector annotation
     * @return The class instantiated and with the fields with the {@literal @}Selector annotation populated.
     * @throws webGrude.mapping.elements.WrongTypeForField When a field have a type incompatible with the page html, example a <p>foo</p> on a float field
     * @throws TooManyResultsException    When a field maps to a type but the css selector returns more than one element
     * @throws IOException    If something goes wrong fetching the page.
     */
    public <T> T execute(Request request, final Class<T> pageClass) throws IOException {
        try (Response response = client().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unsuccessful response " + response.code());
            }
            if (response.body() == null) {
                throw new IOException("Empty body");
            }
            return webgrude.map(response.body().string(), pageClass);
        }
    }

    private static <T> void cryIfNotAnnotated(final Class<T> pageClass) {
        if (!pageClass.isAnnotationPresent(Page.class)) {
            throw new RuntimeException("To be mapped from a page, the class must be annotated  @" + Page.class.getSimpleName());
        }
    }

    private OkHttpClient client(){
        if (client == null) {
            client = new OkHttpClient();
        }
        return client;
    }

    /***
     * Returns the page from the url.
     * @param url to do a get request
     * @return the get body response
     */
    public String getPage(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client().newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            throw new GetException(e, url);
        }
        throw new GetException("Could not get page " + url);
    }

    @Override
    public String visitLink(String href) {
        return getPage(href);
    }
}
