package uk.oczadly.karl.nanopaymentserver.service.blockwatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.jnano.model.block.Block;
import uk.oczadly.karl.jnano.rpc.response.ResponseBlockInfo;
import uk.oczadly.karl.nanopaymentserver.entity.payment.Payment;
import uk.oczadly.karl.nanopaymentserver.exception.InvalidPaymentStateException;
import uk.oczadly.karl.nanopaymentserver.properties.PaymentProperties;
import uk.oczadly.karl.nanopaymentserver.service.payment.PaymentService;
import uk.oczadly.karl.nanopaymentserver.service.RpcService;
import uk.oczadly.karl.nanopaymentserver.util.BlockUtil;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class actively watches for block confirmations, updating the payment state when confirmation is received.
 *
 * Internally a websocket connection to the node is used, receiving real-time block confirmations. Due to the uncertain
 * nature of websockets (connection may drop, packets may be lost) we cannot only rely on this mechanism; we also
 * manually poll the blocks using block_info every 30 seconds to ensure transactions don't get stuck in limbo.
 */
@Service
public class BlockConfirmationWatcherService {
    
    private static final Logger log = LoggerFactory.getLogger(BlockConfirmationWatcherService.class);
    
    @Autowired private PaymentProperties paymentProperties;
    @Autowired private PaymentService paymentService;
    @Autowired private RpcService rpcService;
    
    private final ConfirmationWebsocketClient wsClient;
    private final Map<HexData, Payment> watchingBlocks = new ConcurrentHashMap<>();
    
    @Autowired
    public BlockConfirmationWatcherService(RpcService rpcService) {
        this.wsClient = new ConfirmationWebsocketClient(rpcService, new ConfirmationListener());
        this.wsClient.connect(); // Initialize websocket connection
    }
    
    
    public void watch(Payment payment) {
        log.debug("Watching hash {}", payment.getHandoffHash());
        if (watchingBlocks.putIfAbsent(payment.getHandoffHash(), payment) == null) {
            // Manually check confirmation via RPC
            checkForConfirmation(payment, payment.getHandoffHash());
        }
    }

    public boolean unwatch(HexData hash) {
        log.debug("Removing hash {} from watchlist", hash);
        return watchingBlocks.remove(hash) != null;
    }
    
    public void checkActivePaymentsForConfirmation() {
        log.debug("Polling all handoff blocks for confirmation...");
        watchingBlocks.forEach((hash, payment) -> checkForConfirmation(payment, hash));
    }
    
    public void checkForConfirmation(Payment payment, HexData hash) {
        rpcService.getBlockInfo(hash)
                .filter(ResponseBlockInfo::isConfirmed)
                .ifPresent(blockInfo -> handleConfirmation(
                        payment, blockInfo.getContents(), blockInfo.getAmount()));
    }
    
    /** Called upon confirmation of a block */
    private void handleConfirmation(Payment payment, Block block, NanoAmount amount) {
        log.debug("Handling confirmation for block {}", block.getHash());
        Optional<NanoAccount> destination = BlockUtil.getSendDestination(block);
        try {
            if (amount != null && amount.compareTo(payment.getAmount()) >= 0 && destination.isPresent()
                    && destination.get().equalsIgnorePrefix(payment.getDepositAccount())) {
                // Mark transaction as complete
                paymentService.setPaymentState(payment.getId(), Payment.Status.COMPLETED);
            } else {
                // Not enough sent/or sending to wrong destination/not sending funds
                paymentService.setPaymentState(payment.getId(), Payment.Status.INVALID_BLOCK);
            }
        } catch (InvalidPaymentStateException e) {
            log.warn("Couldn't update payment state", e);
        }
        unwatch(block.getHash());
    }
    
    
    /**
     * Handles block confirmation notifications received from the websocket.
     */
    private class ConfirmationListener implements ConfirmationWebsocketClient.Listener {
        @Override
        public void onConfirmation(Block block, NanoAmount amount) {
            Payment matchingPayment = watchingBlocks.get(block.getHash());
            if (matchingPayment != null) {
                handleConfirmation(matchingPayment, block, amount);
            }
        }
    }
    
}
