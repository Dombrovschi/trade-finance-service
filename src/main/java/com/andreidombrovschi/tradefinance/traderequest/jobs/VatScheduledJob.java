package com.andreidombrovschi.tradefinance.traderequest.jobs;

import com.andreidombrovschi.tradefinance.traderequest.service.VatCalculationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VatScheduledJob {

    private final VatCalculationService vatService;

    public VatScheduledJob(VatCalculationService vatService) {
        this.vatService = vatService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void calculateVatDaily() {

        vatService.calculateVatForApprovedTransactions();
    }
}