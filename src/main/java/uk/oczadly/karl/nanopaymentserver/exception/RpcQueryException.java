package uk.oczadly.karl.nanopaymentserver.exception;

/**
 * @author Karl Oczadly
 */
public class RpcQueryException extends RuntimeException {
    
    public RpcQueryException(String message) {
        super(message);
    }
    
    public RpcQueryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RpcQueryException(Throwable cause) {
        super(cause);
    }
    
}
