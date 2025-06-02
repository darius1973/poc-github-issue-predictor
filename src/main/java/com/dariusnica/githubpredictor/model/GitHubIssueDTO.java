package com.dariusnica.githubpredictor.model;


import java.util.List;

public record GitHubIssueDTO(
        String title,
        String body,
        int comments,
        String created_at,
        String closed_at,
        List<Label> labels,
        User user,
        String author_association
) {
    public record Label(String name) {}
    public record User(String login, String type) {}
}

