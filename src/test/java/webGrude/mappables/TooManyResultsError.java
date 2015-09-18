package webGrude.mappables;

import webGrude.annotations.Page;
import webGrude.annotations.Selector;

@Page
public class TooManyResultsError {

    @Selector("h1")
    public float badFloat;

}
