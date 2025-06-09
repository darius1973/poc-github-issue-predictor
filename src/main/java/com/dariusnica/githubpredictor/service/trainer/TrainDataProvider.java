package com.dariusnica.githubpredictor.service.trainer;


import com.dariusnica.githubpredictor.model.GitHubIssueDTO;
import com.dariusnica.githubpredictor.model.GitHubSearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TrainDataProvider {

    @Value("${github.token}")
    private String token;

    @Value("${github.repo}")
    private String repo;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .filter(logRequest())  // Register logging filter
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            return reactor.core.publisher.Mono.just(clientRequest);
        });
    }

    public List<GitHubIssueDTO> fetchLiveIssueData() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        //closed in 7 days
        String closedRange = sevenDaysAgo.format(FORMATTER) + ".." + today.format(FORMATTER);

        List<GitHubIssueDTO> closedFast = fetchIssuesByQuery(
                repo, true, true, closedRange, 20
        );

        List<GitHubIssueDTO> otherIssues = fetchIssuesByQuery(
                repo, true, false, null, 20
        );

        List<GitHubIssueDTO> combined = new ArrayList<>();
        combined.addAll(closedFast);
        combined.addAll(otherIssues);

        return combined;
    }

    private List<GitHubIssueDTO> fetchIssuesByQuery(
            String repo,
            boolean isIssue,
            boolean isClosed,
            String closedRange,
            int maxItems
    ) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("repo:").append(repo);
        if (isIssue) queryBuilder.append(" is:issue");
        if (isClosed) queryBuilder.append(" is:closed");
        if (closedRange != null && !closedRange.isBlank()) {
            queryBuilder.append(" closed:").append(closedRange);
        }

        String query = queryBuilder.toString();

        GitHubSearchResponse response = webClient.get()
                .uri(uriBuilder -> buildSearchUri(uriBuilder, query, maxItems))
                .retrieve()
                .bodyToMono(GitHubSearchResponse.class)
                .block();
        return response != null ? response.items() : List.of();
    }

    private java.net.URI buildSearchUri(UriBuilder uriBuilder, String query, int maxItems) {
        return uriBuilder
                .path("/search/issues")
                .queryParam("q", query)
                .queryParam("per_page", maxItems)
                .build();
    }
}
