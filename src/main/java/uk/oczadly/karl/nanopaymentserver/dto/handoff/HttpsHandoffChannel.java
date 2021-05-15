package uk.oczadly.karl.nanopaymentserver.dto.handoff;

import java.net.URL;

/**
 * A handoff method which is transported via HTTPS to a URL.
 */
public class HttpsHandoffChannel extends HandoffChannel {
    
    private final String url;
    
    public HttpsHandoffChannel(String url) {
        super("https");
        this.url = url.toLowerCase().startsWith("https://") ? url.substring(8) : url;
    }
    
    
    public String getUrl() {
        return url;
    }
    
}
