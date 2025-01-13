package bg.sofia.uni.fmi.mjt.news.dto;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Objects;

public class ApiResponse {
    private final String status;
    private final int articlesCount;
    private final Article[] articles;

    public ApiResponse(String status, int articlesCount, Article[] articles) {
        this.status = status;
        this.articlesCount = articlesCount;
        this.articles = articles;
    }

    public int getTotalResults() {
        return articlesCount;
    }

    public Article[] getArticles() {
        return articles;
    }

    public static ApiResponse of(String json) {
        Gson gson = new Gson();

        return gson.fromJson(json, ApiResponse.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApiResponse that)) {
            return false;
        }

        return Objects.equals(status, that.status)
            && Objects.equals(articlesCount, that.articlesCount)
            && Arrays.equals(articles, that.articles);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(status, articlesCount);
        result = 31 * result + Arrays.hashCode(articles);

        return result;
    }
}
