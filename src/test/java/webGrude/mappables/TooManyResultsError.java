package webGrude.mappables;

import webGrude.mapping.annotations.Page;
import webGrude.mapping.annotations.Selector;

@Page
public class TooManyResultsError {

    @Selector("h1")
    public float badFloat;

}
