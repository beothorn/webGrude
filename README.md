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
		<a href="">a link to another page</a>
	</p>
</body>
</html>
```
Pojo:
```java

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
		@Selector(".some-repeating-content") public List<String> someContent;
	}
	
	@Selector("p a") public Link<Foo> nextPage;
	public SomeContent someContent;
	public Section section;
}
```
