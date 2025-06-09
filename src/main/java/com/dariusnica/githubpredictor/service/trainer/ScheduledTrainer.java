package com.dariusnica.githubpredictor.service.trainer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTrainer {

    private final ModelTrainerService modelTrainerService;

   // @Scheduled(cron = "0 0 3 * * *") // Every day at 3:00 AM
   @Scheduled(cron = "0 36 11 * * *")
    public void retrainModelDaily() {
        try {
            modelTrainerService.trainModel();
            log.info("[ModelTrainer] Model retrained successfully at " + LocalDateTime.now());
        } catch (Exception e) {
            log.error("[ModelTrainer] Failed to retrain model.Error occured ", e);
        }
    }
}
