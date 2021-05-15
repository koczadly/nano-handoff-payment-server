package uk.oczadly.karl.nanopaymentserver.dto.handoff;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;

import java.nio.charset.StandardCharsets;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HandoffSpecification {

    private String id, address, amount;
    private final Map<String, HandoffChannel> channels = new HashMap<>();
    private boolean exact = true, work = true, reuse = false;
    
    
    public HandoffSpecification() {}
    
    public HandoffSpecification(UUID id, NanoAccount address, NanoAmount amount) {
        this(id.toString(), address.toAddress(), amount.toRawString());
    }
    
    public HandoffSpecification(String id, String address, String amount, HandoffChannel...channels) {
        this.id = id;
        this.address = address;
        this.amount = amount;
        Arrays.stream(channels).forEach(this::addChannel);
    }
    
    
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @JsonProperty("ad")
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    @JsonProperty("am")
    public String getAmount() {
        return amount;
    }
    
    public void setAmount(String amount) {
        this.amount = amount;
    }
    
    @JsonProperty("ch")
    public Map<String, HandoffChannel> getChannels() {
        return channels;
    }
    
    public void addChannel(HandoffChannel method) {
        this.channels.put(method.getType().toLowerCase(), method);
    }
    
    @JsonProperty("ex")
    public Boolean getExact() {
        return exact ? null : false; // To shorten URI, we can exclude default value
    }
    
    public void setExact(boolean exact) {
        this.exact = exact;
    }
    
    @JsonProperty("wk")
    public Boolean getWork() {
        return work ? null : false; // To shorten URI, we can exclude default value
    }
    
    public void setWork(boolean work) {
        this.work = work;
    }
    
    @JsonProperty("re")
    public Boolean getReuse() {
        return reuse ? true : null; // To shorten URI, we can exclude default value
    }
    
    public void setReuse(boolean reuse) {
        this.reuse = reuse;
    }
    
    
    public String toBase64() throws JsonProcessingException {
        String json = new ObjectMapper().writeValueAsString(this);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
    
}
