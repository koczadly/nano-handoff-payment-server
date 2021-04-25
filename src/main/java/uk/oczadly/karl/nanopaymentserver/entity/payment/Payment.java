package uk.oczadly.karl.nanopaymentserver.entity.payment;

import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.nanopaymentserver.entity.converters.HexDataConverter;
import uk.oczadly.karl.nanopaymentserver.entity.converters.NanoAccountConverter;
import uk.oczadly.karl.nanopaymentserver.entity.converters.NanoAmountConverter;
import uk.oczadly.karl.nanopaymentserver.exception.InvalidPaymentStateException;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity(name = "payments")
public class Payment {

    @Id @GeneratedValue
    private UUID id;
    
    @Column(nullable = false)
    private Status status = Status.AWAITING_HANDOFF;
    
    @Column
    private Instant expiration;
    
    @Column(columnDefinition = "BINARY(32) NOT NULL")
    @Convert(converter = NanoAccountConverter.class)
    private NanoAccount depositAccount;
    
    @Column(columnDefinition = "BINARY(16) NOT NULL")
    @Convert(converter = NanoAmountConverter.class)
    private NanoAmount amount;
    
    @Column(columnDefinition = "BINARY(32) UNIQUE")
    @Convert(converter = HexDataConverter.class)
    private HexData handoffHash;
    
    
    public Payment() {}
    
    public Payment(NanoAccount depositAccount, NanoAmount amount, long timeoutMillis) {
        this.depositAccount = depositAccount;
        this.amount = amount;
        this.setExpiration(timeoutMillis);
    }
    
    
    public UUID getId() {
        return id;
    }
    
    public Instant getExpiration() {
        return expiration;
    }
    
    public boolean hasExpired() {
        Instant expiration = getExpiration();
        return !status.isFinalState() && expiration != null && Instant.now().isAfter(expiration);
    }
    
    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }
    
    public void setExpiration(long millis) {
        setExpiration(Instant.now().plusMillis(millis));
    }
    
    public NanoAccount getDepositAccount() {
        return depositAccount;
    }
    
    public NanoAmount getAmount() {
        return amount;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        if (this.status.isFinalState()) {
            throw new InvalidPaymentStateException("Payment is already in a finalized state.");
        }
        this.status = status;
        if (status.isFinalState()) {
            expiration = null;
            if (status != Status.COMPLETED) {
                handoffHash = null; // Allow hash to be used by another payment
            }
        }
    }
    
    public HexData getHandoffHash() {
        return handoffHash;
    }
    
    public void setHandoffHash(HexData handoffHash) {
        this.handoffHash = handoffHash;
    }
    
    
    public enum Status {
        /** Payment active and in progress. */
        AWAITING_HANDOFF     (false, "Awaiting block handoff"),
        /** Handoff has been accepted, awaiting confirmation of block. */
        HANDOFF_ACCEPTED     (false, "Awaiting block confirmation"),
        /** Payment finished successfully. */
        COMPLETED            (true,  "Payment successful"),
        /** Transaction failed as handoff was not made in time. */
        EXPIRED              (true,  "Payment was not made in time and has expired"),
        /** Transaction failed as block was not confirmed in time. */
        CONFIRMATION_TIMEOUT (true,  "Block confirmation took too long"),
        /** Transaction failed as accepted block was invalid (eg: fork, invalid amount, invalid destination). */
        INVALID_BLOCK        (true,  "Payment block was invalid"),
        /** Transaction invalidated as the payment was cancelled. */
        CANCELLED            (true,  "Payment was cancelled");
        
        
        final boolean finalState;
        final String message;
        
        Status(boolean finalState, String message) {
            this.finalState = finalState;
            this.message = message;
        }
        
        public boolean isFinalState() {
            return finalState;
        }
    
        public String getMessage() {
            return message;
        }
    }

}
