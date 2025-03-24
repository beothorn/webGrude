package webGrude;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import webGrude.annotations.Page;
import webGrude.http.LinkVisitor;

import java.io.IOException;

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
public class OkHttpBrowser implements LinkVisitor {

    private OkHttpClient client;
    private final Webgrude webgrude = new Webgrude();

    /**
     * Loads content from url from the Page annotation on pageClass onto an instance of pageClass.
     *
     * @param <T>       An instance of the class with a {@literal @}Page annotantion
     * @param pageClass A class with a {@literal @}Page annotantion
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
     * @param <T>       An instance of the class with a {@literal @}Page annotantion
     * @param url   The url to load.
     * @param pageClass A class with a {@literal @}Page annotantion
     * @return The class instantiated and with the fields with the
     * {@literal @}Selector annotation populated.
     * @throws webGrude.http.GetException          When calling get on the BrowserClient raises an exception
     * @throws webGrude.elements.WrongTypeForField When a field have a type incompatible with the page html, example a <p>foo</p> on a float field
     * @throws webGrude.TooManyResultsException    When a field maps to a type but the css selector returns more than one element
     */
    public <T> T get(final String url, final Class<T> pageClass) {
        final String pageContent = getPage(url);
        return webgrude.map(pageContent, pageClass);
    }

    public <T> T execute(Request request, final Class<T> pageClass) throws IOException {
        try (Response response = client().newCall(request).execute()) {
            if (response.isSuccessful()) {
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

    public String getPage(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client().newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Could not get page");
    }

    @Override
    public String visitLink(String href) {
        return getPage(href);
    }
}
