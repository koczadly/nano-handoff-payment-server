package uk.oczadly.karl.nanopaymentserver.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TransactionExpiryScheduler {

    @Autowired private PaymentService paymentService;


    @Scheduled(fixedDelay = 60000) // 1 min
    public void expire() {
        // Will automatically update transactions which should have expired
        paymentService.getPendingTransactions();
    }

}
