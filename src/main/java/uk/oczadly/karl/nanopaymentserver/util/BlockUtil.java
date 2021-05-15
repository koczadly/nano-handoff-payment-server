package uk.oczadly.karl.nanopaymentserver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.jnano.model.block.*;
import uk.oczadly.karl.jnano.model.block.interfaces.IBlockAccount;
import uk.oczadly.karl.jnano.model.block.interfaces.IBlockBalance;
import uk.oczadly.karl.jnano.model.block.interfaces.IBlockLink;
import uk.oczadly.karl.jnano.model.block.interfaces.IBlockPrevious;

import java.util.Optional;

/**
 * Helper methods for blocks.
 */
public class BlockUtil {
    
    private static final Logger log = LoggerFactory.getLogger(BlockUtil.class);
    
    
    /**
     * @return the previous block, or empty if an open block
     */
    public static Optional<HexData> getPrevious(Block block) {
        if (block instanceof IBlockPrevious) {
            return Optional.ofNullable(((IBlockPrevious)block).getPreviousBlockHash());
        }
        return Optional.empty();
    }
    
    /**
     * @return the account balance, or empty if unknown
     */
    public static Optional<NanoAmount> getBalance(Block block) {
        if (block instanceof IBlockBalance) {
            return Optional.of(((IBlockBalance)block).getBalance());
        }
        return Optional.empty();
    }
    
    /**
     * @return the destination if sending funds, or empty if not a send block
     */
    public static Optional<NanoAccount> getSendDestination(Block block) {
        if (block instanceof IBlockLink) {
            LinkData link = ((IBlockLink)block).getLink();
            if (link.getIntent() == LinkData.Intent.DESTINATION_ACCOUNT) {
                return Optional.of(link.asAccount());
            }
        }
        if (block.getIntent().isSendFunds().bool()) {
            log.warn("Found send block, but did not recognize destination for type {}",
                    block.getClass().getSimpleName());
        }
        return Optional.empty();
    }
    
    /**
     * @return the account which created this block, or empty if unknown
     */
    public static Optional<NanoAccount> getAccount(Block block) {
        if (block instanceof IBlockAccount) {
            return Optional.of(((IBlockAccount)block).getAccount());
        }
        return Optional.empty();
    }
    
    /**
     * @return true if the signature matches or if the signature could not be verified, false if it's not valid
     */
    public static boolean tryVerifySignature(Block block) {
        if (block instanceof IBlockAccount) {
            return block.verifySignature(((IBlockAccount)block).getAccount());
        }
        return false;
    }

}
