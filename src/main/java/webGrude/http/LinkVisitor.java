package webGrude.http;

public interface LinkVisitor {
    String visitLink(final String href);
}