package webGrude.examples;

import webGrude.OkHttpBrowser;
import webGrude.annotations.Page;
import webGrude.annotations.Selector;
import webGrude.elements.Link;

import java.util.List;

@Page("https://news.ycombinator.com/")
public class HackerNews {
	@Selector(".storylink") public List<String> newsTitles;
    @Selector(".moreLink") public Link<HackerNews> nextPage;
    @Selector("span.titleline > a:nth-child(1)") public List<Link<HackerNews>> articleLinks;

    public static void main(String[] args) {
        OkHttpBrowser browser = new OkHttpBrowser();
        HackerNews hackerNews = browser.get(HackerNews.class);
        HackerNews secondPage = hackerNews.nextPage.visit(browser);
        secondPage.articleLinks.forEach(l -> System.out.println(l.getLinkUrl()));
    }
}