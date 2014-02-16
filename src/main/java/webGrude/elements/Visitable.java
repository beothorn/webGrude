package webGrude.elements;

import org.jsoup.nodes.Element;

import webGrude.Browser;

public class Visitable<T> {

	private final Class<T> type;
	private final Element node;
	
	public Visitable(final Element node, final Class<T> type) {
		this.node = node;
		this.type = type;
	}

	public T visit(){
		final String href = node.attr("href");
		return Browser.open(href, type);
	}

}
