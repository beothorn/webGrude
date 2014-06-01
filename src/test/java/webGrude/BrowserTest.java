package webGrude;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import webGrude.http.SimpleHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BrowserTest {

    @Test
    public void test(){

        final String fooUrl = Foo.class.getResource("Foo.html").toString();
        final Foo foo = Browser.open(fooUrl,Foo.class);

        assertEquals("Title",foo.someContent.title);
        assertEquals("Lorem ipsum",foo.someContent.text);

        assertEquals("Nested content Title",foo.someNestedContent.header);
        assertEquals("Nested content",foo.someNestedContent.content);

        assertEquals(2,foo.section.someRepeatingContent.size());
        assertEquals("bar baz",foo.section.someRepeatingContent.get(0));
        assertEquals("bar2 baz2",foo.section.someRepeatingContent.get(1));

        assertEquals("<p> Get content as <br /> element </p>",foo.htmlContent.html());

        assertEquals("linkToBeExtracted1",foo.linksWithHref.get(0));
        assertEquals("linkToBeExtracted2",foo.linksWithHref.get(1));

        assertEquals(fooUrl+"/./page2",foo.nextPage.getLinkUrl());

        assertEquals("www.example.com",foo.linkList.get(0).getLinkUrl());
        assertEquals(fooUrl+"/./page3",foo.linkList.get(1).getLinkUrl());

        assertEquals(0,foo.doesNotExist.size());

        assertEquals(42,foo.i);
        assertEquals(42.24,foo.f,0.001);
        assertEquals(3.1415,foo.fHref,0.00001);
        assertTrue(foo.b);

    }

    @Test
    public void testUrlSubstitution(){

        final String fooUrl = Foo.class.getResource("Foo.html").toString();
        Browser.setWebClient(new SimpleHttpClient(){
            @Override public String getToFile(String link, File destFile) throws ClientProtocolException, IOException {return "DUMMY";}
            @Override public String getOrNull(String url) {return "DUMMY";}
            @Override public String post(String postUrl, Map<String, String> params) throws ClientProtocolException, IOException {return "DUMMY";}
            @Override public String post(String postUrl, String params) throws ClientProtocolException, IOException {return "DUMMY";}
            @Override public String get(String get) throws ClientProtocolException, IOException {return "DUMMY";}
            @Override public void close() {}
        });
        final PageWithParameterizedURL foo = Browser.open(PageWithParameterizedURL.class,"x","y");
        assertEquals("http://www.foo.com/x/bar/y/baz",Browser.getCurentUrl());
    }

}