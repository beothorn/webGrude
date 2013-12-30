package webToJava;

import webToJava.annotations.PageURL;
import webToJava.annotations.QuerySelector;
import webToJava.elements.Link;

@PageURL("http://pudim.com.br")
public class Pudim {

	@QuerySelector("#div_RodapeViewMode a")
	public Link mailTo;
	
}
