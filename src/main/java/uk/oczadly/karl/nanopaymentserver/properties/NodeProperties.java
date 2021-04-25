package uk.oczadly.karl.nanopaymentserver.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.oczadly.karl.nanopaymentserver.util.Util;

import java.net.URI;
import java.net.URL;
import java.util.Objects;

@Component
@ConfigurationProperties("paymentserver.node")
public class NodeProperties {
    
    private URL rpcUrl = Util.parseUrl("http://[::1]:7076");
    private URL workgenRpcUrl;
    private URI websocketUri = URI.create("ws://[::1]:7078");
    private String addressPrefix = "nano";
    
    
    public URL getRpcUrl() {
        return rpcUrl;
    }
    
    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = Util.parseUrl(rpcUrl);
    }
    
    public URL getWorkgenRpcUrl() {
        return Objects.requireNonNullElse(workgenRpcUrl, rpcUrl);
    }
    
    public void setWorkgenRpcUrl(String workgenRpcUrl) {
        this.workgenRpcUrl = workgenRpcUrl != null ? Util.parseUrl(workgenRpcUrl) : null;
    }
    
    public URI getWebsocketUri() {
        return websocketUri;
    }
    
    public void setWebsocketUri(String websocketUri) {
        this.websocketUri = URI.create(websocketUri);
    }
    
    public String getAddressPrefix() {
        return addressPrefix;
    }
    
    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }
}
