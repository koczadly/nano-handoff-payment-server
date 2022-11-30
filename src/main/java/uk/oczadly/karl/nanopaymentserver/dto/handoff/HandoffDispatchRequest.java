package uk.oczadly.karl.nanopaymentserver.dto.handoff;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The handoff request, containing the payment ID, and block or hash
 */
public class HandoffDispatchRequest {

    @JsonProperty("id")
    private String paymentId;
    @JsonProperty("block")
    private ObjectNode blockContents;
    
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public ObjectNode getBlockContents() {
        return blockContents;
    }
    
    public void setBlockContents(ObjectNode blockContents) {
        this.blockContents = blockContents;
    }

}
