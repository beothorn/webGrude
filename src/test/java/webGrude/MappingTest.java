package webGrude;

import org.junit.jupiter.api.Test;
import webGrude.mapping.TooManyResultsException;
import webGrude.mapping.elements.WrongTypeForField;
import webGrude.mappables.Foo;
import webGrude.mappables.TooManyResultsError;
import webGrude.mappables.WrongTypeError;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class MappingTest {

    final String fooPageContents = TestUtils.readTestResource("Foo.html");
    final Webgrude pageToClassMapper = new Webgrude(true);

    @Test
    public void testMappingFromResource() {
        final String url = "http://www.isageek.com.br";
        final Foo foo =  pageToClassMapper.map(fooPageContents, Foo.class, url);

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
    public void testVisitLink() {
        final String url = "http://www.isageek.com.br";
        final Foo foo =  pageToClassMapper.map(fooPageContents, Foo.class, url);

        final AtomicReference<String> linkReceived = new AtomicReference<>();
        Foo fooPage2 = foo.nextPage.visit(link -> {
            linkReceived.set(link);
            return fooPageContents;
        });

        assertEquals("Title", fooPage2.someContent.title);
        assertEquals("http://www.isageek.com.br/./page2", linkReceived.get());
    }

    @Test
    public void testUrlSubstitution() {
        Webgrude pageToClassMapper = new Webgrude();
        final String url =  pageToClassMapper.url(PageWithParameterizedURL.class, "8080", "x", "y");

        assertEquals("http://localhost:8080/x/bar/y/baz", url);
    }

    @Test
    public void tooManyResults() {
        assertThrows(TooManyResultsException.class, () ->  pageToClassMapper.map(fooPageContents, TooManyResultsError.class));
    }

    @Test
    public void testWrongType() {
        assertThrows(WrongTypeForField.class, () ->  pageToClassMapper.map(fooPageContents, WrongTypeError.class));
    }

}
