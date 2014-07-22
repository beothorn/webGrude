package webGrude.elements;

import org.jsoup.nodes.Element;

import webGrude.Browser;

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
 * @see webGrude.Browser
 * @see webGrude.annotations.Page
 * @see webGrude.annotations.Selector
 * @author beothorn
 */
public class Link<T> {

	private final Class<T> type;
	private final Element hrefElement;
	private final String currentPageUrl;
	
	/***
	 * A link uses maps a page from an url to an instance of a class annotated with <i>{@literal @}Page</i>. 
	 * when the method {@link #visit() visit} is called
	 * 
	 * @param hrefElement A Jsoup Element with a href attribute
	 * @param visitingType The type that will be mapped from the page when the method {@link #visit() visit} is called
	 * @param baseUrl The base url, it is used to resolve relative links
	 */
	public Link(final Element hrefElement, final Class<T> visitingType, final String baseUrl) {
		this.hrefElement = hrefElement;
		this.type = visitingType;
		this.currentPageUrl = baseUrl.endsWith("/")?baseUrl.substring(0, baseUrl.length()-2):baseUrl;
	}

    public String getLinkUrl(){
        String urlToVisit;
        final String href = hrefElement.attr("href");
        urlToVisit = href;
        if(href.startsWith("/")){
            final String rootPage = currentPageUrl.replaceAll("(.*://.*?/).*","$1");
            final String newPageUrl = rootPage.substring(0, rootPage.length() - 1) +href;
            urlToVisit = newPageUrl;
        }
        if(href.startsWith(".")){
            final String newPageUrl = currentPageUrl+"/"+href;
            urlToVisit = newPageUrl;
        }
        return urlToVisit;
    }
    /***
     * Visit a page link and map its values to an instance of the visitingType.
     * 
     * @return an instance of visitingType 
     */
	public T visit(){
		return Browser.open(getLinkUrl(), type);
	}

}
