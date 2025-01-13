package bg.sofia.uni.fmi.mjt.news.exceptions;

public class NewsFeedClientException extends Exception {
    public NewsFeedClientException(String message) {
        super(message);
    }
    public NewsFeedClientException(String message, Exception e) {
        super(message, e);
    }
}
