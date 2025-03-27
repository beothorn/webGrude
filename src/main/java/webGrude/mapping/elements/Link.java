package webGrude.mapping.elements;

import org.jsoup.nodes.Element;

import webGrude.OkHttpBrowser;
import webGrude.http.LinkVisitor;
import webGrude.Webgrude;

/**
 * A Link for a Page that can be visited.
 * <p>
 * This class is supposed to be a type of a field on a class anottated with
 * {@literal @}Page. The field must be annotated with {@literal @}Selector ,
 * and the selector must resolve to a link.
 * <p>
 * A link is supposed to be a Link of a Page. For example a page is being mapped
 * and it has a link for www.example.com, wich is mapped by a ExamplePage class,
 * the field should look like this:<br>
 * <p>
 * <i>{@literal @}Selector("a.linksToExample") Link{@literal <}ExamplePage{@literal ></}ExamplePage{@literal >} linkToExample; </i>
 * </p>
 * After the webgrude Browser opens the page, the field linkToExample will
 * contain a link that can be visited:
 * <p>
 * <i> ExamplePage examplePage = linkToExample.visit(); </i>
 * </p>
 *
 * @param <T> The class populated by the link.
 * @see OkHttpBrowser
 * @see webGrude.mapping.annotations.Page
 * @see webGrude.mapping.annotations.Selector
 */
public class Link<T> {

    private final Class<T> type;
    private final Element hrefElement;
    private final String baseUrl;
    private final Webgrude pageToClassMapper;

    /***
     * A link uses maps a page from an url to an instance of a class annotated with <i>{@literal @}Page</i>.
     * when the method {@link Link#visit(LinkVisitor)} is called
     *
     * @param hrefElement  A Jsoup Element with a href attribute
     * @param visitingType The type that will be mapped from the page when the method {@link Link#visit(LinkVisitor)} is called
     * @param baseUrl      The base url, it is used to resolve relative links
     */
    public Link(
        final Webgrude pageToClassMapper,
        final Element hrefElement,
        final Class<T> visitingType,
        final String baseUrl
    ) {
        this.pageToClassMapper = pageToClassMapper;
        this.hrefElement = hrefElement;
        this.type = visitingType;
        this.baseUrl = baseUrl;
    }

    public String getLinkUrl() {
        final String href = this.hrefElement.attr("href");
        String urlToVisit = href;
        if (href.startsWith("/")) {
            final String rootPage = this.baseUrl.replaceAll("(.*://.*?/).*", "$1");
            urlToVisit = rootPage.substring(0, rootPage.length() - 1) + href;
        }
        if (href.startsWith(".")) {
            urlToVisit = this.baseUrl + "/" + href;
        }
        return urlToVisit;
    }

    /***
     * Visit a page link and map its values to an instance of the visitingType.
     *
     * @return an instance of visitingType
     */
    public T visit(final LinkVisitor linkVisitor) {
        final String linkUrl = this.getLinkUrl();
        final boolean linkIsRelative = linkUrl.startsWith(".")
                || linkUrl.startsWith("/")
                || linkUrl.startsWith("?");
        final String linkContents = linkVisitor.visitLink((linkIsRelative) ? baseUrl + linkUrl : linkUrl);
        return pageToClassMapper.map(linkContents, type, baseUrl);
    }

}
