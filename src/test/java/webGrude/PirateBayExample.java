package webGrude;

import webGrude.annotations.Page;
import webGrude.annotations.Selector;

import java.util.List;

public class PirateBayExample {

    @Page("http://thepiratebay.se/search/{0}/0/7/0")
    public static class SearchResult {
        @Selector(value = "#searchResult tbody tr td a[href*=magnet]", attr = "href") public List<String> magnets;
    }

    public static void main(String... args){
        SearchResult tpb = Browser.open(SearchResult.class, "ubuntu iso");
        for (String magnetLink : tpb.magnets){
            System.out.println(magnetLink);
        }
    }

}