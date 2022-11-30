package uk.oczadly.karl.nanopaymentserver.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.oczadly.karl.nanopaymentserver.util.Util;

import java.net.URI;
import java.net.URL;
import java.util.Objects;

@Component
@ConfigurationProperties("payment-server.node")
public class NanoNodeProperties {
    
    private URL rpcUrl = Util.parseUrl("http://[::1]:7076");
    private URL workRpcUrl;
    private URI websocketUri = URI.create("ws://[::1]:7078");


    /**
     * @return the URL of the node's RPC endpoint
     */
    public URL getRpcUrl() {
        return rpcUrl;
    }
    
    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = Util.parseUrl(rpcUrl);
    }

    /**
     * @return the URL of the work generating node's RPC endpoint, or the same as rpc-url if not set
     */
    public URL getWorkRpcUrl() {
        return Objects.requireNonNullElse(workRpcUrl, rpcUrl);
    }
    
    public void setWorkRpcUrl(String workRpcUrl) {
        this.workRpcUrl = workRpcUrl != null ? Util.parseUrl(workRpcUrl) : null;
    }

    /**
     * @return the URL of the node's websocket notifications server
     */
    public URI getWebsocketUri() {
        return websocketUri;
    }
    
    public void setWebsocketUri(String websocketUri) {
        this.websocketUri = URI.create(websocketUri);
    }
    
}
