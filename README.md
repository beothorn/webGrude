WebGrude
=========

  WebGrude is a java library for mapping a html to a java class through annotations with css selectors.  

```java
@Page("https://news.ycombinator.com/")
public class HackerNews {
    @Selector(".storylink") public List<String> newsTitles;
    @Selector(".moreLink") public Link<HackerNews> nextPage;
    @Selector("span.titleline > a:nth-child(1)") public List<Link<HackerNews>> articleLinks;

    public static void main(String[] args) {
        OkHttpBrowser browser = new OkHttpBrowser();
        HackerNews hackerNews = browser.get(HackerNews.class);
        HackerNews secondPage = hackerNews.nextPage.visit(browser);
        // Prints hacker news second page article links
        secondPage.articleLinks.forEach(l -> System.out.println(l.getLinkUrl()));
    }
}
```

  It is suited for scraping pages that are generated server side and have a repeating structure with defined value types.  

  To use it add the @Page annotation on a class and annotate each field corresponding to a website value with a css selector, then call Browser.get to intantiate it.  
  
  You can write the url from where the values should be loaded from on the @Page annotation or call Browser.get passing the url as the first parameter. Also, it is possible to use tokens on your url and pass parameters to replace these tokens on Browser.get.  
  
  You can also use regex to format the scraped value to the field type.  

  Webgrude tries to cast the values from the html to the field types. 
The supported types are:
- String : By default, the html text value is used on the field. It is possible to use an attribute value by passing an attr parameter to the @Selector annotation. 'html' and 'outerHtml' can also be used as attr value.
- int
- float
- boolean
- Date : Assign the date format on the format field of the Selector annotation (See example below)  
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
  <version>3.0.0</version>
</dependency>
```

Examples
=========

## Hackaday blog posts

```java
import java.util.Date;
import java.util.List;

import webGrude.OkHttpBrowser;
import webGrude.annotations.Page;
import webGrude.annotations.Selector;

@Page("http://hackaday.com/blog/")
public class Hackaday {
    @Selector("article")
    static class Post {
        @Selector(".entry-title")
        String title;
        @Selector(value = ".comments-counts", format = "([0-9]*) Comments", defValue = "0")
        int commentsCount;//using regex to extract number
        @Selector(value = ".entry-date a", format = "MMMM dd, yyyy - hh:mm a", attr = "title", locale = "en_US")
        Date date;//using date format

        @Override
        public String toString() {
            return title + " : " + date + " , " + commentsCount + " comments";
        }
    }

    List<Post> posts;

    public static void main(final String[] args) {
        final Hackaday hackaday = new OkHttpBrowser().get(Hackaday.class);
        hackaday.posts.forEach(System.out::println);
    }
}
```

## Use mapper directly

If you need something more complex than a get request, Webgrude can map from html String to Object directly.  

```java
public class HackerNewsMyBrowser {
    @Selector(".storylink") public List<String> newsTitles;
    @Selector(".moreLink") public Link<HackerNews> nextPage;
    @Selector("span.titleline > a:nth-child(1)") public List<Link<HackerNews>> articleLinks;

    public static void main(String[] args) {
        final String url = "https://news.ycombinator.com/";
        String contents = getPage(url);
        Webgrude webgrude = new Webgrude();
        HackerNewsMyBrowser hn = webgrude.map(contents, HackerNewsMyBrowser.class, url);
        HackerNews secondPage = hn.nextPage.visit(HackerNewsMyBrowser::getPage);
        // Prints hacker news second page article links
        secondPage.articleLinks.forEach(l -> System.out.println(l.getLinkUrl()));
    }

    @Nullable
    private static String getPage(final String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        String contents = null;
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                contents = response.body().string();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return contents;
    }
}
```

Useful links
=========

Reference on jsoup Selector  
    - http://jsoup.org/apidocs/org/jsoup/select/Selector.html   
Try jsoup online   
    - http://try.jsoup.org/
