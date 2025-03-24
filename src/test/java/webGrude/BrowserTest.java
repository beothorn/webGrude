package webGrude;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import webGrude.mappables.Foo;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrowserTest {

    private MockWebServer mockWebServer;
    private OkHttpBrowser okHttpBrowser = new OkHttpBrowser();

    @BeforeEach
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        final String body = MappingTest.readTestResource("Foo.html");

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
        final Foo foo = okHttpBrowser.get(url, Foo.class);

        assertEquals("Title", foo.someContent.title);
    }

    @Test
    public void testUrlSubstitution() throws InterruptedException {
        okHttpBrowser.get(PageWithParameterizedURL.class, Integer.toString(mockWebServer.getPort()), "x", "y");
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/x/bar/y/baz", recordedRequest.getPath());
    }
}
