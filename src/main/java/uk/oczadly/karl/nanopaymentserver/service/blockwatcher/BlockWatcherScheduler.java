package uk.oczadly.karl.nanopaymentserver.service.blockwatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.oczadly.karl.nanopaymentserver.entity.payment.Payment;
import uk.oczadly.karl.nanopaymentserver.service.payment.PaymentService;

import java.util.List;

@Component
public class BlockWatcherScheduler implements ApplicationRunner {
    
    private static final Logger log = LoggerFactory.getLogger(BlockWatcherScheduler.class);
    
    @Autowired private BlockConfirmationWatcherService blockWatcherService;
    @Autowired private PaymentService paymentService;
    
    
    /**
     * Run on app startup.
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Watch all blocks on startup (load from database)
        int loadCount = 0;
        for (Payment payment : paymentService.getActivePayments()) {
            if (!payment.getStatus().isFinalState() && payment.getHandoffHash() != null) {
                blockWatcherService.watch(payment);
                loadCount++;
            }
        }
        log.info("Loaded {} ongoing active payments into block watcher.", loadCount);
    }
    
    /**
     * Manually polls the confirmation status of all active payments every 1 minute, as a fallback for the websocket.
     */
    @Scheduled(fixedDelay = 60 * 1000)
    public void pollActivePayments() {
        blockWatcherService.checkActivePaymentsForConfirmation();
    }
    
}
