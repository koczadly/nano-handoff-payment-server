package uk.oczadly.karl.nanopaymentserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.oczadly.karl.nanopaymentserver.dto.payment.NewPaymentRequest;
import uk.oczadly.karl.nanopaymentserver.dto.payment.NewPaymentResponse;
import uk.oczadly.karl.nanopaymentserver.dto.payment.PaymentStatusResponse;
import uk.oczadly.karl.nanopaymentserver.service.payment.PaymentService;

@RestController()
@RequestMapping("/payment")
public class PaymentApiController {
    
    @Autowired private PaymentService paymentService;
    
    
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/new")
    public NewPaymentResponse pNew(@RequestBody NewPaymentRequest request) {
        return paymentService.createNewPayment(request);
    }
    
    // todo
//    @GetMapping("/{id}/wait")
//    public String gWait(@PathVariable String id) {
//        return "{}"; //todo
//    }
    
    // todo
//    @DeleteMapping("/{id}")
//    public String dCancel(@PathVariable String id) {
//        return "{}";
//    }
    
    @GetMapping("/{id}")
    public PaymentStatusResponse gStatus(@PathVariable String id) {
        return new PaymentStatusResponse(paymentService.getPayment(id));
    }

}
