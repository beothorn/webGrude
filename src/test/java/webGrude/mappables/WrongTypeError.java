package webGrude.mappables;

import webGrude.mapping.annotations.Page;
import webGrude.mapping.annotations.Selector;

@Page
public class WrongTypeError {
	
	@Selector("#boolean")
	public float badFloat;

}
