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

	public T visit(){
		final String href = node.attr("href");
		if(href.startsWith("/")){
			final String rootPage = type.getAnnotation(Page.class).value();
			final String newPageUrl = rootPage.substring(0, rootPage.length() - 1) +href;
			return Browser.open(newPageUrl, type);
		}
		if(href.startsWith(".")){
			final String newPageUrl = currentPageUrl+href;
			return Browser.open(newPageUrl, type);
		}
		return Browser.open(href, type);
	}

}
