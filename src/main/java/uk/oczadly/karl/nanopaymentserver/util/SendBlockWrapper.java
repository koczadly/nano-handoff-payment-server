package uk.oczadly.karl.nanopaymentserver.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.jnano.model.block.Block;
import uk.oczadly.karl.jnano.model.block.BlockDeserializer;
import uk.oczadly.karl.jnano.model.block.interfaces.IBlockAccount;
import uk.oczadly.karl.jnano.model.block.interfaces.IBlockBalance;
import uk.oczadly.karl.jnano.model.block.interfaces.IBlockPrevious;

import java.util.Optional;

/**
 * Wrapper for blocks which are sending funds, providing compatibility with future types. Does <em>not</em> support
 * legacy {@code send} types due to the lack of state information.
 */
public class SendBlockWrapper {
    
    private static final Logger log = LoggerFactory.getLogger(SendBlockWrapper.class);
    
    private final Block block;
    private final NanoAccount account, destination;
    private final HexData previous;
    private final NanoAmount balance;
    
    private SendBlockWrapper(Block block, NanoAccount account, NanoAccount destination, HexData previous,
                             NanoAmount balance) {
        this.block = block;
        this.account = account;
        this.destination = destination;
        this.previous = previous;
        this.balance = balance;
    }
    
    
    /**
     * @return the block contents
     */
    public Block getContents() {
        return block;
    }
    
    /**
     * @return the previous block
     */
    public HexData getPrevious() {
        return previous;
    }
    
    /**
     * @return the balance
     */
    public NanoAmount getBalance() {
        return balance;
    }
    
    /**
     * @return the account which created this block
     */
    public NanoAccount getAccount() {
        return account;
    }
    
    /**
     * @return the destination of the funds
     */
    public NanoAccount getDestination() {
        return destination;
    }
    
    
    /**
     * @return the send block wrapper object, or empty if not a send block
     */
    public static Optional<SendBlockWrapper> of(Block block) {
        if (block.getIntent().isSendFunds().boolLenient()) {
            // Block is (likely) sending funds
            if (block instanceof IBlockAccount && block instanceof IBlockPrevious
                    && block instanceof IBlockBalance) {
                NanoAccount account = ((IBlockAccount)block).getAccount();
                if (!block.verifySignature(account)) {
                    return Optional.empty(); // Invalid signature
                }
                
                Optional<NanoAccount> destination = BlockUtil.getSendDestination(block);
                if (destination.isPresent()) {
                    return Optional.of(new SendBlockWrapper(block, account, destination.get(),
                            ((IBlockPrevious)block).getPreviousBlockHash(),
                            ((IBlockBalance)block).getBalance()));
                }
            }
            // Couldn't identify block parameters, but was sending funds!
            log.warn("Identified send block, but couldn't recognize parameters for type {}",
                    block.getClass().getSimpleName());
        }
        return Optional.empty();
    }
    
    /**
     * @return the send block wrapper object, or empty if json is invalid or not a send block
     */
    public static Optional<SendBlockWrapper> tryParse(ObjectNode json) {
        if (json.has("type") && json.get("type").asText().equalsIgnoreCase("state")) {
            // jNano library requires state blocks to have a subtype property
            //todo: test this doesn't cause problems when processing (ie. accepting wrong subtype)
            if (!json.has("subtype")) {
                json.put("subtype", "send");
            }
        }
        try {
            return of(Block.parse(json.toString()));
        } catch (BlockDeserializer.BlockParseException e) {
            return Optional.empty();
        }
    }
    
}
