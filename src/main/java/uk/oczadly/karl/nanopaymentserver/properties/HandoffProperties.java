package uk.oczadly.karl.nanopaymentserver.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("paymentserver.payment.handoff")
public class HandoffProperties {
    
    @Autowired private ServerProperties serverProperties;
    
    private boolean workGen = true;
    private String successMessage, successLabel, url;
    
    
    public String getUrl() {
        return url != null ? url : "localhost:" + serverProperties.getPort() + "/handoff";
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getSuccessMessage() {
        return successMessage;
    }
    
    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
    
    public String getSuccessLabel() {
        return successLabel;
    }
    
    public void setSuccessLabel(String successLabel) {
        this.successLabel = successLabel;
    }
    
    public boolean getWorkGen() {
        return workGen;
    }
    
    public void setWorkGen(boolean workGen) {
        this.workGen = workGen;
    }
    
}
