package webGrude.elements;

import org.jsoup.nodes.Element;

import webGrude.Browser;
import webGrude.annotations.Page;

public class Link<T> {

	private final Class<T> type;
	private final Element node;
	private final String currentPageUrl;

	public Link(final Element node, final Class<T> type, final String currentPageUrl) {
		this.node = node;
		this.type = type;
		this.currentPageUrl = currentPageUrl;
	}

    public String getLinkUrl(){
        String urlToVisit;
        final String href = node.attr("href");
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

	public T visit(){
		return Browser.open(getLinkUrl(), type);
	}

}
