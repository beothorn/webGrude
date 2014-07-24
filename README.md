WebGrude
=========

WebGrude is a java library for mapping a html to a java class through annotations with css selectors.  
For example, this is a pirate bay search that prints the resulting magnet links:

```java
package webGrude;

import webGrude.annotations.Page;
import webGrude.annotations.Selector;

import java.util.List;

public class PirateBay {

    @Page("http://thepiratebay.se/search/{0}/0/7/0")
    public static class SearchResult {
        @Selector(value = "#searchResult tbody tr td a[href*=magnet]", attr = "href")
        public List<String> magnets;
    }

    public static void main(String... args){
        Browser
            .get(SearchResult.class, "ubuntu iso")
            .magnets
            .forEach(System.out::println);
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
  <version>1.0.1</version>
</dependency>
```

Resources
=========

Reference on jsoup Selector http://jsoup.org/apidocs/org/jsoup/select/Selector.html  
Try jsoup online http://try.jsoup.org/
