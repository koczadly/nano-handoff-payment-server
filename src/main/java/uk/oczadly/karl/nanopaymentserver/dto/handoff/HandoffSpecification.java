package uk.oczadly.karl.nanopaymentserver.dto.handoff;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;

import java.nio.charset.StandardCharsets;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HandoffSpecification {

    private String id, address, amount;
    private Map<String, HandoffMethod> methods = new HashMap<>();
    private boolean exact = true, work = true, reuse = false;
    
    
    public HandoffSpecification() {}
    
    public HandoffSpecification(UUID id, NanoAccount address, NanoAmount amount) {
        this(id.toString(), address.toAddress(), amount.toRawString());
    }
    
    public HandoffSpecification(String id, String address, String amount) {
        this.id = id;
        this.address = address;
        this.amount = amount;
    }
    
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getAmount() {
        return amount;
    }
    
    public void setAmount(String amount) {
        this.amount = amount;
    }
    
    public Map<String, HandoffMethod> getMethods() {
        return methods;
    }
    
    public void addMethod(HandoffMethod method) {
        this.methods.put(method.getType().toLowerCase(), method);
    }
    
    public Boolean getExact() {
        return exact ? null : false; // To shorten URI, we can exclude default value
    }
    
    public void setExact(boolean exact) {
        this.exact = exact;
    }
    
    public Boolean getWork() {
        return work ? null : false; // To shorten URI, we can exclude default value
    }
    
    public void setWork(boolean work) {
        this.work = work;
    }
    
    public Boolean getReuse() {
        return reuse ? true : null; // To shorten URI, we can exclude default value
    }
    
    public void setReuse(boolean reuse) {
        this.reuse = reuse;
    }
    
    
    public String toBase64() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(this);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
    
}
