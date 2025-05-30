package webGrude.mappables;

import webGrude.mapping.annotations.*;

import java.util.List;

@XML
public class Bar {

    @XPath("/aaa/ab")
    private String ab;

    @XPath("/aaa/ac")
    private String ac;

    @XPath("/aaa/a1")
    private String a1;

    public static class NestedContent {

        @XPath("/content")
        private String content;

        @XPath("/item")
        private List<String> items;

        @Override
        public String toString() {
            return "NestedContent{" +
                    "content='" + content + '\'' +
                    ", items=" + items +
                    '}';
        }
    }

    @XPath("/aaa/aNested") NestedContent content;

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
