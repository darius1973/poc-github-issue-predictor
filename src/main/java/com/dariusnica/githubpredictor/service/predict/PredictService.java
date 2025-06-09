package com.dariusnica.githubpredictor.service.predict;

import com.dariusnica.githubpredictor.model.GitHubIssueDTO;
import com.dariusnica.githubpredictor.model.IssueFeatures;
import com.dariusnica.githubpredictor.service.trainer.ModelTrainerService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import smile.classification.LogisticRegression;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictService {

    private final ModelTrainerService modelTrainerService;

    @Value("${github.repo}")
    private String repo;

    @Value("${github.token}")
    private String token;

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

    public int predictFromIssueNumber(int issueNumber) {
        GitHubIssueDTO dto = fetchIssue(issueNumber);
        IssueFeatures features = IssueFeatures.fromGitHubIssue(dto);
        return predictWith95PercentAccuracy(features);
    }

    private GitHubIssueDTO fetchIssue(int issueNumber) {
        List<String> repoData = Arrays.stream(repo.split("/")).toList();
        String owner = repoData.get(0);
        String repoValue = repoData.get(1);
        return webClient.get()
                .uri("/repos/{owner}/{repo}/issues/{issue}", owner, repoValue, issueNumber)
                .retrieve()
                .bodyToMono(GitHubIssueDTO.class)
                .block(); // blocking for simplicity
    }

    public int predictWith95PercentAccuracy(IssueFeatures input) {
        double threshold = 0.95;

        double[] features = {
                input.titleLength(),
                input.commentsFirstDay(),
                input.hasCodeBlock() ? 1.0 : 0.0,
                input.isCollaborator() ? 1.0 : 0.0,
                input.labelBug() ? 1.0 : 0.0,
                input.isBigStory() ? 1.0 : 0.0
        };

        LogisticRegression model = modelTrainerService.getModel();
        double probability = model.predict(features);

        return probability >= threshold ? 1 : 0;
    }
}

