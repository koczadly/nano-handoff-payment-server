package uk.oczadly.karl.nanopaymentserver.dto.handoff;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HandoffPaymentRequest {

    private UUID paymentId;
    private String amountRaw, destination;
    private Map<String, HandoffChannel> channels = new HashMap<>();
    private boolean variableAmount = false, workRequired = false;


    public HandoffPaymentRequest(UUID paymentId, String destination, String amountRaw) {
        this.paymentId = paymentId;
        this.destination = destination;
        this.amountRaw = amountRaw;
    }


    @JsonProperty("id")
    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    @JsonProperty("d")
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @JsonProperty("a")
    public String getAmountRaw() {
        return amountRaw;
    }

    public void setAmountRaw(String amountRaw) {
        this.amountRaw = amountRaw;
    }

    @JsonProperty("c")
    public Map<String, HandoffChannel> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, HandoffChannel> channels) {
        this.channels = channels;
    }

    public void addChannel(HandoffChannel method) {
        this.channels.put(method.getType().toLowerCase(), method);
    }
    
    @JsonProperty("va")
    public Boolean isVariableAmount() {
        return variableAmount;
    }

    public void setVariableAmount(boolean variableAmount) {
        this.variableAmount = variableAmount;
    }
    
    @JsonProperty("wk")
    public Boolean getWorkRequired() {
        return workRequired;
    }

    public void setWorkRequired(boolean workRequired) {
        this.workRequired = workRequired;
    }



    /**
     * @return an encoded handoff URI to be passed to the customer
     */
    public String encodeUri() {
        String json = null;
        try {
            json = new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new AssertionError(e); // Serialization to JSON shouldn't throw an exception
        }
        String b64 = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        return "nanohandoff:" + b64;
    }
    
}
