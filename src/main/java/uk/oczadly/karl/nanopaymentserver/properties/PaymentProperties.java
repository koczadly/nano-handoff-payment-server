package uk.oczadly.karl.nanopaymentserver.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("paymentserver.payment")
public class PaymentProperties {
    
    private long timeout = 20 * 60000; // 20 Min
    private long confirmationTimeout = 10 * 60000; // 10 Min
    
    
    public long getTimeout() {
        return timeout;
    }
    
    public void setTimeout(long seconds) {
        this.timeout = seconds * 1000;
    }
    
    public long getConfirmationTimeout() {
        return confirmationTimeout;
    }
    
    public void setConfirmationTimeout(long seconds) {
        this.confirmationTimeout = seconds * 1000;
    }
    
}
