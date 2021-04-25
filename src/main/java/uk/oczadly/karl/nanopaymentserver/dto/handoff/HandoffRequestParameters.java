package uk.oczadly.karl.nanopaymentserver.dto.handoff;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Karl Oczadly
 */
public class HandoffRequestParameters {

    private String id, hash;
    @JsonProperty("block")
    private ObjectNode blockContents;
    
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
    
    public ObjectNode getBlockContents() {
        return blockContents;
    }
    
    public void setBlockContents(ObjectNode blockContents) {
        this.blockContents = blockContents;
    }
}
