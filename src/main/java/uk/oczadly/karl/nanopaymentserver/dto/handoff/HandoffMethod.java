package uk.oczadly.karl.nanopaymentserver.dto.handoff;

public abstract class HandoffMethod {

    private String type;
    
    public HandoffMethod(String type) {
        this.type = type;
    }
    
    
    public String getType() {
        return type;
    }

}
