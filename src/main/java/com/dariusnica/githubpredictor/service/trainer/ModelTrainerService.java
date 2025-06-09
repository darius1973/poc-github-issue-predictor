package com.dariusnica.githubpredictor.service.trainer;

import com.dariusnica.githubpredictor.model.GitHubIssueDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import com.dariusnica.githubpredictor.model.IssueFeatures;
import org.springframework.stereotype.Service;
import smile.classification.LogisticRegression;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.vector.BooleanVector;
import smile.data.vector.IntVector;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ModelTrainerService {

    private final TrainDataProvider trainDataProvider;

    private volatile LogisticRegression model;

    public LogisticRegression getModel() {
        if (model == null) {
            throw new IllegalStateException("Model has not been trained yet");
        }
        return model;
    }

    @PostConstruct
    public void trainModelOnStartup() {
       trainModel();
    }

    public void trainModel() {
        List<GitHubIssueDTO> rawIssues = trainDataProvider.fetchLiveIssueData();
        List<IssueFeatures> featuresList = rawIssues.stream()
                .map(IssueFeatures::fromGitHubIssue)
                .toList();

        if (featuresList.isEmpty()) {
            throw new RuntimeException("No issue data fetched from GitHub");
        }

        DataFrame df = toDataFrame(featuresList);
        this.model = LogisticRegression.fit(Formula.lhs("closed_within_7_days"), df);
    }


    private DataFrame toDataFrame(List<IssueFeatures> issues) {
        int[] titleLengths = issues.stream().mapToInt(IssueFeatures::titleLength).toArray();
        int[] commentsFirstDay = issues.stream().mapToInt(IssueFeatures::commentsFirstDay).toArray();
        boolean[] hasCodeBlock = extractBoolean(issues, IssueFeatures::hasCodeBlock);
        boolean[] isCollaborator = extractBoolean(issues, IssueFeatures::isCollaborator);
        boolean[] labelBug = extractBoolean(issues, IssueFeatures::labelBug);
        boolean[] isBigStory = extractBoolean(issues, IssueFeatures::isBigStory);
        boolean[] closedIn7Days = extractBoolean(issues, IssueFeatures::wasClosedIn7Days);

        return DataFrame.of(
                IntVector.of("titleLength", titleLengths),
                IntVector.of("commentsFirstDay", commentsFirstDay),
                BooleanVector.of("hasCodeBlock", hasCodeBlock),
                BooleanVector.of("isCollaborator", isCollaborator),
                BooleanVector.of("labelBug", labelBug),
                BooleanVector.of("isBigStory", isBigStory),
                BooleanVector.of("closed_within_7_days", closedIn7Days)
        );
    }

    private static boolean[] extractBoolean(List<IssueFeatures> list, Function<IssueFeatures, Boolean> getter) {
        boolean[] result = new boolean[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = getter.apply(list.get(i));
        }
        return result;
    }
}


