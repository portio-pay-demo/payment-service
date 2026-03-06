package io.portioapay.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Nightly reconciliation job for payment settlement files.
 *
 * NP-2028: Fixed timing gap where settlements arriving after midnight UTC were
 * excluded from the daily report. Job now runs at 02:30 UTC to capture all
 * settlement files (max 2h delay from payment providers).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    // Runs at 02:30 UTC daily — after settlement file delivery window closes
    @Scheduled(cron = "0 30 2 * * *", zone = "UTC")
    public void runDailyReconciliation() {
        Instant endOfWindow = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant startOfWindow = endOfWindow.minus(26, ChronoUnit.HOURS); // 24h + 2h buffer

        log.info("Starting reconciliation for window [{}, {}]", startOfWindow, endOfWindow);

        try {
            int transactionsProcessed = reconcileWindow(startOfWindow, endOfWindow);
            log.info("Reconciliation complete. Processed {} transactions.", transactionsProcessed);
        } catch (Exception ex) {
            log.error("Reconciliation failed: {}", ex.getMessage(), ex);
        }
    }

    private int reconcileWindow(Instant start, Instant end) {
        // Load settled transactions from DB
        // Compare against settlement files from payment providers
        // Flag mismatches for finance team review
        // Generate daily reconciliation report
        return 0; // placeholder
    }
}
