package webGrude.mappables;

import java.util.Date;
import java.util.List;

import org.jsoup.nodes.Element;

import webGrude.mapping.annotations.AfterPageLoad;
import webGrude.mapping.annotations.Page;
import webGrude.mapping.annotations.Selector;
import webGrude.mapping.elements.Link;

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
        @Selector("h1")  private String header;
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
    @Selector(".multiselector,.multiselector2")        public List<String> multiSelector;
    @Selector(".some-repeating-content-outside-a-tag") public List<SomeRepeatingContent> repeatingContentsNoSurroundingTag;

    @Selector("#float")   private float floatValue;
    @Selector(value = "#float-european", locale = "fr_FR")   private float floatEuropeanValue;
    @Selector("#integer") private int intValueWithRegex;
    @Selector("#boolean") private boolean boolValue;

    @Selector(value ="#integer-from-string-with-regex", format = "([0-9]*) comments") private int intValue;
    @Selector(value ="#integer-from-string-with-regex-with-default", format = "([0-9]*) comments", defValue="0") public int intRegexWithDefault;
    @Selector(value = "#numberOnAnAttributeRegex", attr = "href", format = "pi is ([0-9\\.]*)") public float fHrefRegex;


    @Selector(value="#links",attr="html") public String linksInnerHtml;
    @Selector(value="p>a",attr="outerHtml") public String linksOuterHtml;

    @Selector(value = "#numberOnAnAttribute", attr = "href") public float fHref;

    @Selector(value = "#date", format ="MMMM dd, yyyy - h:mm a", attr = "title", locale = "en_US") public Date date;

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

    public float getFloatEuropeanValue() {
    	return floatEuropeanValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public int getIntValueWithRegex() {
        return intValueWithRegex;
    }

    public boolean getBoolValue() {
        return boolValue;
    }

}
