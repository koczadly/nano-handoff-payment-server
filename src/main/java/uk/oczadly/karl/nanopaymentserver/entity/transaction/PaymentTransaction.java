package uk.oczadly.karl.nanopaymentserver.entity.transaction;

import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.nanopaymentserver.entity.converters.AmountConverter;
import uk.oczadly.karl.nanopaymentserver.entity.invoice.PaymentInvoice;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity(name = "transactions")
public class PaymentTransaction {

    // todo: Replace hash string with blob. Hibernate disallows AttributeConverters on ID fields
    @Id
    @Column(nullable = false)
    private String blockHash;

    @Column(nullable = false)
    @Convert(converter = AmountConverter.class)
    private NanoAmount amount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private PaymentInvoice invoice;
    
    @Column
    private Instant processTimestamp;
    
    @Column
    private Status status;


    public PaymentTransaction() {}

    public PaymentTransaction(String blockHash, NanoAmount amount, PaymentInvoice invoice) {
        this.blockHash = blockHash.toUpperCase();
        this.amount = amount;
        this.invoice = invoice;
        this.processTimestamp = Instant.now();
        this.status = Status.PENDING_PUBLISH;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash.toUpperCase();
    }

    public NanoAmount getAmount() {
        return amount;
    }

    public void setAmount(NanoAmount amount) {
        this.amount = amount;
    }

    public PaymentInvoice getInvoice() {
        return invoice;
    }

    public void setInvoice(PaymentInvoice invoice) {
        this.invoice = invoice;
    }

    public Instant getProcessTimestamp() {
        return processTimestamp;
    }

    public void setProcessTimestamp(Instant processTimestamp) {
        this.processTimestamp = processTimestamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    public enum Status {
        /** Transaction has been successfully confirmed. */
        CONFIRMED,
        /** Handoff has been accepted, but transaction is still awaiting confirmation on the Nano network. */
        PENDING,
        /** Handoff has been accepted, but block has yet to propagate the Nano network. */
        PENDING_PUBLISH,
        /** Transaction was not confirmed in time and has been rejected. */
        FAIL_TIMEOUT,
        /** Payment invoice was cancelled before transaction was validated. */
        FAIL_CANCELLED,
        /** An error occurred when publishing the block to the network. */
        FAIL_UNPUBLISHED;
        
        public boolean isSuccessful() {
            return this == CONFIRMED;
        }

        public boolean isInProgress() {
            return this == PENDING || this == PENDING_PUBLISH;
        }

        public boolean isFinalState() {
            return !isInProgress();
        }
    }
    
}
