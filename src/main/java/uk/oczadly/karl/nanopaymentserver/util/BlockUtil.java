package uk.oczadly.karl.nanopaymentserver.util;

import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.block.Block;
import uk.oczadly.karl.jnano.model.block.SendBlock;
import uk.oczadly.karl.jnano.model.block.StateBlock;
import uk.oczadly.karl.jnano.model.block.StateBlockSubType;

import java.util.Optional;

/**
 * Helper methods for blocks.
 */
public class BlockUtil {
    
    /**
     * @return the destination of the send block, or null if not a send block
     */
    public static Optional<NanoAccount> getSendDestination(Block block) {
        if (block instanceof SendBlock) {
            return Optional.of(((SendBlock)block).getDestinationAccount());
        } else if (block instanceof StateBlock) {
            StateBlock sb = (StateBlock)block;
            if (sb.getSubType() == StateBlockSubType.SEND) {
                return Optional.of(sb.getLink().asAccount());
            }
        }
        return Optional.empty();
    }

}
