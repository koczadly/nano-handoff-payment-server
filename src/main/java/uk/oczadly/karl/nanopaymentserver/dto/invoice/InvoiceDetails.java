package uk.oczadly.karl.nanopaymentserver.dto.invoice;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.nanopaymentserver.entity.invoice.PaymentInvoice;

import java.time.Duration;
import java.time.Instant;

public class InvoiceDetails {

    private final Status status;
    private final Invoice invoice;
    private final String handoffUri;

    public InvoiceDetails(Status status, Invoice invoice, String handoffUri) {
        this.status = status;
        this.invoice = invoice;
        this.handoffUri = handoffUri;
    }

    public InvoiceDetails(PaymentInvoice invoice, String handoffUri) {
        this(   new InvoiceDetails.Status(
                    invoice.getExpirationDate()),
                new InvoiceDetails.Invoice(
                        invoice.getDestination(),
                        invoice.getAmount(),
                        !invoice.isExactAmount(),
                        invoice.isSinglePayment()),
                handoffUri);
    }


    public Status getStatus() {
        return status;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public String getHandoffUri() {
        return handoffUri;
    }


    public static class Status {
        private final Instant expiryDate;

        public Status(Instant expiryDate) {
            this.expiryDate = expiryDate;
        }


        @JsonProperty("expireDate")
        public Instant getExpiryDate() {
            return expiryDate;
        }

        @JsonProperty("expiresIn")
        public long getExpiresIn() {
            if (isActive()) {
                return Math.max(Duration.between(Instant.now(), expiryDate).toSeconds(), 0);
            } else {
                return 0;
            }
        }

        @JsonProperty("isActive")
        public boolean isActive() {
            if (expiryDate == null) return false;
            return expiryDate.isAfter(Instant.now());
        }
    }

    public static class Invoice {
        private final NanoAccount destination;
        private final SerializableAmount amount;
        private final boolean isVariableAmount, isOneTime;

        public Invoice(NanoAccount destination, NanoAmount amount, boolean isVariableAmount, boolean isOneTime) {
            this.destination = destination;
            this.amount = new SerializableAmount(amount);
            this.isVariableAmount = isVariableAmount;
            this.isOneTime = isOneTime;
        }

        public String getDestination() {
            return destination.toAddress();
        }

        public SerializableAmount getAmount() {
            return amount;
        }

        public boolean isVariableAmount() {
            return isVariableAmount;
        }

        public boolean isOneTime() {
            return isOneTime;
        }
    }

}
