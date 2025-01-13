package bg.sofia.uni.fmi.mjt.news;

import bg.sofia.uni.fmi.mjt.news.dto.ApiResponse;
import bg.sofia.uni.fmi.mjt.news.dto.Article;
import bg.sofia.uni.fmi.mjt.news.dto.Source;
import bg.sofia.uni.fmi.mjt.news.exceptions.BadAPIKeyException;
import bg.sofia.uni.fmi.mjt.news.exceptions.BadRequestException;
import bg.sofia.uni.fmi.mjt.news.exceptions.NewsFeedClientException;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsFeedClientTest {

    private static ApiResponse firstApiResponse;
    private static ApiResponse secondApiResponse;
    private static String firstApiResponseJson;
    private static String secondApiResponseJson;
    private final static Article gameArticle = new Article(new Source(":D", ":D"), "Ivo",
        "Elden Ring is GOTY", "Elden Ring won game of the year.",
        "https://eldenring.com/...", "https://i1.sndcdn.com/avatars-000610836519-ie5ynf-t500x500.jpg",
        "14-12-2022", "Elden Ring won with GoW Ragnarok second close...");
    private final static Article anotherGameArticle = new Article(new Source(":D", ":D"), "Ivo",
        "Hogwarts Legacy will come out soon", "Hogwarts Legacy will be realised on 10th Feb 2023.",
        "https://hogwartslegacy.com/...", "https://i1.sndcdn.com/avatars-000610836519-ie5ynf-t500x500.jpg",
        "20-01-2023", "Fans of Harry Poter can't wait to play it...");
    private final static Article fmiArticle = new Article(new Source(":D", ":D"), "Ivo",
        "FMI session", "FMI session has started.",
        "https://fmi.bg/...", "https://i1.sndcdn.com/avatars-000610836519-ie5ynf-t500x500.jpg",
        "24-01-2023", "The session in FMI has began, students are depressed...");
    private final static Article drinkingArticle = new Article(new Source(":D", ":D"), "Ivo",
        "What drinking can do", "The effect of drinking on students.",
        "https://students-drinking.bg/...", "https://i1.sndcdn.com/avatars-000610836519-ie5ynf-t500x500.jpg",
        "01-02-2023", "With the coming exams a lot of students get drunk in order to celebrate or forget their disappointment...");

    @Mock
    private HttpClient newsFeedHttpClientMock;

    @Mock
    private HttpResponse<String> httpNewsFeedResponseMock;

    private NewsFeedClient newsClient;

    @BeforeAll
    static void setUpClass() {
        firstApiResponse = new ApiResponse("ok", 3, new Article[] {gameArticle, anotherGameArticle});
        secondApiResponse = new ApiResponse("ok", 3, new Article[] {fmiArticle, drinkingArticle});
        firstApiResponseJson = new Gson().toJson(firstApiResponse);
        secondApiResponseJson = new Gson().toJson(secondApiResponse);
    }

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        newsClient = new NewsFeedClient(newsFeedHttpClientMock);
    }

    @Test
    void testGetArticlesNoKeywords() {
        assertThrows(IllegalArgumentException.class,
            () -> newsClient.getArticles(new String[0], null, null),
            "There should me at least one keyword");
    }

    @Test
    void testGetArticlesUnsupportedCategory() {
        assertThrows(IllegalArgumentException.class,
            () -> newsClient.getArticles(new String[] {"test"}, "test", null),
            "The category is not in the supported list");
    }

    @Test
    void testGetArticlesUnsupportedCountry() {
        assertThrows(IllegalArgumentException.class,
            () -> newsClient.getArticles(new String[] {"test"}, null, "test"),
            "The country is not in the supported list");
    }

    @Test
    void testGetArticlesInvalidArticlesPerPage() {
        assertThrows(IllegalArgumentException.class,
            () -> newsClient.getArticles(new String[] {"test"}, null, null, -1, 5),
            "The number of articles per page should be positive");
        assertThrows(IllegalArgumentException.class,
            () -> newsClient.getArticles(new String[] {"test"}, null, null, 0, 5),
            "The number of articles per page should be positive");
    }

    @Test
    void testGetArticlesInvalidMaxArticles() {
        assertThrows(IllegalArgumentException.class,
            () -> newsClient.getArticles(new String[] {"test"}, null, null, 3, -1),
            "The number of max articles should be positive");
        assertThrows(IllegalArgumentException.class,
            () -> newsClient.getArticles(new String[] {"test"}, null, null, 3, 0),
            "The number of max articles should be positive");
    }

    @Test
    void testGetArticlesResponseCode400() throws IOException, InterruptedException {
        when(httpNewsFeedResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);
        when(newsFeedHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenReturn(httpNewsFeedResponseMock);

        assertThrows(BadRequestException.class,
            () -> newsClient.getArticles(new String[] {"test"}, null, null),
            "An exception should be thrown if the status code is 400");
    }

    @Test
    void testGetArticlesResponseCode401() throws IOException, InterruptedException {
        when(httpNewsFeedResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_UNAUTHORIZED);
        when(newsFeedHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenReturn(httpNewsFeedResponseMock);

        assertThrows(BadAPIKeyException.class,
            () -> newsClient.getArticles(new String[] {"test"}, null, null),
            "An exception should be thrown if the status code is 401");
    }

    @Test
    void testGetArticlesResponseCode500() throws IOException, InterruptedException {
        when(httpNewsFeedResponseMock.statusCode()).thenReturn(500);
        when(newsFeedHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenReturn(httpNewsFeedResponseMock);

        assertThrows(NewsFeedClientException.class,
            () -> newsClient.getArticles(new String[] {"test"}, null, null),
            "An exception should be thrown if the status code is different from 200, 400 or 401");
    }

    @Test
    void testGetArticlesSuccessfulOnePage()
        throws IOException, InterruptedException, NewsFeedClientException {
        when(httpNewsFeedResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(newsFeedHttpClientMock.send(Mockito.any(HttpRequest.class),
            ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenReturn(httpNewsFeedResponseMock);

        when(httpNewsFeedResponseMock.body()).thenReturn(firstApiResponseJson);

        List<Article> actual = newsClient.getArticles(null, "entertainment", "", 2, 2);

        assertEquals(2, actual.size());
        assertEquals(gameArticle, actual.get(0));
        assertEquals(anotherGameArticle, actual.get(1));
    }

    @Test
    void testGetArticlesSuccessfulMultiplePages()
        throws IOException, InterruptedException, NewsFeedClientException {
        when(httpNewsFeedResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(newsFeedHttpClientMock.send(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
            .thenReturn(httpNewsFeedResponseMock);

        when(httpNewsFeedResponseMock.body())
            .thenReturn(firstApiResponseJson)
            .thenReturn(secondApiResponseJson);

        List<Article> actual = newsClient.getArticles(new String[] {"test"}, "", "", 2, 3);

        assertEquals(3, actual.size());
        assertEquals(gameArticle, actual.get(0));
        assertEquals(anotherGameArticle, actual.get(1));
        assertEquals(fmiArticle, actual.get(2));
    }
}