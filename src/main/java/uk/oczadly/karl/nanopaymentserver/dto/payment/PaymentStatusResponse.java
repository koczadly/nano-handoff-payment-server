package uk.oczadly.karl.nanopaymentserver.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.nanopaymentserver.entity.payment.Payment;

import java.time.Duration;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentStatusResponse {

    private final Payment.Status status;
    private final String reqAccount;
    private final String reqAmount, reqAmountRaw;
    private final String handoffBlockHash;
    private final Long timeRemaining;
    
    public PaymentStatusResponse(Payment payment) {
        this(payment.getStatus(), payment.getDepositAccount(), payment.getAmount(),
                payment.getHandoffHash() != null ? payment.getHandoffHash().toHexString() : null,
                (payment.getExpiration() == null || payment.getStatus().isFinalState()) ? null
                        : Math.max(Duration.between(Instant.now(), payment.getExpiration()).toSeconds(), 0));
    }
    
    public PaymentStatusResponse(Payment.Status status, NanoAccount reqAccount, NanoAmount reqAmount,
                                 String handoffBlockHash, Long timeRemaining) {
        this.status = status;
        this.reqAccount = reqAccount.toAddress();
        this.reqAmount = reqAmount.getAsNano().toPlainString();
        this.reqAmountRaw = reqAmount.toRawString();
        this.handoffBlockHash = handoffBlockHash;
        this.timeRemaining = timeRemaining;
    }
    
    
    public String getStatus() {
        return status.name().toLowerCase();
    }
    
    public String getStatusMessage() {
        return status.getMessage();
    }
    
    public String getReqAccount() {
        return reqAccount;
    }
    
    public String getReqAmountRaw() {
        return reqAmountRaw;
    }
    
    public String getReqAmount() {
        return reqAmount;
    }
    
    public String getHandoffBlockHash() {
        return handoffBlockHash;
    }
    
    public Long getTimeRemaining() {
        return timeRemaining;
    }
    
    public boolean isSuccessful() {
        return status == Payment.Status.COMPLETED;
    }
    
    public boolean isFailed() {
        return !isSuccessful() && status.isFinalState();
    }
    
    public boolean isActive() {
        return !status.isFinalState();
    }
    
}
