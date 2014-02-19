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
		return (T) node.text();
	}

	public static boolean typeIsVisitable(final Class<?> fieldClass) {
		return fieldClass.equals(Link.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> T visitableForNode(final Element node, final Class c, final String currentPageUrl) {
		return (T) new Link<T>(node, c, currentPageUrl);
	}
}
