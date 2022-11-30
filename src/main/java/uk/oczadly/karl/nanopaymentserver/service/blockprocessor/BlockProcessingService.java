package uk.oczadly.karl.nanopaymentserver.service.blockprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.oczadly.karl.jnano.model.block.Block;
import uk.oczadly.karl.jnano.rpc.response.ResponseBlockInfo;
import uk.oczadly.karl.nanopaymentserver.domain.SendBlock;
import uk.oczadly.karl.nanopaymentserver.entity.transaction.PaymentTransaction;
import uk.oczadly.karl.nanopaymentserver.exception.RpcQueryException;
import uk.oczadly.karl.nanopaymentserver.service.RpcService;
import uk.oczadly.karl.nanopaymentserver.service.payment.PaymentService;

@Service
public class BlockProcessingService {

    private static final Logger log = LoggerFactory.getLogger(BlockProcessingService.class);

    @Autowired private RpcService rpcService;
    @Autowired private PaymentService paymentService;


    @Async
    public void publishAndMonitorConfirmation(SendBlock block) {
        publishBlock(block.getContents());
        paymentService.updateTransactionStatus(block.getHash(), PaymentTransaction.Status.PENDING);
    }


    /**
     * Publishes the block to the Nano network, generating work if required.
     */
    public void publishBlock(Block block) {
        try {
            if (block.getWorkSolution() == null) {
                rpcService.generateWork(block);
            }
            rpcService.publishBlock(block);
            log.debug("Successfully processed handoff block {}", block.getHash());
        } catch (RpcQueryException e) {
            log.error("Failed to publish handoff block {}", block.getHash(), e);
            paymentService.updateTransactionStatus(block.getHash().toHexString(),
                    PaymentTransaction.Status.FAIL_UNPUBLISHED);
        }
    }

}
