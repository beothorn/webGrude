package webGrude;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import webGrude.http.BrowserClient;

import java.io.IOException;

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

        assertEquals("HEAD1",foo.repeatingContentsNoSurroundingTag.get(0).head);
        assertEquals("TAIL1",foo.repeatingContentsNoSurroundingTag.get(0).tail);
        assertEquals("HEAD2",foo.repeatingContentsNoSurroundingTag.get(1).head);
        assertEquals("TAIL2",foo.repeatingContentsNoSurroundingTag.get(1).tail);

        assertEquals(0,foo.doesNotExist.size());

        assertEquals(42,foo.i);
        assertEquals(42.24,foo.f,0.001);
        assertEquals(3.1415,foo.fHref,0.00001);
        assertTrue(foo.b);

        assertEquals(41,foo.afterLoadValue);
    }

    @Test
    public void testUrlSubstitution(){
        Browser.setWebClient(new BrowserClient(){
            public String get(String get){return "DUMMY";}
        });
        Browser.open(PageWithParameterizedURL.class,"x","y");
        assertEquals("http://www.foo.com/x/bar/y/baz",Browser.getCurentUrl());
    }

}