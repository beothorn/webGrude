package webGrude.mappables;

import webGrude.annotations.Page;
import webGrude.annotations.Selector;

@Page
public class WrongTypeError {
	
	@Selector("#boolean")
	public float badFloat;

}
