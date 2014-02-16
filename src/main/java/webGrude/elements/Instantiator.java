package webGrude.elements;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;

@SuppressWarnings("rawtypes")
public class Instantiator {

	private static List<Class> classes;
	static {
		classes = new ArrayList<Class>();
		classes.add(String.class);
		classes.add(Link.class);
		classes.add(Element.class);
	}
	
	public static boolean typeIsKnown(final Class c){
		return classes.contains(c);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T instanceForNode(final Element node, final Class<T> c){
		if(c.equals(Element.class))
			return (T) node;
		if(c.equals(Link.class))
			return (T) new Link<T>();
		return (T) node.text();
	}
}
