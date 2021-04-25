package uk.oczadly.karl.nanopaymentserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Karl Oczadly
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PaymentNotFoundException extends RuntimeException {
    
    public PaymentNotFoundException() {
        this("Payment not found.");
    }
    
    public PaymentNotFoundException(String message) {
        super(message);
    }
    
    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PaymentNotFoundException(Throwable cause) {
        super(cause);
    }
    
    public PaymentNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
