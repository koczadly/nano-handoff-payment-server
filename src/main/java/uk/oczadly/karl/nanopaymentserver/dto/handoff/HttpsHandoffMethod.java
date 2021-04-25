package uk.oczadly.karl.nanopaymentserver.dto.handoff;

/**
 * A handoff method which is transported via HTTPS to a URL.
 */
public class HttpsHandoffMethod extends HandoffMethod {
    
    private final String url;
    
    public HttpsHandoffMethod(String url) {
        super("https");
        this.url = url;
    }
    
    
    public String getUrl() {
        return url;
    }
    
}
