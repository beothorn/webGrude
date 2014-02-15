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
		classes.add(HTML.class);
	}
	
	public static boolean typeIsKnown(final Class c){
		return classes.contains(c);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T instanceForNode(final Element node, final Class<T> c){
		if(c.equals(HTML.class))
			return (T) new HTML(node);
		return (T) node.text();
	}
}
