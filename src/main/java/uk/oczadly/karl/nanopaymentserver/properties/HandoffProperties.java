package uk.oczadly.karl.nanopaymentserver.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("payment-server.handoff")
public class HandoffProperties {

    private boolean generateWork = true;
    private String successMessage, successLabel, url;


    /**
     * @return the URL of the handoff server, without protocol scheme
     */
    public String getUrl() {
        return url.toLowerCase().startsWith("https://") ? url.substring(8) : url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the message to be shown to the customer on acceptance of a payment
     */
    public String getSuccessMessage() {
        return successMessage;
    }
    
    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    /**
     * @return the reference to be shown to the customer alongside their transaction
     */
    public String getSuccessLabel() {
        return successLabel;
    }
    
    public void setSuccessLabel(String successLabel) {
        this.successLabel = successLabel;
    }

    /**
     * @return true if work may be generated on the customer's behalf
     */
    public boolean getGenerateWork() {
        return generateWork;
    }
    
    public void setGenerateWork(boolean generateWork) {
        this.generateWork = generateWork;
    }
    
}
