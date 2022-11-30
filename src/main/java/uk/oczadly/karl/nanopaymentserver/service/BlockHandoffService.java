package uk.oczadly.karl.nanopaymentserver.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.oczadly.karl.jnano.rpc.response.ResponseAccountInfo;
import uk.oczadly.karl.nanopaymentserver.domain.SendBlock;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffDispatchRequest;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffDispatchResponse;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffPaymentRequest;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HttpsHandoffChannel;
import uk.oczadly.karl.nanopaymentserver.entity.invoice.PaymentInvoice;
import uk.oczadly.karl.nanopaymentserver.entity.transaction.PaymentTransaction;
import uk.oczadly.karl.nanopaymentserver.exception.HandoffException;
import uk.oczadly.karl.nanopaymentserver.exception.InvoiceNotFoundException;
import uk.oczadly.karl.nanopaymentserver.properties.HandoffProperties;
import uk.oczadly.karl.nanopaymentserver.service.blockprocessor.BlockProcessingService;
import uk.oczadly.karl.nanopaymentserver.service.payment.PaymentService;

import java.util.Optional;
import java.util.UUID;

/**
 * The block handoff service provides methods for working with the handoff protocol, including the creation/encoding of
 * handoff payment requests, and the acceptance of incoming blocks.
 */
@Service
public class BlockHandoffService {
    
    private static final Logger log = LoggerFactory.getLogger(BlockHandoffService.class);
    
    @Autowired private HandoffProperties handoffProperties;
    @Autowired private RpcService rpcService;
    @Autowired private PaymentService paymentService;
    @Autowired private BlockProcessingService blockProcessingService;


    public String encodeHandoffUri(PaymentInvoice invoice) {
        HandoffPaymentRequest request = new HandoffPaymentRequest(
                invoice.getId(),
                invoice.getDestination().toAddress(),
                invoice.getAmount().toRawString());
        request.setWorkRequired(!handoffProperties.getGenerateWork());
        request.setVariableAmount(request.isVariableAmount());
        request.addChannel(new HttpsHandoffChannel(handoffProperties.getUrl()));
        return request.encodeUri();
    }


    /**
     * Validates, processes and publishes the offered block.
     */
    public HandoffDispatchResponse processBlockHandoff(HandoffDispatchRequest handoff) {
        log.debug("Processing incoming handoff for payment {}", handoff.getPaymentId());

        // Locate invoice matching given ID
        if (handoff.getPaymentId() == null) {
            throw new HandoffException(HandoffDispatchResponse.Status.ERR_INVALID, "No payment ID provided.");
        }
        PaymentInvoice invoice;
        try {
            invoice = paymentService.getInvoice(UUID.fromString(handoff.getPaymentId()));
        } catch (InvoiceNotFoundException | IllegalArgumentException e) {
            throw new HandoffException(HandoffDispatchResponse.Status.ERR_INVALID, "Payment is unrecognized.");
        }

        // Parse and validate block contents
        if (handoff.getBlockContents() == null) {
            throw new HandoffException(HandoffDispatchResponse.Status.ERR_INVALID, "No block provided.");
        }
        SendBlock block = parseAndValidateBlock(invoice, handoff.getBlockContents());

        // Accept handoff into database
        acceptHandoff(new PaymentTransaction(block.getHash(), block.getAmount(), invoice));

        // Publish block and watch for confirmation
        blockProcessingService.publishAndMonitorConfirmation(block);

        // Return success response
        return new HandoffDispatchResponse(HandoffDispatchResponse.Status.ACCEPTED,
                formatResultString(handoffProperties.getSuccessMessage(), invoice),
                formatResultString(handoffProperties.getSuccessLabel(), invoice));
    }

    
    /** Performs preliminary block property and state validation, throwing a HandoffException if invalid. */
    private SendBlock parseAndValidateBlock(PaymentInvoice invoice, ObjectNode blockJson) {
        // Parse the block
        SendBlock block = SendBlock.tryParse(blockJson)
                .orElseThrow(() -> new HandoffException(
                        HandoffDispatchResponse.Status.ERR_INVALID, "Invalid or unsupported block."));

        // Ensure block is sending to correct account
        if (!block.getDestination().equalsIgnorePrefix(invoice.getDestination()))
            throw new HandoffException(HandoffDispatchResponse.Status.ERR_INVALID, "Incorrect destination.");
        // Check that the account which created the block exists
        Optional<ResponseAccountInfo> accountInfo = rpcService.getAccountInfo(block.getAccount());
        if (accountInfo.isEmpty())
            throw new HandoffException(HandoffDispatchResponse.Status.ERR_INVALID, "Invalid block.");
        // Ensure block doesn't already exist (as frontier, at least - if it exists before the frontier, then it will
        // be caught rejected at the next stage).
        if (block.getContents().getHash().equalsValue(accountInfo.get().getFrontierBlockHash()))
            throw new HandoffException(HandoffDispatchResponse.Status.ERR_BLOCK_ALREADY_PUBLISHED);
        // Check that block comes after head block
        if (!block.getPrevious().equalsValue(accountInfo.get().getFrontierBlockHash()))
            throw new HandoffException(HandoffDispatchResponse.Status.ERR_INCORRECT_BLOCK_STATE, "Incorrect previous.");
        // Check that balance has decreased (block balance < account balance)
        if (block.getBalance().compareTo(accountInfo.get().getBalance()) >= 0)
            throw new HandoffException(HandoffDispatchResponse.Status.ERR_INCORRECT_BLOCK_STATE, "Incorrect balance.");
        // Calculate block amount
        block.setAmount(accountInfo.get().getBalance().subtract(block.getBalance()));
        // Ensure block is sending right amount
        if ((invoice.isExactAmount() && !block.getAmount().equals(invoice.getAmount())) ||
                block.getAmount().compareTo(invoice.getAmount()) < 0)
            throw new HandoffException(HandoffDispatchResponse.Status.ERR_INCORRECT_AMOUNT);
        // Ensure that work difficulty is sufficient
        if (!rpcService.isWorkValidForSend(block.getContents())) {
            if (handoffProperties.getGenerateWork()) {
                // Work will be computed during processing
                block.getContents().setWorkSolution(null);
            } else {
                // Difficulty of provided work too low
                throw new HandoffException(HandoffDispatchResponse.Status.ERR_INSUFFICIENT_WORK);
            }
        }
        return block;
    }
    
    /**
     * Associates the given block hash with the specified payment ID, and watches the block for confirmation.
     */
    public void acceptHandoff(PaymentTransaction transaction) {
        // Verify hash doesn't exist in network (must be published after handoff)
        if (rpcService.getBlockInfo(transaction.getBlockHash()).isPresent()) {
            throw new HandoffException(HandoffDispatchResponse.Status.ERR_BLOCK_ALREADY_PUBLISHED);
        }

        // Attempt to associate handoff with payment in database
        paymentService.registerTransaction(transaction);
        log.info("Accepted handoff (block {}) for payment #{}",
                transaction.getBlockHash(), transaction.getInvoice().getId());
    }
    
    
    private String formatResultString(String str, PaymentInvoice payment) {
        return str == null ? null : str
                .replace("{id}", payment.getId().toString())
                .replace("{shortId}", payment.getId().toString().substring(24, 36));
    }
    
}
