package uk.oczadly.karl.nanopaymentserver.dto.handoff;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class HandoffChannel {

    private final String type;
    
    public HandoffChannel(String type) {
        this.type = type;
    }
    
    
    @JsonIgnore
    public String getType() {
        return type;
    }

}
