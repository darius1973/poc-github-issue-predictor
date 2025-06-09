package com.dariusnica.githubpredictor.model;


import java.util.List;

public record GitHubSearchResponse(
        int total_count,
        boolean incomplete_results,
        List<GitHubIssueDTO> items
) {}
