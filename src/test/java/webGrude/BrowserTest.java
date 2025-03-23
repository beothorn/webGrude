package webGrude;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import webGrude.elements.WrongTypeForField;
import webGrude.http.BrowserClient;
import webGrude.http.GetException;
import webGrude.mappables.Foo;
import webGrude.mappables.TooManyResultsError;
import webGrude.mappables.WrongTypeError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BrowserTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String body = Files.readString(Paths.get("src/test/resources/__files/Foo.html"));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/html")
                .setBody(body));
    }

    @AfterEach
    public void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testMappingFromResource() {
        String url = mockWebServer.url("/foo").toString();
        final Foo foo = Browser.get(url, Foo.class);

        assertEquals("Title", foo.someContent.title);
        assertEquals("Lorem ipsum", foo.someContent.text);

        assertEquals("Nested content Title", foo.someNestedContent.getHeader());
        assertEquals("Nested content", foo.someNestedContent.content);

        assertEquals(2, foo.section.someRepeatingContent.size());
        assertEquals("bar baz", foo.section.someRepeatingContent.get(0));
        assertEquals("bar2 baz2", foo.section.someRepeatingContent.get(1));

        assertEquals("<p>Get content as <br>\n  element</p>", foo.htmlContent.html());

        assertEquals("<a href=\"linkToBeExtracted1\">Some useless text</a> <a href=\"linkToBeExtracted2\">Some useless text</a>", foo.linksInnerHtml);
        assertEquals("<a href=\"./page2\">link to next page</a>", foo.linksOuterHtml);

        assertEquals("linkToBeExtracted1", foo.linksWithHref.get(0));
        assertEquals("linkToBeExtracted2", foo.linksWithHref.get(1));

        assertEquals(url + "/./page2", foo.nextPage.getLinkUrl());

        assertEquals("www.example.com", foo.linkList.get(0).getLinkUrl());
        assertEquals(url + "/./page3", foo.linkList.get(1).getLinkUrl());

        assertEquals("HEAD1", foo.repeatingContentsNoSurroundingTag.get(0).head);
        assertEquals("TAIL1", foo.repeatingContentsNoSurroundingTag.get(0).tail);
        assertEquals("HEAD2", foo.repeatingContentsNoSurroundingTag.get(1).head);
        assertEquals("TAIL2", foo.repeatingContentsNoSurroundingTag.get(1).tail);

        assertEquals(0, foo.doesNotExist.size());

        assertEquals("[a, b]", foo.multiSelector.toString());

        assertEquals(42, foo.getIntValue());
        assertEquals(42, foo.getIntValueWithRegex());
        assertEquals(0, foo.intRegexWithDefault);
        assertEquals(42.24, foo.getFloatValue(), 0.001);
        assertEquals(42.24, foo.getFloatEuropeanValue(), 0.001);
        assertEquals(3.1415, foo.fHref, 0.00001);
        assertEquals(3.1415, foo.fHrefRegex, 0.00001);
        final SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy - h:mm a", Locale.US);
        assertEquals("September 23, 2016 - 1:00 PM", df.format(foo.date));
        assertTrue(foo.getBoolValue());

        assertEquals(41, foo.afterLoadValue);
    }

    @Test
    public void testUrlSubstitution() {
        Browser.setWebClient(new BrowserClient() {
            @Override
            public String get(final String url) {
                return "DUMMY";
            }

            @Override
            public String post(final String post, final Map<String, String>... params) {
                return "DUMMY";
            }
        });
        Browser.get(PageWithParameterizedURL.class, "x", "y");
        assertEquals("http://www.foo.com/x/bar/y/baz", Browser.getCurentUrl());
    }

    @Test
    public void testUriInvalidFormat() {
        assertThrows(GetException.class, () -> Browser.get("jnnkljbnkjb", Foo.class));
    }

    @Test
    public void testUriNotAccessible() {
        assertThrows(GetException.class, () -> Browser.get("www.thisurldoesnotexis.bla.blabla", Foo.class));
    }

    @Test
    public void tooManyResults() {
        String url = mockWebServer.url("/foo").toString();
        assertThrows(TooManyResultsException.class, () -> Browser.get(url, TooManyResultsError.class));
    }

    @Test
    public void testWrongType() {
        String url = mockWebServer.url("/foo").toString();
        assertThrows(WrongTypeForField.class, () -> Browser.get(url, WrongTypeError.class));
    }
}
