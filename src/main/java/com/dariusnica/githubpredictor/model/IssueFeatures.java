package com.dariusnica.githubpredictor.model;


import lombok.Builder;

import java.time.Duration;
import java.time.Instant;

@Builder
public record IssueFeatures (
         int titleLength,
         int commentsFirstDay,
         boolean hasCodeBlock,
         boolean isCollaborator,
         boolean labelBug,
         boolean isBigStory,
         boolean wasClosedIn7Days
) {
    public static IssueFeatures fromGitHubIssue(GitHubIssueDTO dto) {
        boolean wasClosedIn7Days = false;
        if (dto.closed_at() != null) {
            Instant created = Instant.parse(dto.created_at());
            Instant closed = Instant.parse(dto.closed_at());
            Duration duration = Duration.between(created, closed);
            wasClosedIn7Days = duration.toDays() <= 7;
        }

        return IssueFeatures.builder()
                .titleLength(dto.title().length())
                .commentsFirstDay(dto.comments()) // approx
                .hasCodeBlock(dto.body() != null && dto.body().contains("```"))
                .isCollaborator("COLLABORATOR".equalsIgnoreCase(dto.author_association()))
                .labelBug(dto.labels().stream().anyMatch(l -> l.name().equalsIgnoreCase("bug")))
                .isBigStory(dto.body() != null && dto.body().length() > 1000)
                .wasClosedIn7Days(wasClosedIn7Days)
                .build();
    }
}
