WebGrude
=========

WebGrude is a java library for mapping a html to a java class through annotations with css selectors.  
For example, this is a pirate bay search that prints the resulting magnet links:

```java
package www;

import webGrude.Browser;
import webGrude.annotations.Page;
import webGrude.annotations.Selector;
import webGrude.elements.Link;

import java.util.List;

@Page("http://thepiratebay.se/search/{0}/0/7/0")
public class PirateBay {

    public static PirateBay search(String term){
        return Browser.get(PirateBay.class, term);
    }

    private PirateBay(){}

    @Selector(value = "#searchResult tbody tr td a[href*=magnet]", attr = "href")
    public List<String> magnets;
    
    @Selector("a:has(img[alt=Next])")
    private Link<PirateBay> next;
    
    public PirateBay nextPage(){
    	if(next == null)
    		return null;
    	return next.visit();
    }
    
    public static void main(String[] args) {
        PirateBay search = PirateBay.search("ubuntu");
        while(search!=null){
        	search.magnets.forEach(System.out::println);
        	search = search.nextPage();
        }
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
  <version>1.0.2</version>
</dependency>
```

Resources
=========

Reference on jsoup Selector http://jsoup.org/apidocs/org/jsoup/select/Selector.html  
Try jsoup online http://try.jsoup.org/
