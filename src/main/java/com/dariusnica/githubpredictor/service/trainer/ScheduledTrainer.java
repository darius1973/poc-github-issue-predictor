package com.dariusnica.githubpredictor.service.trainer;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ScheduledTrainer {

    private final ModelTrainerService modelTrainerService;

    @Scheduled(cron = "0 0 3 * * *") // Every day at 3:00 AM
    public void retrainModelDaily() {
        try {
            modelTrainerService.trainModel();
            System.out.println("[ModelTrainer] Model retrained successfully at " + LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("[ModelTrainer] Failed to retrain model: " + e.getMessage());
            //handle error nicely
            e.printStackTrace();
        }
    }
}
