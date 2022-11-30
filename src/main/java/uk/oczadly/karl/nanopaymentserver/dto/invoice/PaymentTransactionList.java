package uk.oczadly.karl.nanopaymentserver.dto.invoice;

import com.fasterxml.jackson.annotation.JsonInclude;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.nanopaymentserver.entity.transaction.PaymentTransaction;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentTransactionList {

    private final SerializableAmount totalConfirmed, totalPending;
    private final List<TransactionDetails> transactions;

    public PaymentTransactionList(NanoAmount totalConfirmed, NanoAmount totalPending,
                                  List<TransactionDetails> transactions) {
        this.totalConfirmed = new SerializableAmount(totalConfirmed);
        this.totalPending = new SerializableAmount(totalPending);
        this.transactions = transactions;
    }


    public SerializableAmount getTotalConfirmed() {
        return totalConfirmed;
    }

    public SerializableAmount getTotalPending() {
        return totalPending;
    }

    public List<TransactionDetails> getTransactions() {
        return transactions;
    }


    public static class TransactionDetails {
        private final String blockHash;
        private final SerializableAmount amount;
        private final PaymentTransaction.Status status;
        private final Instant processTimestamp;

        public TransactionDetails(String blockHash, NanoAmount amount, PaymentTransaction.Status status,
                                  Instant processTimestamp) {
            this.blockHash = blockHash;
            this.amount = new SerializableAmount(amount);
            this.status = status;
            this.processTimestamp = processTimestamp;
        }


        public String getBlockHash() {
            return blockHash;
        }

        public SerializableAmount getAmount() {
            return amount;
        }

        public boolean isConfirmed() {
            return status == PaymentTransaction.Status.CONFIRMED;
        }

        public String getStatus() {
            return status.name();
        }

        public Instant getProcessTimestamp() {
            return processTimestamp;
        }
    }
    
}
