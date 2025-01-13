package bg.sofia.uni.fmi.mjt.news;

import bg.sofia.uni.fmi.mjt.news.dto.ApiResponse;
import bg.sofia.uni.fmi.mjt.news.dto.Article;
import bg.sofia.uni.fmi.mjt.news.exceptions.BadAPIKeyException;
import bg.sofia.uni.fmi.mjt.news.exceptions.BadRequestException;
import bg.sofia.uni.fmi.mjt.news.exceptions.NewsFeedClientException;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewsFeedClient {

    private static final String ARTICLES_PER_PAGE = "Articles per page";
    private static final String MAX_ARTICLES = "Max articles";

    private static final String API_KEY = "0823118367d04442ad5cf1799d8968db";

    private static final String ENDPOINT_SCHEME = "http";
    private static final String ENDPOINT_HOST = "newsapi.org";
    private static final String ENDPOINT_PATH = "/v2/top-headlines";
    private static final String ENDPOINT_QUERY = "q=%s&apiKey=%s";
    private static final int DEFAULT_ARTICLES_PER_PAGE = 20;
    private static final int FIRST_PAGE = 1;
    private static final int DEFAULT_MAX_ARTICLES = 100;
    private static final String FILTERS_SEPARATOR = "&";
    private static final String CATEGORY_FILTER = "category=";
    private static final String COUNTRY_FILTER = "country=";
    private static final String PAGE_SIZE_FILTER = "pageSize=";
    private static final String PAGE_FILTER = "page=";

    private static final Map<String, String> AVAILABLE_COUNTRIES = Map.<String, String>ofEntries(
        Map.entry("The United Arab Emirates", "ae"),
        Map.entry("Argentina", "ar"),
        Map.entry("Austria", "at"),
        Map.entry("Australia", "au"),
        Map.entry("Belgium", "be"),
        Map.entry("Bulgaria", "bg"),
        Map.entry("Brazil", "br"),
        Map.entry("Canada", "ca"),
        Map.entry("Switzerland", "ch"),
        Map.entry("China", "cn"),
        Map.entry("Colombia", "co"),
        Map.entry("Cuba", "cu"),
        Map.entry("The Czech Republic", "cz"),
        Map.entry("Germany", "de"),
        Map.entry("Egypt", "eg"),
        Map.entry("France", "fr"),
        Map.entry("The United Kingdom", "gb"),
        Map.entry("Greece", "gr"),
        Map.entry("Hong Kong", "hk"),
        Map.entry("Indonesia", "id"),
        Map.entry("Ireland", "ie"),
        Map.entry("Israel", "il"),
        Map.entry("India", "in"),
        Map.entry("Italy", "it"),
        Map.entry("Japan", "jp"),
        Map.entry("South Korea", "kr"),
        Map.entry("Lithuania", "lt"),
        Map.entry("Latvia", "lv"),
        Map.entry("Morocco", "ma"),
        Map.entry("Mexico", "mx"),
        Map.entry("Malaysia", "my"),
        Map.entry("Nigeria", "ng"),
        Map.entry("The Netherlands", "nl"),
        Map.entry("Norway", "no"),
        Map.entry("New Zealand", "nz"),
        Map.entry("The Philippines", "ph"),
        Map.entry("Poland", "pl"),
        Map.entry("Portugal", "pt"),
        Map.entry("Romania", "ro"),
        Map.entry("Serbia", "rs"),
        Map.entry("Russia", "ru"),
        Map.entry("Saudi Arabia", "sa"),
        Map.entry("Sweden", "se"),
        Map.entry("Singapore", "sg"),
        Map.entry("Slovenia", "si"),
        Map.entry("Slovakia", "sk"),
        Map.entry("Thailand", "th"),
        Map.entry("Turkey", "tr"),
        Map.entry("Taiwan", "tw"),
        Map.entry("Ukraine", "ua"),
        Map.entry("The United States", "us"),
        Map.entry("Venezuela", "ve"),
        Map.entry("South Africa", "za")
    );
    private static final Set<String> AVAILABLE_CATEGORIES = Set.of(
        "business",
        "entertainment",
        "general",
        "health",
        "science",
        "sports",
        "technology"
    );

    private final HttpClient newsHttpClient;
    private final String apiKey;

    public NewsFeedClient(HttpClient newsHttpClient) {
        this(newsHttpClient, API_KEY);
    }

    public NewsFeedClient(HttpClient newsHttpClient, String apiKey) {
        this.newsHttpClient = newsHttpClient;
        this.apiKey = apiKey;
    }

    public List<Article> getArticles(String[] keywords, String category, String country)
        throws NewsFeedClientException {
        return getArticles(keywords, category, country, DEFAULT_ARTICLES_PER_PAGE, DEFAULT_MAX_ARTICLES);
    }

    public List<Article> getArticles(String[] keywords, String category, String country,
                                     int articlesPerPage, int maxArticles) throws NewsFeedClientException {
        validation(keywords, category, country, articlesPerPage, maxArticles);

        int neededArticles = maxArticles;
        int currentPage = FIRST_PAGE;
        List<Article> result = new ArrayList<>();

        while (result.size() < neededArticles) {
            HttpResponse<String> response = getResponse(keywords, category, country, articlesPerPage, currentPage);
            badStatusCodesCheck(response);

            ApiResponse apiResponse = ApiResponse.of(response.body());

            Article[] currentArticles = apiResponse.getArticles();

            if (currentArticles.length > maxArticles - result.size()) {
                int remaining = maxArticles - result.size();

                result.addAll(Arrays.asList(currentArticles).subList(0, remaining));
                break;

            } else {
                result.addAll(Arrays.asList(currentArticles));
            }

            currentPage++;
            neededArticles = Math.min(apiResponse.getTotalResults(), maxArticles);
        }

        return result;
    }

    private HttpResponse<String> getResponse(String[] keywords, String category, String country,
                                             int articlesPerPage, int pageNumber) throws NewsFeedClientException {
        HttpResponse<String> response;

        try {
            URI uri = new URI(ENDPOINT_SCHEME, ENDPOINT_HOST, ENDPOINT_PATH,
                getEndpointQuery(keywords, category, country, articlesPerPage, pageNumber), null);

            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

            response = newsHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new NewsFeedClientException("Could not receive news feed", e);
        }

        return response;
    }

    private String getEndpointQuery(String[] keywords, String category, String country,
                                    int articlesPerPage, int pageNumber) {
        StringBuilder filters = new StringBuilder();

        if (keywords != null) {
            filters.append(String.join("+", keywords));
        }

        if (category != null && !category.isBlank()) {
            if (!filters.isEmpty()) {
                filters.append(FILTERS_SEPARATOR);
            }
            filters.append(CATEGORY_FILTER).append(category);
        }

        if (country != null && !country.isBlank()) {
            if (!filters.isEmpty()) {
                filters.append(FILTERS_SEPARATOR);
            }
            filters.append(COUNTRY_FILTER).append(AVAILABLE_COUNTRIES.get(country));
        }

        if (!filters.isEmpty()) {
            filters.append(FILTERS_SEPARATOR);
        }
        filters.append(PAGE_SIZE_FILTER).append(articlesPerPage);
        if (!filters.isEmpty()) {
            filters.append(FILTERS_SEPARATOR);
        }
        filters.append(PAGE_FILTER).append(pageNumber);

        return ENDPOINT_QUERY.formatted(filters.toString(), apiKey);
    }

    private void badStatusCodesCheck(HttpResponse<String> response) throws NewsFeedClientException {
        if (response.statusCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new BadRequestException("The request is unacceptable - missing or misconfigured parameter");
        }

        if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new BadAPIKeyException("The API Key was either missing or incorrect");
        }

        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw new NewsFeedClientException("Unexpected response code from news feed server");
        }
    }

    private void validation(String[] keywords, String category, String country, int articlesPerPage, int maxArticles) {
        if (keywords != null && keywords.length == 0) {
            throw new IllegalArgumentException("There must be at least one keyword");
        }
        if (category != null && !category.isBlank() && !AVAILABLE_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("The given category is not supported");
        }
        if (country != null && !country.isBlank() && !AVAILABLE_COUNTRIES.containsKey(country)) {
            throw new IllegalArgumentException("The given country is not supported");
        }

        if (articlesPerPage <= 0) {
            throw new IllegalArgumentException(ARTICLES_PER_PAGE + " must be positive");
        }
        if (maxArticles <= 0) {
            throw new IllegalArgumentException(MAX_ARTICLES + " must be positive");
        }
    }
}