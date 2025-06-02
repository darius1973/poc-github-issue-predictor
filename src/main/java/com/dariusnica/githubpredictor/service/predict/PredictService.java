package com.dariusnica.githubpredictor.service.predict;

import com.dariusnica.githubpredictor.model.GitHubIssueDTO;
import com.dariusnica.githubpredictor.model.IssueFeatures;
import com.dariusnica.githubpredictor.service.trainer.ModelTrainerService;
import org.springframework.stereotype.Service;
import smile.classification.LogisticRegression;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class PredictService {

    private final ModelTrainerService modelTrainerService;

    @Value("${github.repo}")
    private String repo;

    @Value("${github.token}")
    private String token;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader("Authorization", "Bearer " + token)
            .build();


    public int predictFromIssueNumber(int issueNumber) {
        GitHubIssueDTO dto = fetchIssue(issueNumber);
        IssueFeatures features = IssueFeatures.fromGitHubIssue(dto);
        return predictWith95PercentAccuracy(features);
    }

    private GitHubIssueDTO fetchIssue(int issueNumber) {
        return webClient.get()
                .uri("/repos/{repo}/issues/{issue}", repo, issueNumber)
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

