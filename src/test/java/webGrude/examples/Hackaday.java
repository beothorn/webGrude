package webGrude.examples;

import webGrude.OkHttpBrowser;
import webGrude.mapping.annotations.Page;
import webGrude.mapping.annotations.Selector;

import java.util.Date;
import java.util.List;

@Page("http://hackaday.com/blog/")
public class Hackaday {

    static class Post {
        @Selector(".entry-title")
        String title;
        @Selector(value = ".comments-counts", format = "([0-9]*) Comments", defValue = "0")
        int commentsCount;//using regex to extract number
        @Selector(value = ".entry-date a", format = "MMMM dd, yyyy - hh:mm a", attr = "title", locale = "en_US")
        Date date;//using date format

        String unusedField;

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