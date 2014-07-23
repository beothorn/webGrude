package webGrude;

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

    @Selector("#some-content") static public class SomeContent {
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

    @Selector(value = "#numberOnAnAttribute", attr = "href") public float fHref;

    public SomeContent someContent;
    public SomeNestedContent someNestedContent;
    public Section section;

    public int afterLoadValue;

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
