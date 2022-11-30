package uk.oczadly.karl.nanopaymentserver.exception;

public class IllegalPaymentStateException extends RuntimeException {

    public IllegalPaymentStateException() {
    }

    public IllegalPaymentStateException(String message) {
        super(message);
    }
}
