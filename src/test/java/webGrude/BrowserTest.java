package webGrude;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
		
	}
	
}