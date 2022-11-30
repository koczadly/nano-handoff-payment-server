package uk.oczadly.karl.nanopaymentserver.util;

import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.jnano.model.block.*;
import uk.oczadly.karl.jnano.model.block.interfaces.IBlockLink;

import java.util.Optional;

/**
 * Additional helper methods for Nano.
 */
public class NanoUtil {
    
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
    
    
    public static String amountToString(NanoAmount amount, boolean useDecimal) {
        return useDecimal ? amount.getAsNano().toPlainString() : amount.toRawString();
    }
    
    public static NanoAmount amountFromString(String str, boolean useDecimal) {
        return useDecimal ? NanoAmount.valueOfNano(str) : NanoAmount.valueOfRaw(str);
    }
    
}
