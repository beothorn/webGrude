package webGrude;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import webGrude.mappables.Bar;

public class XMLTest {

    final String barXmlContents = TestUtils.readTestResource("Bar.xml");
    final Webgrude pageToClassMapper = new Webgrude(true);

    @Test
    void simpleMapping() {
        final Bar bar =  pageToClassMapper.map(barXmlContents, Bar.class);
        Assertions.assertEquals("Bar{ab='Test', ac='Another test', a1='A1test', " +
                "content=NestedContent{content='Nested content', items=[a, b, c]}}", bar.toString());
    }

}
