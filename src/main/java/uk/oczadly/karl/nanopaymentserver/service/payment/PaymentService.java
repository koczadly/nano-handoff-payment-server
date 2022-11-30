package uk.oczadly.karl.nanopaymentserver.service.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.nanopaymentserver.dto.invoice.NewInvoiceRequest;
import uk.oczadly.karl.nanopaymentserver.dto.invoice.NewInvoiceResponse;
import uk.oczadly.karl.nanopaymentserver.entity.invoice.PaymentInvoice;
import uk.oczadly.karl.nanopaymentserver.entity.invoice.PaymentInvoiceRepository;
import uk.oczadly.karl.nanopaymentserver.entity.transaction.PaymentTransaction;
import uk.oczadly.karl.nanopaymentserver.entity.transaction.PaymentTransactionRepository;
import uk.oczadly.karl.nanopaymentserver.exception.BadRequestException;
import uk.oczadly.karl.nanopaymentserver.exception.IllegalPaymentStateException;
import uk.oczadly.karl.nanopaymentserver.exception.InvoiceNotFoundException;
import uk.oczadly.karl.nanopaymentserver.properties.PaymentProperties;
import uk.oczadly.karl.nanopaymentserver.service.BlockHandoffService;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The payment service facilitates the creation, cancellation and retrieval of payments, primarily with the
 * {@link PaymentInvoice} and {@link PaymentTransaction} entities.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    public static final List<PaymentTransaction.Status> PENDING_STATUSES =
            Arrays.stream(PaymentTransaction.Status.values())
                    .filter(PaymentTransaction.Status::isInProgress)
                    .collect(Collectors.toUnmodifiableList());

    @Autowired private PaymentProperties paymentProperties;
    @Autowired private BlockHandoffService handoffService;
    @Autowired private PaymentInvoiceRepository invoiceRepo;
    @Autowired private PaymentTransactionRepository transactionRepo;
    

    public PaymentInvoice getInvoice(UUID id) {
        return invoiceRepo.findById(id)
                .orElseThrow(InvoiceNotFoundException::new);
    }

    public NewInvoiceResponse createNewInvoice(NewInvoiceRequest invoiceSpec) {
        // Compute invoice values
        NanoAmount amount = computeInvoiceAmount(invoiceSpec);
        NanoAccount destination = computeInvoiceDestination(invoiceSpec);
        Instant expireDate = computeInvoiceExpireDate(invoiceSpec);

        // Create invoice entity
        PaymentInvoice invoice = invoiceRepo.save(
                new PaymentInvoice(destination, amount, expireDate, !invoiceSpec.isVariableAmount(),
                        invoiceSpec.isOneTime()));

        // Encode handoff and return
        String handoffUri = handoffService.encodeHandoffUri(invoice);
        return new NewInvoiceResponse(invoice.getId(), handoffUri);
    }

    public PaymentTransaction registerTransaction(PaymentTransaction transaction) {
        return transactionRepo.save(transaction);
    }

    /**
     * @throws EntityNotFoundException if the transaction matching the hash doesn't exist
     * @throws IllegalPaymentStateException if the transaction is already in a finalized state
     */
    public void updateTransactionStatus(String hash, PaymentTransaction.Status status) {
        PaymentTransaction transaction = transactionRepo.findById(hash)
                .orElseThrow(EntityNotFoundException::new);
        if (transaction.getStatus().isFinalState()) {
            throw new IllegalPaymentStateException("Transaction already has a finalized status.");
        }
        transaction.setStatus(status);
        transactionRepo.save(transaction);
    }

    public List<PaymentTransaction> getPendingTransactions() {
        List<PaymentTransaction> transactions = transactionRepo.findByStatusIn(PENDING_STATUSES);
        ListIterator<PaymentTransaction> iterator = transactions.listIterator();
        while (iterator.hasNext()) {
            PaymentTransaction transaction = iterator.next();
            iterator.set(checkTransactionState(transaction));
        }
        return transactions;
    }

    /**
     * @throws EntityNotFoundException if the transaction matching the hash doesn't exist
     */
    public PaymentTransaction getTransaction(String blockHash) {
        return transactionRepo.findById(blockHash)
                .map(this::checkTransactionState)
                .orElseThrow(EntityNotFoundException::new);
    }

    /**
     * Check if transaction should have expired; if it has, then the status will be overwritten, saving the new entity
     * to the database.
     */
    private PaymentTransaction checkTransactionState(PaymentTransaction transaction) {
        if (transaction.getStatus().isInProgress() && Instant.now().compareTo(transaction.getProcessTimestamp()
                .plusMillis(paymentProperties.getConfirmationTimeout())) > 0) {
            transaction.setStatus(PaymentTransaction.Status.FAIL_TIMEOUT);
            return transactionRepo.save(transaction);
        }
        return transaction;
    }


    private NanoAmount computeInvoiceAmount(NewInvoiceRequest invoice) {
        if (invoice.getAmount().compareTo(paymentProperties.getMinimumAmount()) < 0) {
            if (invoice.isVariableAmount()) {
                // Force the global minimum amount
                return paymentProperties.getMinimumAmount();
            } else {
                // Fixed amount is below the minimum
                throw new BadRequestException("Amount is smaller than minimum receivable.");
            }
        }
        return invoice.getAmount();
    }

    private NanoAccount computeInvoiceDestination(NewInvoiceRequest invoice) {
        if (invoice.getDestination() != null) {
            return invoice.getDestination();
        } else if (paymentProperties.getDefaultDestination() != null) {
            return paymentProperties.getDefaultDestination();
        } else {
            throw new BadRequestException("No destination address has been defined.");
        }
    }

    private Instant computeInvoiceExpireDate(NewInvoiceRequest invoice) {
        if (invoice.getExpireDate() != null) {
            // From absolute date
            return invoice.getExpireDate();
        } else if (invoice.getExpireTime() > 0) {
            // From given offset
            return Instant.now().plusMillis(invoice.getExpireTime());
        } else {
            // From default configuration
            return Instant.now().plusMillis(paymentProperties.getDefaultTimeout());
        }
    }

}
