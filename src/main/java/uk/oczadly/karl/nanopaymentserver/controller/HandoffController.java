package uk.oczadly.karl.nanopaymentserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.oczadly.karl.nanopaymentserver.exception.HandoffException;
import uk.oczadly.karl.nanopaymentserver.exception.PaymentNotFoundException;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffRequest;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffResponse;
import uk.oczadly.karl.nanopaymentserver.service.BlockHandoffService;

@RestController
public class HandoffController {
    
    private static final Logger log = LoggerFactory.getLogger(HandoffController.class);
    
    @Autowired private BlockHandoffService handoffService;
    
    
    @GetMapping("/handoff")
    public String gHandoff() {
        return "This URL must be accessed through a compatible wallet.";
    }
    
    @PostMapping(path = "/handoff", consumes = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public HandoffResponse pHandoff(@RequestBody HandoffRequest params) {
        return handoffService.handoff(params); // Only returns if successful; failure will throw an exception
    }
    
    
    @ExceptionHandler(HandoffException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HandoffResponse handleHandoffException(HandoffException ex) {
        log.debug("Rejecting handoff, reason: {}", ex.getResponseObject().getStatus().name());
        return ex.getResponseObject();
    }
    
    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HandoffResponse handlePaymentNotFound(PaymentNotFoundException ex) {
        return new HandoffResponse(HandoffResponse.Status.ERR_INVALID, ex.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public HandoffResponse handleInternalException(Exception e) {
        log.error("Internal error when processing handoff", e);
        return new HandoffResponse(HandoffResponse.Status.ERR_SEVICE_FAILURE, "Internal server error!");
    }

}
