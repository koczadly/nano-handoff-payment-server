package uk.oczadly.karl.nanopaymentserver.entity.invoice;

import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.nanopaymentserver.entity.converters.AccountConverter;
import uk.oczadly.karl.nanopaymentserver.entity.converters.AmountConverter;
import uk.oczadly.karl.nanopaymentserver.entity.transaction.PaymentTransaction;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity(name = "invoices")
public class PaymentInvoice {

    @Id
    @GeneratedValue
    private UUID id;

    @Convert(converter = AccountConverter.class)
    private NanoAccount destination;

    @Column(name = "created", nullable = false)
    private Instant issuedDate;
    
    @Column(name = "expires")
    private Instant expirationDate;

    @Convert(converter = AmountConverter.class)
    private NanoAmount amount;
    
    @Column
    private boolean exactAmount;

    @Column
    private boolean singlePayment;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "invoice")
    private List<PaymentTransaction> transactions;


    public PaymentInvoice() {}

    public PaymentInvoice(NanoAccount destination, NanoAmount amount, Instant expirationDate, boolean exactAmount,
                          boolean singlePayment) {
        this.destination = destination;
        this.amount = amount;
        this.issuedDate = Instant.now();
        this.expirationDate = expirationDate;
        this.exactAmount = exactAmount;
        this.singlePayment = singlePayment;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public NanoAccount getDestination() {
        return destination;
    }

    public void setDestination(NanoAccount destination) {
        this.destination = destination;
    }

    public Instant getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(Instant issuedDate) {
        this.issuedDate = issuedDate;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public boolean hasExpired() {
        return getExpirationDate() != null && Instant.now().isAfter(getExpirationDate());
    }

    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }

    public NanoAmount getAmount() {
        return amount;
    }

    public void setAmount(NanoAmount amount) {
        this.amount = amount;
    }

    public boolean isExactAmount() {
        return exactAmount;
    }

    public void setExactAmount(boolean exactAmount) {
        this.exactAmount = exactAmount;
    }

    public boolean isSinglePayment() {
        return singlePayment;
    }

    public void setSinglePayment(boolean singlePayment) {
        this.singlePayment = singlePayment;
    }

    public List<PaymentTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<PaymentTransaction> transactions) {
        this.transactions = transactions;
    }

}
