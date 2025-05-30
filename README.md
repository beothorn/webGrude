WebGrude
=========

WebGrude is a java library for mapping a html or xml to a java class through annotations.  

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

To use it annotate fields with @Selector and they will be populated with the text value from the html matching the 
selector.  
You can write the url from where the values should be loaded from on a @Page annotation or call OkHttpBrowser.get 
passing the url as the first parameter.  
@Page can also have an url template and parameters.
```java
@Page("http://localhost:{0}/{1}/bar/{2}/baz")
public class SomePage
// Usage
new OkHttpBrowser().get(SomePage.class, "john", "1")
```

You can also use regex to select a part of the scraped value to fill the field.  
See the hackaday example below.  

Webgrude tries to cast the values from the html to the field types. 
The supported types are:
- String : By default, the html text value is used on the field. It is possible to use an attribute value by passing an 
attr parameter to the @Selector annotation. 'html' and 'outerHtml' can also be used as attr value.
- int
- float
- boolean
- Date : Assign the date format on the format field of the Selector annotation (See example below)  
- List<> : Can be a list of any supported type or a list of a class annotated with @Selector 
- Link<ClassWithSelectors>  : A Link must be loaded from a tag containing a href attribute. Link has a method visit, which loads and 
returns an instance of the declared generic type.
- List<Link<ClassWithSelectors>> : A list of links is also possible.
- org.jsoup.nodes.Element : See http://jsoup.org/apidocs/org/jsoup/nodes/Element.html

If a value can't be casted an WrongTypeForField is thrown. This can be useful when writing automated test that expects 
a certain type value on a generated page.

Maven dependency
=========

```xml
<dependency>
  <groupId>com.github.beothorn</groupId>
  <artifactId>webGrude</artifactId>
  <version>5.0.0</version>
</dependency>
```

Examples
=========

## Hackaday blog posts

```java
import java.util.Date;
import java.util.List;

import webGrude.OkHttpBrowser;
import webGrude.mapping.annotations.Page;
import webGrude.mapping.annotations.Selector;

@Page("http://hackaday.com/blog/")
public class Hackaday {
    static class Post {
        @Selector(".entry-title") String title;
        //using regex to extract number
        @Selector(value = ".comments-counts", format = "([0-9]*) Comments", defValue = "0") int commentsCount;
        //using date format
        @Selector(value = ".entry-date a", format = "MMMM dd, yyyy - hh:mm a", attr = "title", locale = "en_US") Date date;

        @Override
        public String toString() {
            return title + " : " + date + " , " + commentsCount + " comments";
        }
    }

    @Selector("article") List<Post> posts;

    public static void main(final String[] args) {
        final Hackaday hackaday = new OkHttpBrowser().get(Hackaday.class);
        hackaday.posts.forEach(System.out::println);
    }
}
```

## Xml read

```xml
<aaa>
    <ab>
        Test
    </ab>
    <ac>
        Another test
    </ac>
    <a1>
        A1test
    </a1>
    <aNested>
        <content>
            Nested content
        </content>
        <item>a</item>
        <item>b</item>
        <item>c</item>
    </aNested>

</aaa>
```

```java
import webGrude.mapping.annotations.XPath;
import webGrude.mapping.annotations.XML;

@XML
public class Bar {
    @XPath("/aaa/ab") private String ab;
    @XPath("/aaa/ac") private String ac;
    @XPath("/aaa/a1") private String a1;
    public static class NestedContent {
        @XPath("/content") private String content;
        @XPath("/item") private List<String> items;
    }
    @XPath("/aaa/aNested") NestedContent content;
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
