package uk.oczadly.karl.nanopaymentserver.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.jnano.model.block.Block;
import uk.oczadly.karl.jnano.model.block.BlockDeserializer;
import uk.oczadly.karl.jnano.model.block.StateBlock;
import uk.oczadly.karl.jnano.rpc.response.ResponseAccountInfo;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffRequest;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffResponse;
import uk.oczadly.karl.nanopaymentserver.entity.payment.Payment;
import uk.oczadly.karl.nanopaymentserver.exception.HandoffException;
import uk.oczadly.karl.nanopaymentserver.exception.RpcQueryException;
import uk.oczadly.karl.nanopaymentserver.properties.HandoffProperties;
import uk.oczadly.karl.nanopaymentserver.service.blockwatcher.BlockWatcherService;
import uk.oczadly.karl.nanopaymentserver.service.payment.PaymentService;
import uk.oczadly.karl.nanopaymentserver.util.BlockUtil;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BlockHandoffService {
    
    private static final Logger log = LoggerFactory.getLogger(BlockHandoffService.class);
    
    @Autowired private HandoffProperties handoffProperties;
    @Autowired private RpcService rpcService;
    @Autowired private PaymentService paymentService;
    @Autowired private BlockWatcherService blockWatcherService;
    
    private final ExecutorService blockPublishExecutor = Executors.newSingleThreadExecutor();
    
    
    /**
     * Processes and accepts the offered handoff data (hash/block).
     */
    public HandoffResponse handoff(HandoffRequest handoff) {
        if (handoff.getId() == null) {
            throw new HandoffException(HandoffResponse.Status.ERR_INVALID, "No ID provided.");
        }
        Payment payment = paymentService.getPayment(handoff.getId());
        
        // Check if payment is in correct state (waiting for handoff). This is just a preliminary check, and will be
        // verified again when updating the payment state.
        if (payment.getStatus() == Payment.Status.EXPIRED) {
            throw new HandoffException(HandoffResponse.Status.ERR_REJECTED, "Payment has expired.");
        } else if (payment.getStatus() != Payment.Status.AWAITING_HANDOFF) {
            throw new HandoffException(HandoffResponse.Status.ERR_ALREADY_PROVIDED);
        }
        
        // Attempt to handoff block
        if (handoff.getBlockContents() != null) {
            handoffBlock(payment, handoff.getBlockContents());
        } else if (handoff.getHash() != null) {
            handoffHash(payment.getId(), handoff.getHash());
        } else {
            throw new HandoffException(HandoffResponse.Status.ERR_INVALID, "No block or hash provided.");
        }
        
        // Create success response
        return new HandoffResponse(HandoffResponse.Status.ACCEPTED,
                formatResultString(handoffProperties.getSuccessMessage(), payment),
                formatResultString(handoffProperties.getSuccessLabel(), payment));
    }
    
    /**
     * Processes and accepts the offered block handoff.
     */
    public void handoffBlock(Payment payment, ObjectNode blockJson) {
        // Try to parse the block contents
        if (!blockJson.has("subtype")) {
            blockJson.put("subtype", "send"); // jNano library requires state blocks to have a subtype property
        }
        StateBlock block;
        try {
            block = StateBlock.parse(blockJson.toString());
        } catch (BlockDeserializer.BlockParseException e) {
            // Couldn't parse as state block
            throw new HandoffException(HandoffResponse.Status.ERR_INVALID, "Invalid block.");
        }
        
        validateBlockContents(payment, block);
        registerHandoff(payment.getId(), block.getHash());
        processBlock(payment.getId(), block);
    }
    
    /** Performs preliminary block property and state validation, throwing a HandoffException if invalid. */
    private void validateBlockContents(Payment payment, Block block) {
        // Retrieve block specifics
        Optional<NanoAccount> account = BlockUtil.getAccount(block);
        Optional<NanoAccount> destination = BlockUtil.getSendDestination(block);
        Optional<HexData> previous = BlockUtil.getPrevious(block);
        Optional<NanoAmount> balance = BlockUtil.getBalance(block);
        
        // Check block parameters
        if (destination.isEmpty() || account.isEmpty() || previous.isEmpty() || balance.isEmpty())
            throw new HandoffException(HandoffResponse.Status.ERR_INVALID, "Incorrect or unsupported block type.");
        
        // Check block signature
        if (!BlockUtil.tryVerifySignature(block))
            throw new HandoffException(HandoffResponse.Status.ERR_INVALID, "Invalid block signature.");
        // Ensure block is sending to correct account
        if (!destination.get().equalsIgnorePrefix(payment.getDepositAccount()))
            throw new HandoffException(HandoffResponse.Status.ERR_INVALID, "Incorrect destination.");
        Optional<ResponseAccountInfo> accountInfo = rpcService.getAccountInfo(account.get());
        // Check that the account which created the block exists
        if (accountInfo.isEmpty())
            throw new HandoffException(HandoffResponse.Status.ERR_INVALID, "Invalid block.");
        // Ensure block doesn't already exist (as frontier, at least - if it exists behind the frontier, then it will
        // be caught by the previous field check performed after).
        if (block.getHash().equalsValue(accountInfo.get().getFrontierBlockHash()))
            throw new HandoffException(HandoffResponse.Status.ERR_BLOCK_ALREADY_PUBLISHED);
        // Check previous == frontier
        if (!previous.get().equalsValue(accountInfo.get().getFrontierBlockHash()))
            throw new HandoffException(HandoffResponse.Status.ERR_INCORRECT_BLOCK_STATE, "Incorrect previous.");
        // Check that balance has decreased (block balance < account balance)
        if (balance.get().compareTo(accountInfo.get().getBalance()) >= 0)
            throw new HandoffException(HandoffResponse.Status.ERR_INCORRECT_BLOCK_STATE, "Incorrect balance.");
        // Ensure sending amount matches the payment amount
        NanoAmount amount = accountInfo.get().getBalance().subtract(balance.get());
        if (!amount.equals(payment.getAmount()))
            throw new HandoffException(HandoffResponse.Status.ERR_INCORRECT_BLOCK_AMOUNT);
        // Ensure that work difficulty is sufficient
        if (!rpcService.isWorkValidForSend(block)) {
            if (handoffProperties.getWorkGen()) {
                // Work will be computed during processing
                block.setWorkSolution(null);
            } else if (block.getWorkSolution() != null) {
                // Difficulty of provided work too low
                throw new HandoffException(HandoffResponse.Status.ERR_INSUFFICIENT_WORK);
            } else {
                // No work provided
                throw new HandoffException(HandoffResponse.Status.ERR_INSUFFICIENT_WORK,
                        "Service doesn't support work generation.");
            }
        }
    }
    
    /**
     * Processes and accepts the offered hash handoff.
     */
    public void handoffHash(UUID id, String rawHash) {
        HexData hashHex;
        try {
            hashHex = new HexData(rawHash.toUpperCase(), 32); // 32 bytes == 64 hex chars
        } catch (IllegalArgumentException e) {
            throw new HandoffException(HandoffResponse.Status.ERR_INVALID, "Invalid hash.");
        }
        registerHandoff(id, hashHex);
    }
    
    /**
     * Associates the given block hash with the specified payment ID, and watches the block for confirmation.
     */
    public void registerHandoff(UUID id, HexData hash) {
        // Verify hash doesn't exist in network (must be published after handoff)
        if (rpcService.getBlockInfo(hash).isPresent()) {
            throw new HandoffException(HandoffResponse.Status.ERR_BLOCK_ALREADY_PUBLISHED);
        }
        // Attempt to associate handoff with payment in database
        Payment paymentReq = paymentService.acceptHandoff(id, hash);
        log.info("Accepted handoff hash {} for payment #{}", hash, paymentReq.getId());
        // Register payment and hash to watcher service
        blockWatcherService.watch(paymentReq);
    }
    
    /**
     * Asynchronously publish the block to the Nano network, generating work if required.
     */
    public void processBlock(UUID paymentId, Block block) {
        blockPublishExecutor.submit(() -> {
            try {
                if (block.getWorkSolution() == null) {
                    rpcService.generateWork(block);
                }
                rpcService.publishBlock(block);
            } catch (RpcQueryException e) {
                log.error("Failed to publish block.", e);
                paymentService.setPaymentState(paymentId, Payment.Status.INVALID_BLOCK);
            }
        });
    }
    
    
    private String formatResultString(String str, Payment payment) {
        return str == null ? null : str
                .replace("{id}", payment.getId().toString())
                .replace("{shortId}", payment.getId().toString().substring(24, 36));
    }
    
}
