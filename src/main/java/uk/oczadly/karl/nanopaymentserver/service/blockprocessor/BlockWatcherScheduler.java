package uk.oczadly.karl.nanopaymentserver.service.blockprocessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BlockWatcherScheduler {

    @Autowired private BlockWatcherService service;


    @Scheduled(fixedDelay = 2000)
    public void checkConfirmations() {
        service.checkForConfirmations();
    }

}
