package webGrude.elements;

import org.jsoup.nodes.Element;

public class HTML {
	
	public String html;
	
	public HTML(final Element node) {
		html = node.html();
	}


}
