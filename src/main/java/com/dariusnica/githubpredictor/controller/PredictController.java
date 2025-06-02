package com.dariusnica.githubpredictor.controller;


import com.dariusnica.githubpredictor.service.predict.PredictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predict")
@RequiredArgsConstructor
public class PredictController {

    private final PredictService predictService;

    @GetMapping("/{issueNumber}")
    public PredictionResult predict(@PathVariable int issueNumber) {
        int prediction = predictService.predictFromIssueNumber(issueNumber);
        return new PredictionResult(prediction == 1 ? "Resolved quickly" : "May take longer");
    }

    public record PredictionResult(String result) {}
}
