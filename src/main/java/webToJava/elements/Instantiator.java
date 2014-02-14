package webToJava.elements;

import org.jsoup.nodes.Element;

public class Instantiator {

	public static boolean typeIsKnown(final Class c){
		if(c.equals(String.class))
			return true;
		return false;
	}
	
	public static <T> T instanceForNode(final Element node, final Class c){
		return (T) node.text();
	}
}
