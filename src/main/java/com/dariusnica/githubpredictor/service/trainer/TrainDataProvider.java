package com.dariusnica.githubpredictor.service.trainer;


import com.dariusnica.githubpredictor.model.GitHubIssueDTO;
import com.dariusnica.githubpredictor.model.GitHubSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import org.springframework.web.reactive.function.client.WebClient;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class TrainDataProvider {

    @Value("${github.token}")
    private String token;

    @Value("${github.repo}")
    private String repo;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();

    public List<GitHubIssueDTO> fetchLiveIssueData() {
        var today = LocalDate.now();
        var sevenDaysAgo = today.minusDays(7);

        var createdSince = sevenDaysAgo.format(FORMATTER);
        var closedBefore = today.format(FORMATTER);

        // 20 issues closed within 7 days
        List<GitHubIssueDTO> closedFast = fetchIssuesByQuery(
                "repo:" + repo +
                        " type:issue state:closed created:>=" + createdSince +
                        " closed:<=" + closedBefore, 20
        );

        // 20 issues NOT closed fast — still open or closed after 7 days
        List<GitHubIssueDTO> notClosedFast = fetchIssuesByQuery(
                "repo:" + repo +
                        " type:issue state:open created:>=" + createdSince +
                        " OR (state:closed closed:>" + closedBefore + ")", 20
        );

        List<GitHubIssueDTO> combined = new ArrayList<>();
        combined.addAll(closedFast);
        combined.addAll(notClosedFast);
        return combined;
    }

    private List<GitHubIssueDTO> fetchIssuesByQuery(String query, int maxItems) {
        String uri = "/search/issues?q=" + UriUtils.encodeQuery(query, StandardCharsets.UTF_8) + "&per_page=" + maxItems;

        var response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(GitHubSearchResponse.class)
                .block();

        return response != null ? response.items() : List.of();
    }

}
