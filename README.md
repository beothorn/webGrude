WebGrude
=========

  WebGrude is a java library for mapping a html to a java class through annotations with css selectors.  

  To use it add the @Page annotation on a class and annotate each field corresponding to a website value with a css selector,  
then call Browser.get to intantiate it.  
  You can write the url from where the values should be loaded from on the @Page annotation or call Browser.get passing  
the url as the first parameter. Also, it is possible to use tokens on your url and pass parameters to replace these tokens on Browser.get.  

  Webgrude tries to cast the values from the html to the field types. 
The supported types are:
- String
- int
- float
- boolean
- List<> : Can be a list of any supported type or a list of a class annotated with @Selector 
- webGrude.elements.Link<>  : A Link must be loaded from a tag containing a href attribute. Link has a method visit, which loads and returns an instance of the declared generic type.
- org.jsoup.nodes.Element : See http://jsoup.org/apidocs/org/jsoup/nodes/Element.html


Maven dependency
=========

```xml
<dependency>
  <groupId>com.github.beothorn</groupId>
  <artifactId>webGrude</artifactId>
  <version>1.0.3</version>
</dependency>
```

Examples
=========

This is a pirate bay search that prints the resulting magnet links:

```java
package www;

import webGrude.Browser;
import webGrude.annotations.Page;
import webGrude.annotations.Selector;
import webGrude.elements.Link;

import java.util.List;

@Page("http://thepiratebay.se/search/{0}/0/7/0")
public class PirateBay {

    public static void main(String[] args) {
        //Search calls Browser, which loads the page on a PirateBay instance
        PirateBay search = PirateBay.search("ubuntu");
        while(search!=null){
        	search.magnets.forEach(System.out::println);
        	search = search.nextPage();
        }
    }

    public static PirateBay search(String term){
        return Browser.get(PirateBay.class, term);
    }

    private PirateBay(){}

    /*
    * This selector matches all magnet links. The result is added to this String list.
    * The default behaviour is to use the rendered html inside the matched tag, but here
    * we want to use the href value instead.
    */
    @Selector(value = "#searchResult tbody tr td a[href*=magnet]", attr = "href")
    public List<String> magnets;
    
    /*
    * This selector matches a link to the next page result, wich can be mapped to a PirateBay instance.
    * The Link next gets the page on the href attribute of the link when method visit is called.
    */
    @Selector("a:has(img[alt=Next])")
    private Link<PirateBay> next;
    
    public PirateBay nextPage(){
    	if(next == null)
    		return null;
    	return next.visit();
    }
}
```

A more advanced example using inner classes to map some repeating contents and type casting:  

The html source:  

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

	<p>
		<a href="./page2">link to next page</a>
	</p>

	<div id="html-content">
		<p>
			Get content as <br> 
			element
		</p>
	</div>

    <div id="links">
        <a href="linkToBeExtracted1">Some useless text</a>
        <a href="linkToBeExtracted2">Some useless text</a>
    </div>

    <div id="linkList">
        <a href="www.example.com">A link to a mapped page</a>
        <a href="./page3">Another link to a mapped page</a>
    </div>


    <div id="integer">
        42
    </div>

    <div id="float">
        42.24
    </div>

    <a id="numberOnAnAttribute" href="3.1415">Number on an attribute</a>

    <div id="boolean">
        true
    </div>


    <span class="some-repeating-content-outside-a-tag">
        <span class="head">HEAD1</span>
        <span class="tail">TAIL1</span>
	</span>
	<span class="some-repeating-content-outside-a-tag">
	    <span class="head">HEAD2</span>
        <span class="tail">TAIL2</span>
	</span>

</body>
</html>
```

The java class corresponding to the source html:  

```java
package webGrude.mappables;

import java.util.List;

import org.jsoup.nodes.Element;

import webGrude.annotations.AfterPageLoad;
import webGrude.annotations.Page;
import webGrude.annotations.Selector;
import webGrude.elements.Link;

@Page
public class Foo {

    @Selector("")
    static public class SomeRepeatingContent {
        @Selector(".head") public String head;
        @Selector(".tail") public String tail;
    }

    @Selector("#some-content") 
    static public class SomeContent {
            @Selector("h1")  public String title;
            @Selector("div") public String text;
    }

    @Selector("#some-nested-content") static public class SomeNestedContent {
            @Selector("h1")   private String header;
            @Selector("span") public  String content;
			public String getHeader() {
				return header;
			}
    }

    @Selector("#section") static public class Section {
            @Selector(".some-repeating-content") public List<String> someRepeatingContent;
            public SomeNestedContent someNestedContent;
    }

    @Selector(value = "#links a",     attr = "href")   public List<String> linksWithHref;
    @Selector(value = "#linkList a",  attr = "href")   public List<Link<Foo>> linkList;
    @Selector("#html-content")                         public Element htmlContent;
    @Selector("p a")                                   public Link<Foo> nextPage;
    @Selector(".doesNotExist")                         public List<String> doesNotExist;
    @Selector(".some-repeating-content-outside-a-tag") public List<SomeRepeatingContent> repeatingContentsNoSurroundingTag;

    @Selector("#float")   private float floatValue;
    @Selector("#integer") private int intValue;
    @Selector("#boolean") private boolean boolValue;
    
    @Selector(value="#links",attr="html") public String linksInnerHtml;
    @Selector(value="p>a",attr="outerHtml") public String linksOuterHtml;

    @Selector(value = "#numberOnAnAttribute", attr = "href") public float fHref;

    public SomeContent someContent;
    public SomeNestedContent someNestedContent;
    public Section section;

    public int afterLoadValue;
    
    private Foo(){}

    @AfterPageLoad
    public void copyIntegerMinusOne(){
        afterLoadValue = getIntValue() - 1;
    }

	public float getFloatValue() {
		return floatValue;
	}
	public int getIntValue() {
		return intValue;
	}

	public boolean getBoolValue() {
		return boolValue;
	}

}
```

Useful links
=========

A blog post with a more in depth example on how to use webgrude  
    - http://www.isageek.com.br/2014/06/web-scraping-on-java-with-webgrude.html  
Reference on jsoup Selector  
    - http://jsoup.org/apidocs/org/jsoup/select/Selector.html   
Try jsoup online   
    - http://try.jsoup.org/
