package uk.oczadly.karl.nanopaymentserver.dto.handoff;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class HandoffMethod {

    private final String type;
    
    public HandoffMethod(String type) {
        this.type = type;
    }
    
    
    @JsonIgnore
    public String getType() {
        return type;
    }

}
