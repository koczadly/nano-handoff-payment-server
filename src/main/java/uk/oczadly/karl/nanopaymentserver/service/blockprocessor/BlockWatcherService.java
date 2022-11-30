package uk.oczadly.karl.nanopaymentserver.service.blockprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.oczadly.karl.jnano.rpc.response.ResponseBlockInfo;
import uk.oczadly.karl.nanopaymentserver.entity.transaction.PaymentTransaction;
import uk.oczadly.karl.nanopaymentserver.service.RpcService;
import uk.oczadly.karl.nanopaymentserver.service.payment.PaymentService;

import java.util.List;
import java.util.Optional;

/**
 * This service continually watches blocks for their confirmation. Upon successful confirmation, the state of the
 * associated transaction will be updated.
 */
@Service
public class BlockWatcherService {

    private static final Logger log = LoggerFactory.getLogger(BlockWatcherService.class);

    @Autowired private RpcService rpcService;
    @Autowired private PaymentService paymentService;


    public void checkForConfirmations() {
        List<PaymentTransaction> pendingTx = paymentService.getPendingTransactions();
        log.debug("Checking {} pending transactions for confirmation...", pendingTx.size());
        pendingTx.forEach(this::checkConfirmation);
    }

    public void checkConfirmation(PaymentTransaction transaction) {
        boolean isConfirmed = rpcService.getBlockInfo(transaction.getBlockHash())
                .map(ResponseBlockInfo::isConfirmed)
                .orElse(false);
        if (isConfirmed) {
            log.info("Block {} has been flagged as confirmed", transaction.getBlockHash());
            paymentService.updateTransactionStatus(transaction.getBlockHash(), PaymentTransaction.Status.CONFIRMED);
        }
    }

}
