package webGrude.examples;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;
import webGrude.Webgrude;
import webGrude.annotations.Selector;
import webGrude.elements.Link;

import java.io.IOException;
import java.util.List;

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