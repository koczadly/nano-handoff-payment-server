package uk.oczadly.karl.nanopaymentserver.exception;

/**
 * @author Karl Oczadly
 */
public class InvalidPaymentStateException extends RuntimeException {
    
    public InvalidPaymentStateException() {
    }
    
    public InvalidPaymentStateException(String message) {
        super(message);
    }
    
    public InvalidPaymentStateException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidPaymentStateException(Throwable cause) {
        super(cause);
    }
    
    public InvalidPaymentStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
