package uk.oczadly.karl.nanopaymentserver.service.blockwatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.jnano.model.block.Block;
import uk.oczadly.karl.jnano.websocket.NanoWebSocketClient;
import uk.oczadly.karl.jnano.websocket.TopicListener;
import uk.oczadly.karl.jnano.websocket.WsObserver;
import uk.oczadly.karl.jnano.websocket.topic.message.MessageContext;
import uk.oczadly.karl.jnano.websocket.topic.message.TopicMessageConfirmation;
import uk.oczadly.karl.nanopaymentserver.service.RpcService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * TODO: refactor and cleanup jNano library websocket API
 *
 * This class is a bit messy, as NanoWebSocketClient objects are not re-usable. Upon disconnect, we need to create
 * a new instance and attempt to reconnect. Once (re)connected, the confirmation filter must be applied. isInitialized
 * tracks whether the topic has been subscribed to and received an acknowledgement packet.
 */
public class ConfirmationWebsocketClient {
    
    private static final Logger log = LoggerFactory.getLogger(ConfirmationWebsocketClient.class);

    private final RpcService rpcService;
    private final Listener listener;
    private volatile NanoWebSocketClient client;
    private volatile Thread connectThread;
    private final ConfirmationHandler confirmationHandler = new ConfirmationHandler();
    private final ExecutorService listenerExecutor = Executors.newFixedThreadPool(50);
    
    public ConfirmationWebsocketClient(RpcService rpcService, Listener listener) {
        this.rpcService = rpcService;
        this.listener = listener;
    }
    
    
    public synchronized void connect() {
        if (!isOpen() && (connectThread == null || !connectThread.isAlive())) {
            connectThread = new Thread(new RetryingConnectTask(), "nano-ws-con-thread");
            connectThread.setDaemon(true);
            connectThread.start();
        }
    }
    
    public synchronized boolean isOpen() {
        return client != null && client.isOpen();
    }
    
    private void initNewClient() {
        client = rpcService.createWebSocketClient();
        client.setObserver(new WsObserverHandler());
        client.getTopics().topicConfirmedBlocks().registerListener(confirmationHandler);
    }
    
    
    /**
     * Handles websocket events (connect, disconnect, etc).
     * Will spawn a reconnection thread when the socket is closed.
     */
    private class WsObserverHandler implements WsObserver {
        @Override
        public void onOpen(int httpStatus) {
            try {
                log.info("WebSocket connected, subscribing to topic(s)...");
                NanoWebSocketClient usingClient = client;
                boolean initialized;
                do {
                    // Subscribe to confirmation topic
                    initialized = usingClient.getTopics().topicConfirmedBlocks().subscribeBlocking(30000);
                } while (!initialized && usingClient.isOpen());
                if (initialized) {
                    log.info("Successfully subscribed to WebSocket topics.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    
        @Override
        public void onClose(int code, String reason, boolean remote) {
            log.debug("WebSocket disconnected, errCode: {}", code);
            connect();
        }
    
        @Override
        public void onSocketError(Exception ex) {
            log.debug("WebSocket connection error occurred", ex);
        }
    
        @Override
        public void onHandlerError(Exception ex) {
            log.error("WebSocket handler error", ex);
        }
    }
    
    /**
     * Accepts and handles block confirmation events as they are received, forwarding them to the confirmation listener.
     */
    private class ConfirmationHandler implements TopicListener<TopicMessageConfirmation> {
        @Override
        public void onMessage(TopicMessageConfirmation message, MessageContext context) {
            listenerExecutor.submit(() -> listener.onConfirmation(message.getBlock(), message.getAmount()));
        }
    }
    
    /**
     * A runnable task which continually tries to (re)connect the websocket until successful.
     */
    private class RetryingConnectTask implements Runnable {
        @Override
        public void run() {
            initNewClient();
            try {
                boolean connected;
                do {
                    log.debug("Attempting to connect to node WebSocket.");
                    connected = client.connect();
                    if (!connected) {
                        log.debug("Connection attempt failed, will retry...");
                        Thread.sleep(2500);
                    }
                } while (!connected);
            } catch (InterruptedException e) {
                log.warn("Reconnection thread interrupted, will not attempt to reconnect!");
            }
        }
    }
    
    
    public interface Listener {
        /**
         * Called on block confirmation
         * @param block  the block contents
         * @param amount the amount being transferred, or null if non-transactional
         */
        void onConfirmation(Block block, NanoAmount amount);
    }
    
}
