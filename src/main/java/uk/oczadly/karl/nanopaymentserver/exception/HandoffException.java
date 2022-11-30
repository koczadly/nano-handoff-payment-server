package uk.oczadly.karl.nanopaymentserver.exception;

import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffDispatchResponse;

/**
 * @author Karl Oczadly
 */
public class HandoffException extends RuntimeException {
    
    private final HandoffDispatchResponse response;
    
    public HandoffException(HandoffDispatchResponse response) {
        super(response.getStatus() + (response.getMessage() != null ? (": " + response.getMessage()) : ""));
        this.response = response;
    }
    
    public HandoffException(HandoffDispatchResponse.Status status) {
        this(new HandoffDispatchResponse(status));
    }
    
    public HandoffException(HandoffDispatchResponse.Status status, String message) {
        this(new HandoffDispatchResponse(status, message));
    }
    
    
    public HandoffDispatchResponse getResponse() {
        return response;
    }
    
}
