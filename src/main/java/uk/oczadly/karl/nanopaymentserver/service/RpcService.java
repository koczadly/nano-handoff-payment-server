package uk.oczadly.karl.nanopaymentserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.block.Block;
import uk.oczadly.karl.jnano.model.block.interfaces.IBlockPrevious;
import uk.oczadly.karl.jnano.model.work.WorkSolution;
import uk.oczadly.karl.jnano.rpc.RpcQueryNode;
import uk.oczadly.karl.jnano.rpc.exception.RpcEntityNotFoundException;
import uk.oczadly.karl.jnano.rpc.exception.RpcException;
import uk.oczadly.karl.jnano.rpc.exception.RpcExternalException;
import uk.oczadly.karl.jnano.rpc.request.node.*;
import uk.oczadly.karl.jnano.rpc.response.ResponseAccountInfo;
import uk.oczadly.karl.jnano.rpc.response.ResponseBlockHashes;
import uk.oczadly.karl.jnano.rpc.response.ResponseBlockInfo;
import uk.oczadly.karl.jnano.rpc.response.ResponseWork;
import uk.oczadly.karl.jnano.websocket.NanoWebSocketClient;
import uk.oczadly.karl.nanopaymentserver.exception.RpcQueryException;
import uk.oczadly.karl.nanopaymentserver.properties.NodeProperties;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RpcService {
    
    private static final Logger log = LoggerFactory.getLogger(RpcService.class);
    
    private final RpcQueryNode rpc, workRpc;
    private final URI wsUri;
    private final ExecutorService blockPublishExecutor = Executors.newSingleThreadExecutor();
    
    @Autowired
    public RpcService(NodeProperties config) {
        this.rpc = RpcQueryNode.builder()
                .setDefaultTimeout(10000)
                .setAddress(config.getRpcUrl())
                .build();
        this.workRpc = new RpcQueryNode(config.getWorkgenRpcUrl());
        this.wsUri = config.getWebsocketUri();
    }
    
    
    public RpcQueryNode getRpcClient() {
        return rpc;
    }
    
    public NanoWebSocketClient createWebSocketClient() {
        return new NanoWebSocketClient(wsUri);
    }
    
    
    /**
     * Returns information about the given account.
     * @param account the account to retrieve
     * @return the account info, or empty if the account doesn't exist
     */
    public Optional<ResponseAccountInfo> getAccountInfo(NanoAccount account) {
        try {
            return Optional.of(rpc.processRequest(new RequestAccountInfo(account.toAddress())));
        } catch (RpcEntityNotFoundException e) {
            return Optional.empty(); // Account doesn't exist
        } catch (IOException | RpcException e) {
            throw wrapException(e);
        }
    }
    
    /**
     * Returns information about the given block hash.
     * @param hash the hash of the block to retrieve
     * @return the block info, or empty if the block doesn't exist
     */
    public Optional<ResponseBlockInfo> getBlockInfo(HexData hash) {
        try {
            return Optional.of(rpc.processRequest(new RequestBlockInfo(hash.toHexString())));
        } catch (RpcEntityNotFoundException e) {
            return Optional.empty(); // Block doesn't exist
        } catch (IOException | RpcException e) {
            throw wrapException(e);
        }
    }
    
    /**
     * Publishes the given block to the Nano network.
     * @param block the block to publish
     */
    public void publishBlock(Block block) {
        try {
            rpc.processRequest(new RequestProcess(block));
            log.info("Published block {} to network", block.getHash());
        } catch (IOException | RpcException e) {
            throw wrapException(e);
        }
    }
    
    /**
     * Checks whether the work is valid for the given block, assuming the send subtype.
     * @param block the block to check work
     * @return true if the work is valid
     */
    public boolean isWorkValidForSend(IBlockPrevious block) {
        if (block.getWorkSolution() == null)
            return false; // Work value is empty
        try {
            return rpc.processRequest(new RequestWorkValidate(
                    block.getWorkSolution().getAsHexadecimal(), block.getPreviousBlockHash().toHexString()))
                    .isValidAll();
        } catch (IOException | RpcException e) {
            throw wrapException(e);
        }
    }
    
    /**
     * Generates work for the given block via RPC, and updates the work property of the given block.
     * @param block the block to generate work for
     */
    public void generateWork(Block block) {
        log.info("Generating work for block {}...", block.getHash());
        try {
            ResponseWork work = workRpc.processRequest(new RequestWorkGenerate.Builder(block).build());
            block.setWorkSolution(work.getWorkSolution());
        } catch (RpcExternalException e) {
            if (e.getMessage().equals("Provided work is already enough for given difficulty."))
                return; // Work already sufficient
            throw wrapException(e);
        } catch (IOException | RpcException e) {
            throw wrapException(e);
        }
    }
    
    
    /**
     * Wraps a given exception in an RpcQueryException service exception.
     */
    private RpcQueryException wrapException(Exception e) {
        if (e instanceof RpcException) {
            throw new RpcQueryException("Unexpected RPC exception.", e);
        } else if (e instanceof IOException) {
            throw new RpcQueryException("Node unreachable.", e);
        } else {
            throw new RpcQueryException("Unknown error!", e);
        }
    }

}
