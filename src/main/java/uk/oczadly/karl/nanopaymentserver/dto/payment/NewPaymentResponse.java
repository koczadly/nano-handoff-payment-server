package uk.oczadly.karl.nanopaymentserver.dto.payment;

/**
 * @author Karl Oczadly
 */
public class NewPaymentResponse {

    private String id, handoff;
    
    public NewPaymentResponse(String id, String handoff) {
        this.id = id;
        this.handoff = handoff;
    }
    
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getHandoff() {
        return handoff;
    }
    
    public void setHandoff(String handoff) {
        this.handoff = handoff;
    }
    
}
