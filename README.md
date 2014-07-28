WebGrude
=========

WebGrude is a java library for mapping a html to a java class through annotations with css selectors.  

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

For more complex use cases check see src/test/java/webGrude/BrowserTest.java

Maven dependency
=========

```xml
<dependency>
  <groupId>com.github.beothorn</groupId>
  <artifactId>webGrude</artifactId>
  <version>1.0.3</version>
</dependency>
```

Useful links
=========

A blog post with a more in depth example on how to use webgrude  
    - http://www.isageek.com.br/2014/06/web-scraping-on-java-with-webgrude.html  
Reference on jsoup Selector  
    - http://jsoup.org/apidocs/org/jsoup/select/Selector.html   
Try jsoup online   
    - http://try.jsoup.org/
