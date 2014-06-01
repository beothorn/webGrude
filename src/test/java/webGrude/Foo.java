package webGrude;

import java.util.List;

import org.jsoup.nodes.Element;

import webGrude.annotations.Page;
import webGrude.annotations.Selector;
import webGrude.elements.Link;

@Page
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
    @Selector("p a") public Link<Foo> nextPage;
    @Selector(value = "#links a",attr = "href") public List<String> linksWithHref;
    @Selector(value = "#linkList a",attr = "href") public List<Link<Foo>> linkList;
    @Selector(".doesNotExist") public List<String> doesNotExist;

    @Selector("#float") public float f;
    @Selector("#integer") public int i;
    @Selector("#boolean") public boolean b;

    @Selector(value = "#numberOnAnAttribute", attr = "href") public float fHref;

    public SomeContent someContent;
    public SomeNestedContent someNestedContent;
    public Section section;

}
