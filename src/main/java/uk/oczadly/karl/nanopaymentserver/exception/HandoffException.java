package uk.oczadly.karl.nanopaymentserver.exception;

import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffResponse;

/**
 * @author Karl Oczadly
 */
public class HandoffException extends RuntimeException {
    
    private final HandoffResponse response;
    
    public HandoffException(HandoffResponse response) {
        super(response.getStatus() + (response.getMessage() != null ? (": " + response.getMessage()) : ""));
        this.response = response;
    }
    
    public HandoffException(HandoffResponse.Status status) {
        this(new HandoffResponse(status));
    }
    
    public HandoffException(HandoffResponse.Status status, String message) {
        this(new HandoffResponse(status, message));
    }
    
    
    public HandoffResponse getResponseObject() {
        return response;
    }
    
}
