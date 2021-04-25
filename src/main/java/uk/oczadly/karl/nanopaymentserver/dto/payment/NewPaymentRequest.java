package uk.oczadly.karl.nanopaymentserver.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Transient;
import uk.oczadly.karl.jnano.model.NanoAmount;

import java.math.BigInteger;

/**
 * @author Karl Oczadly
 */
public class NewPaymentRequest {

    private String account;
    private String amount, amountRaw;
    
    
    public String getAccount() {
        return account;
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
    
    public String getAmount() {
        return amount;
    }
    
    public void setAmount(String amount) {
        this.amount = amount;
    }
    
    public String getAmountRaw() {
        return amountRaw;
    }
    
    @JsonProperty("amount_raw")
    public void setAmountRaw(String amountRaw) {
        this.amountRaw = amountRaw;
    }
    
}
