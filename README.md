WebGrude
=========

WebGrude is a java library for Web scraping.  
It uses annotations to map a page to a pojo with jsoup and httpComponents.   

Example
=========

Page foo.com:
```html
<html>
<body>
	<div id="some-content">
		<h1>Title</h1>
		<div>Lorem ipsum</div>
	</div>
  
	<div id="section">
		<span class="some-repeating-content">
			bar baz
		</span>
		<span class="some-repeating-content">
			bar2 baz2
		</span>
		<div id="some-nested-content">
			<h1>Nested content Title</h1>
			<span>Nested content</span>
		</div>
	</div>
	
	<div id="html-content">
		<p>
			Get content as <br> 
			element
		</p>
	</div>
	
	<a href="nextPage.html" class="next"> next </a>
</body>
</html>
```
Java Code:
```java

import java.util.List;

import org.jsoup.nodes.Element;

import webGrude.annotations.Page;
import webGrude.annotations.Selector;
import webGrude.elements.Link;

@Page("http://www.foo.com.br")
public class Foo {
	@Selector("#some-content") static public class SomeContent {
		@Selector("h1") public String title;
		@Selector("div") public String text;
	}
	
	@Selector("#some-nested-content") static public class SomeNestedContent {
		@Selector("h1") public String header;
		@Selector("span") public String content;
	}

	@Selector("#section") static public class Section {
		@Selector(".some-repeating-content") public List<String> someRepeatingContent;
		public SomeNestedContent someNestedContent;
	}
	
	@Selector("#html-content") public Element htmlContent;
	@Selector(".next") public Link<Foo> nextPage;
	
	public SomeContent someContent;
	public SomeNestedContent someNestedContent;
	public Section section;
	
	public static void main(final String[] args) {
		Foo foo = Browser.open(Foo.class);
		System.out.println(foo.someContent.title);
		System.out.println(foo.someContent.text);
		
		System.out.println(foo.someNestedContent.header);
		System.out.println(foo.someNestedContent.content);
		
		System.out.println(foo.section.someRepeatingContent.get(0));
		System.out.println(foo.section.someRepeatingContent.get(1));
		
		System.out.println(foo.htmlContent.html());
		foo = foo.nextPage.visit();//Follow link
	}
}

```
