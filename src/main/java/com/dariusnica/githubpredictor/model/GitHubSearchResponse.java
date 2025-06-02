package com.dariusnica.githubpredictor.model;


import java.util.List;

public record GitHubSearchResponse(
        List<GitHubIssueDTO> items
) {}
