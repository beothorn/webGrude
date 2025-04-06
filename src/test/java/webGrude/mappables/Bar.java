package webGrude.mappables;

import webGrude.mapping.annotations.Page;
import webGrude.mapping.annotations.ParseFormat;
import webGrude.mapping.annotations.Selector;

import java.util.List;

@Page(format = ParseFormat.XML)
public class Bar {

    @Selector(value = "/aaa/ab", useXpath = true)
    private String ab;

    @Selector(value = "/aaa/ac", useXpath = true)
    private String ac;

    @Selector(value = "/aaa/a1", useXpath = true)
    private String a1;

    @Selector(value = "/aaa/aNested", useXpath = true)
    public static class NestedContent {
        // root ensures sub xml is valid
        @Selector(value = "/content", useXpath = true)
        private String content;

        @Selector(value = "/item", useXpath = true)
        private List<String> items;

        @Override
        public String toString() {
            return "NestedContent{" +
                    "content='" + content + '\'' +
                    ", items=" + items +
                    '}';
        }
    }

    NestedContent content;

    @Override
    public String toString() {
        return "Bar{" +
                "ab='" + ab + '\'' +
                ", ac='" + ac + '\'' +
                ", a1='" + a1 + '\'' +
                ", content=" + content +
                '}';
    }
}
