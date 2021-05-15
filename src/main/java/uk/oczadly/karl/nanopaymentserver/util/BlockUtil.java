package uk.oczadly.karl.nanopaymentserver.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffResponse;
import uk.oczadly.karl.nanopaymentserver.exception.HandoffException;

import java.util.Optional;

/**
 * Helper methods for blocks.
 */
public class BlockUtil {
    
    /**
     * @return the destination if sending funds, or empty if not a send block
     */
    public static Optional<NanoAccount> getSendDestination(Block block) {
        if (block instanceof SendBlock) {
            return Optional.of(((SendBlock)block).getDestinationAccount());
        } else if (block instanceof IBlockLink) {
            LinkData link = ((IBlockLink)block).getLink();
            if (link.getIntent() == LinkData.Intent.DESTINATION_ACCOUNT) {
                return Optional.of(link.asAccount());
            }
        }
        return Optional.empty();
    }
    
    
}
