package webGrude.elements;

import org.jsoup.nodes.Element;

import java.util.List;

@SuppressWarnings("rawtypes")
public class Instantiator {

	public static boolean typeIsKnown(final Class c){

        if(c.equals(String.class))
            return true;
        if(c.equals(Integer.class) || c.getSimpleName().equals("int"))
            return true;
        if(c.equals(Float.class) || c.getSimpleName().equals("float"))
            return true;
        if(c.equals(Boolean.class) || c.getSimpleName().equals("boolean"))
            return true;
        if(c.equals(Link.class))
            return true;
        if(c.equals(Element.class))
            return true;
        if(c.equals(List.class))
            return true;

		return false;
	}

	@SuppressWarnings("unchecked")
    public static <T> T instanceForNode(final Element node, String attribute, final Class<T> c){
		String value;
		
		try{
	        if(c.equals(Element.class))
	            return (T) node;
	
	        if(attribute != null && !attribute.isEmpty())
	            value = node.attr(attribute);
	        else
	            value = node.text();
	
	        if(c.equals(String.class))
	            return (T) value;
	        if(c.equals(Integer.class) || c.getSimpleName().equals("int"))
	            return (T) Integer.valueOf(value);
	        if(c.equals(Float.class) || c.getSimpleName().equals("float"))
	            return (T) Float.valueOf(value);
	        if(c.equals(Boolean.class) || c.getSimpleName().equals("boolean"))
	            return (T) Boolean.valueOf(value);
		}catch(Exception e){
			throw new WrongTypeForField(node,attribute,c,e);
		}

        return (T) value;
    }

	public static boolean typeIsVisitable(final Class<?> fieldClass) {
		return fieldClass.equals(Link.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> T visitableForNode(final Element node, final Class c, final String currentPageUrl) {
		return (T) new Link<T>(node, c, currentPageUrl);
	}
}
