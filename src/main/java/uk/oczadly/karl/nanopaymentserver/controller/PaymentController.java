package uk.oczadly.karl.nanopaymentserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.nanopaymentserver.dto.invoice.InvoiceDetails;
import uk.oczadly.karl.nanopaymentserver.dto.invoice.NewInvoiceRequest;
import uk.oczadly.karl.nanopaymentserver.dto.invoice.NewInvoiceResponse;
import uk.oczadly.karl.nanopaymentserver.dto.invoice.PaymentTransactionList;
import uk.oczadly.karl.nanopaymentserver.entity.invoice.PaymentInvoice;
import uk.oczadly.karl.nanopaymentserver.entity.transaction.PaymentTransaction;
import uk.oczadly.karl.nanopaymentserver.service.BlockHandoffService;
import uk.oczadly.karl.nanopaymentserver.service.payment.PaymentService;

import java.util.*;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    @Autowired private PaymentService paymentService;
    @Autowired private BlockHandoffService handoffService;


    /**
     * Creates a new single (one-time) outstanding payment.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/newInvoice")
    public NewInvoiceResponse postNewInvoice(@RequestBody NewInvoiceRequest request) {
        return paymentService.createNewInvoice(request);
    }

    /**
     * Returns the payment details of the associated invoice ID.
     */
    @GetMapping("/{id}")
    public InvoiceDetails getDetails(@PathVariable UUID id) {
        PaymentInvoice invoice = paymentService.getInvoice(id);
        String handoffUri = handoffService.encodeHandoffUri(invoice);
        return new InvoiceDetails(invoice, handoffUri);
    }

    /**
     * Returns a list of transactions associated with the given invoice ID.
     */
    @GetMapping("/{id}/transactions")
    public PaymentTransactionList getTransactions(@PathVariable UUID id) {
        PaymentInvoice invoice = paymentService.getInvoice(id);

        NanoAmount totalConfirmed = NanoAmount.ZERO;
        NanoAmount totalPending = NanoAmount.ZERO;
        List<PaymentTransactionList.TransactionDetails> txList = new ArrayList<>();

        for (PaymentTransaction tx : invoice.getTransactions()) {
            if (tx.getStatus().isSuccessful()) {
                totalConfirmed = totalConfirmed.add(tx.getAmount());
            } else if (tx.getStatus().isInProgress()) {
                totalPending = totalPending.add(tx.getAmount());
            }
            txList.add(new PaymentTransactionList.TransactionDetails(
                    tx.getBlockHash(), tx.getAmount(), tx.getStatus(), tx.getProcessTimestamp()
            ));
        }
        return new PaymentTransactionList(totalConfirmed, totalPending, txList);
    }

//    @DeleteMapping("/{id}")
//    public String deleteCancel(@PathVariable UUID id) {
//        return "{}"; //todo
//    }

}
