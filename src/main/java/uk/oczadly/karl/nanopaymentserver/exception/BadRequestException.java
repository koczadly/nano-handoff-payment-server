package uk.oczadly.karl.nanopaymentserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Service exception which represents an invalid request argument or format.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException() {}

    public BadRequestException(String message) {
        super(message);
    }
    
}
