package uk.oczadly.karl.nanopaymentserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Karl Oczadly
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvoiceNotFoundException extends BadRequestException {

    public InvoiceNotFoundException() {
        this("Payment not found.");
    }
    
    public InvoiceNotFoundException(String message) {
        super(message);
    }
    
}
