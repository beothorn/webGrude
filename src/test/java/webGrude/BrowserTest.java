package webGrude;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import webGrude.elements.WrongTypeForField;
import webGrude.http.BrowserClient;
import webGrude.http.GetException;
import webGrude.mappables.Foo;
import webGrude.mappables.TooManyResultsError;
import webGrude.mappables.WrongTypeError;

public class BrowserTest {

    @Test
    public void testMappingFromResource(){

        final String fooUrl = Foo.class.getResource("Foo.html").toString();
        final Foo foo = Browser.get(fooUrl,Foo.class);

        assertEquals("Title",foo.someContent.title);
        assertEquals("Lorem ipsum",foo.someContent.text);

        assertEquals("Nested content Title",foo.someNestedContent.getHeader());
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

        assertEquals(42,foo.getIntValue());
        assertEquals(42.24,foo.getFloatValue(),0.001);
        assertEquals(3.1415,foo.fHref,0.00001);
        assertTrue(foo.getBoolValue());

        assertEquals(41,foo.afterLoadValue);
    }

    @Test
    public void testUrlSubstitution(){
        Browser.setWebClient(new BrowserClient(){
            public String get(String url){return "DUMMY";}
        });
        Browser.get(PageWithParameterizedURL.class,"x","y");
        assertEquals("http://www.foo.com/x/bar/y/baz",Browser.getCurentUrl());
    }
    
    @Test
    public void testUriInvalidFormat(){
    	try{
    		Browser.get("jnnkljbnkjb",Foo.class);
    		Assert.fail("Should have thrown GetException");
    	}catch(GetException e){}
    }
    
    @Test
    public void testUriNotAccessible(){
    	try{
    		Browser.get("www.thisurldoesnotexis.bla.blabla",Foo.class);
    		Assert.fail("Should have thrown GetException");
    	}catch(GetException e){}
    }
    
    @Test
    public void tooManyResults(){
    	try{
    		final String fooUrl = Foo.class.getResource("Foo.html").toString();
            Browser.get(fooUrl,TooManyResultsError.class);
    		Assert.fail("Should have thrown GetException");
    	}catch(TooManyResultsException e){}
    }
    
    @Test
    public void testWrongType(){
    	try{
    		final String fooUrl = Foo.class.getResource("Foo.html").toString();
            WrongTypeError wrongTypeError = Browser.get(fooUrl,WrongTypeError.class);
    		Assert.fail("Should have thrown GetException, and wrong value is "+wrongTypeError.badFloat);
    	}catch(WrongTypeForField e){}
    }

}