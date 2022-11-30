package uk.oczadly.karl.nanopaymentserver.controller;

import com.fasterxml.jackson.core.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffDispatchRequest;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffDispatchResponse;
import uk.oczadly.karl.nanopaymentserver.exception.HandoffException;
import uk.oczadly.karl.nanopaymentserver.exception.InvoiceNotFoundException;
import uk.oczadly.karl.nanopaymentserver.service.BlockHandoffService;

@RestController
public class HandoffController {
    
    private static final Logger log = LoggerFactory.getLogger(HandoffController.class);
    
    @Autowired
    private BlockHandoffService handoffService;
    
    
    @GetMapping("/handoff")
    public String getHandoffNotice() {
        return "This URL must be accessed through a compatible wallet.";
    }
    
    @PostMapping(path = "/handoff", consumes = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public HandoffDispatchResponse postHandoff(@RequestBody HandoffDispatchRequest params) {
        return handoffService.processBlockHandoff(params); // Only returns if successful; failure will throw an exception
    }
    
    
    @ExceptionHandler(HandoffException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HandoffDispatchResponse handleHandoffException(HandoffException ex) {
        log.debug("Rejecting handoff, reason: {}, \"{}\"",
                ex.getResponse().getStatus().name(), ex.getResponse().getMessage());
        return ex.getResponse();
    }
    
    @ExceptionHandler(InvoiceNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HandoffDispatchResponse handlePaymentNotFound(InvoiceNotFoundException ex) {
        return new HandoffDispatchResponse(HandoffDispatchResponse.Status.ERR_INVALID, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HandoffDispatchResponse handleInvalidJson(JsonParseException ex) {
        return new HandoffDispatchResponse(HandoffDispatchResponse.Status.ERR_INVALID, "Invalid JSON body.");
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public HandoffDispatchResponse handleInternalException(Exception e) {
        log.error("Internal error when processing handoff", e);
        return new HandoffDispatchResponse(HandoffDispatchResponse.Status.ERR_SERVICE_FAILURE, "Internal server error!");
    }

}
