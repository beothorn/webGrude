WebGrude
=========

  WebGrude is a java library for mapping a html to a java class through annotations with css selectors.  
  It is suited for scraping pages that are generated server side and have a repeating structure.  

  To use it add the @Page annotation on a class and annotate each field corresponding to a website value with a css selector, then call Browser.get to intantiate it.  
  
  You can write the url from where the values should be loaded from on the @Page annotation or call Browser.get passing the url as the first parameter. Also, it is possible to use tokens on your url and pass parameters to replace these tokens on Browser.get.  
  
  You can also use regex to format the scraped value to the field type.  

  Webgrude tries to cast the values from the html to the field types. 
The supported types are:
- String : By default, the html text value is used on the field. It is possible to use an attribute value by passing an attr parameter to the @Selector annotation. 'html' and 'outerHtml' can also be used as attr value.
- int
- float
- boolean
- Date : Use it with assigning the date format on the format field of the Selector annotation  
- List<> : Can be a list of any supported type or a list of a class annotated with @Selector 
- webGrude.elements.Link<>  : A Link must be loaded from a tag containing a href attribute. Link has a method visit, which loads and returns an instance of the declared generic type.
- org.jsoup.nodes.Element : See http://jsoup.org/apidocs/org/jsoup/nodes/Element.html

  If a value can't be casted an WrongTypeForField is thrown. This can be usefull when writing automated test that expects a certain value on a generated page.

Maven dependency
=========

```xml
<dependency>
  <groupId>com.github.beothorn</groupId>
  <artifactId>webGrude</artifactId>
  <version>2.0.0</version>
</dependency>
```

Examples
=========
Hacker news first page articles  
```java
@Page("https://news.ycombinator.com/")
public class HackerNews {
	@Selector(".storylink") public List<String> newsTitles;
}

//Usage

Browser.get(HackerNews.class).newsTitles.forEach(System.out::println);

```

hackaday blog posts  
```java
import java.util.Date;
import java.util.List;

import webGrude.Browser;
import webGrude.annotations.Page;
import webGrude.annotations.Selector;

@Page("http://hackaday.com/blog/") public class Hackaday {
    @Selector("article") static class Post{
        @Selector(".entry-title") String title;
        @Selector(value=".comments-counts",format="([0-9]*) Comments", defValue="0") int commentsCount;//using regex to extract number
        @Selector(value = ".entry-date a", format ="MMMM dd, yyyy - hh:mm a", attr = "title", locale = "en-US") Date date;//using date format
        @Override public String toString() {return title+" : "+date+" , "+commentsCount+" comments";}
    }
    List<Post> posts;
    public static void main(final String[] args) {
        final Hackaday hackaday = Browser.get(Hackaday.class);
        hackaday.posts.forEach(System.out::println);
    }
}

```

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
 Check the unit tests at webGrude.BrowserTest to see all the features.  

Useful links
=========

A blog post with a more in depth example on how to use webgrude  
    - http://www.isageek.com.br/2014/06/web-scraping-on-java-with-webgrude.html  
Reference on jsoup Selector  
    - http://jsoup.org/apidocs/org/jsoup/select/Selector.html   
Try jsoup online   
    - http://try.jsoup.org/
