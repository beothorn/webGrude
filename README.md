webGrude
=========

WebGrude is a java library for Web scraping.  
It uses annotations to map a page to a pojo.  
Example:
Page foo.com:
```html
<html>
<body>
	<div id="some-content">
		<h1>Title</h1>
		<div>Lorem ipsum</div>
	</div>
  
	<div class="section">
		<span class="some-repeating-content">
			bar baz
		</span>
		<span class="some-repeating-content">
			bar2 baz2
		</span>
	</div>
	
	<p>
		<a href="">link to next page</a>
	</p>
</body>
</html>
```
Java Code:
```java

import static webGrude.Browser.open;

import java.util.List;

import webGrude.annotations.PageURL;
import webGrude.annotations.Selector;
import webGrude.elements.Link;

@PageURL("http://foo.com/")
public class Foo {
	@Selector("#some-content") static public class SomeContent {
		@Selector("h1") public String title;
		@Selector("div") public String text;
	}

	@Selector("#section") static public class Section {
		@Selector(".some-repeating-content") public List<String> someRepeatingContent;
	}
	
	@Selector("p a") public Link<Foo> nextPage;
	public SomeContent someContent;
	public Section section;
	
	public static void main(final String[] args) {
		final Foo foo = open(Foo.class);
		
		System.out.println(foo.someContent.title);
		System.out.println(foo.someContent.text);
		
		for (final String content : foo.section.someRepeatingContent) {
			System.out.println(content);
		}
		
		Foo nextPage = foo.nextPage.visit();

	}
}
```
