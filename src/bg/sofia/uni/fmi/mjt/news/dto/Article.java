package bg.sofia.uni.fmi.mjt.news.dto;

import java.util.Objects;

public class Article {
    private final Source source;
    private final String author;
    private final String title;
    private final String description;
    private final String url;
    private final String urlToImage;
    private final String publishedAt;
    private final String content;

    public Article(Source source, String author, String title, String description,
                   String url, String urlToImage, String publishedAt, String content) {
        this.source = source;
        this.author = author;
        this.title = title;
        this.description = description;
        this.url = url;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Source: " + source.getName() + System.lineSeparator()
            + "Title: " + title + System.lineSeparator()
            + "Author: " + author + System.lineSeparator()
            + "Description: " + description + System.lineSeparator()
            + "Content: " + content + System.lineSeparator()
            + "Date: " + publishedAt + System.lineSeparator()
            + "Image: " + urlToImage + System.lineSeparator()
            + "Full article: " + url + System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Article that)) {
            return false;
        }

        return Objects.equals(source, that.source) && Objects.equals(author, that.author)
            && Objects.equals(title, that.title) && Objects.equals(description, that.description)
            && Objects.equals(url, that.url) && Objects.equals(urlToImage, that.urlToImage)
            && Objects.equals(publishedAt, that.publishedAt) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, author, title, description, url, urlToImage, publishedAt, content);
    }
}
