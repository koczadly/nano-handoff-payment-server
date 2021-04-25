package uk.oczadly.karl.nanopaymentserver.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentServiceScheduler {
    
    @Autowired private PaymentService paymentService;
    
    
    /**
     * Ensures that payments have their state updated in the database, and that redundant information is removed.
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void updateExpiry() {
        paymentService.updateExpiredPayments();
    }
    
}
