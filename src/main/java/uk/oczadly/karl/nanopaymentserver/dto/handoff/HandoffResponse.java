package uk.oczadly.karl.nanopaymentserver.dto.handoff;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * The response to be sent back to the wallet after a block is handed off.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HandoffResponse {

    private Status status;
    private String message, label;
    private ObjectNode data;
    
    public HandoffResponse(Status status) {
        this.status = status;
    }
    
    public HandoffResponse(Status status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public HandoffResponse(Status status, String message, String label) {
        this.status = status;
        this.message = message;
        this.label = label;
    }
    
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public ObjectNode getData() {
        return data;
    }
    
    public void setData(ObjectNode data) {
        this.data = data;
    }
    
    
    @JsonSerialize(using = Status.Serializer.class)
    public enum Status {
        ACCEPTED                     (0),
        ERR_INVALID                  (-1),
        ERR_REJECTED                 (-2),
        ERR_SEVICE_FAILURE           (-3),
        ERR_INSUFFICIENT_WORK        (-4),
        ERR_INCORRECT_BLOCK_STATE    (-5),
        ERR_INCORRECT_BLOCK_AMOUNT   (-6),
        ERR_BLOCK_ALREADY_ASSOCIATED (-7),
        ERR_ALREADY_PROVIDED         (-8),
        ERR_BLOCK_ALREADY_PUBLISHED  (-9);
        
        
        final int code;
        
        Status(int code) {
            this.code = code;
        }
        
        
        public int getCode() {
            return code;
        }
        
        public boolean isSuccessful() {
            return code >= 0;
        }
        
        
        static class Serializer extends JsonSerializer<Status> {
            @Override
            public void serialize(Status status, JsonGenerator jGen, SerializerProvider provider) throws IOException {
                jGen.writeNumber(status.getCode());
            }
        }
        
    }
}
