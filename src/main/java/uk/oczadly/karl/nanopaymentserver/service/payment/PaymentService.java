package uk.oczadly.karl.nanopaymentserver.service.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffResponse;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HandoffSpecification;
import uk.oczadly.karl.nanopaymentserver.dto.handoff.HttpsHandoffChannel;
import uk.oczadly.karl.nanopaymentserver.dto.payment.NewPaymentRequest;
import uk.oczadly.karl.nanopaymentserver.dto.payment.NewPaymentResponse;
import uk.oczadly.karl.nanopaymentserver.entity.payment.Payment;
import uk.oczadly.karl.nanopaymentserver.entity.payment.PaymentRepository;
import uk.oczadly.karl.nanopaymentserver.exception.BadRequestException;
import uk.oczadly.karl.nanopaymentserver.exception.HandoffException;
import uk.oczadly.karl.nanopaymentserver.exception.InvalidPaymentStateException;
import uk.oczadly.karl.nanopaymentserver.exception.PaymentNotFoundException;
import uk.oczadly.karl.nanopaymentserver.properties.HandoffProperties;
import uk.oczadly.karl.nanopaymentserver.properties.PaymentProperties;
import uk.oczadly.karl.nanopaymentserver.service.blockwatcher.BlockWatcherService;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    
    //stopship: need to add/remove payment from watcher
    
    
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired private HandoffProperties handoffProperties;
    @Autowired private PaymentProperties paymentProperties;
    @Autowired private PaymentRepository paymentRepo;
    @Autowired private BlockWatcherService blockWatcherService;
    
    private final Object repoMutex = new Object(); //todo: replace with db-provided transactional locking
    
    
    /**
     * @throws BadRequestException
     */
    public NewPaymentResponse createNewPayment(NewPaymentRequest request) {
        // Parse destination account
        NanoAccount account;
        if (request.getAccount() == null) {
            throw new BadRequestException("No account specified.");
        }
        try {
            account = NanoAccount.parse(request.getAccount());
        } catch (NanoAccount.AddressFormatException e) {
            throw new BadRequestException("Invalid account format.");
        }
        // Parse amount
        NanoAmount amount;
        try {
            if (request.getAmountRaw() != null) {
                amount = NanoAmount.valueOfRaw(request.getAmountRaw());
            } else if (request.getAmount() != null) {
                amount = NanoAmount.valueOfNano(request.getAmount());
            } else {
                throw new BadRequestException("No amount specified.");
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid amount.");
        }
        return createNewPayment(account, amount);
    }
    
    public NewPaymentResponse createNewPayment(NanoAccount destination, NanoAmount amount) {
        Payment payment = save(new Payment(destination, amount, paymentProperties.getTimeout()));
        
        // Create and return handoff specification
        HandoffSpecification handoffSpec = new HandoffSpecification(
                payment.getId(), destination, amount,
                new HttpsHandoffChannel(handoffProperties.getUrl()));
        handoffSpec.setWorkRequired(!handoffProperties.getWorkGen());
        try {
            return new NewPaymentResponse(handoffSpec.getId(), handoffSpec.toBase64());
        } catch (JsonProcessingException e) {
            throw new AssertionError(); // Serialization to JSON shouldn't throw an exception
        }
    }
    
    public List<Payment> getActivePayments() {
        return paymentRepo.findByExpirationNotNull();
    }
    
    /**
     * @throws PaymentNotFoundException
     */
    public Payment getPayment(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new PaymentNotFoundException("Invalid ID format.");
        }
        return getPayment(uuid);
    }
    
    /**
     * @throws PaymentNotFoundException
     */
    public Payment getPayment(UUID id) {
        synchronized (repoMutex) {
            Payment payment = paymentRepo.findById(id).orElseThrow(PaymentNotFoundException::new);
            updateIfExpired(payment);
            return payment;
        }
    }
    
    /**
     * @throws InvalidPaymentStateException
     * @throws PaymentNotFoundException
     */
    @Transactional
    public Payment acceptHandoff(UUID id, HexData blockHash) {
        synchronized (repoMutex) {
            Payment paymentReq = getPayment(id);
            if (paymentReq.getStatus() == Payment.Status.EXPIRED) {
                // Payment expired
                throw new HandoffException(HandoffResponse.Status.ERR_EXPIRED, "Payment has expired.");
            }
            if (paymentReq.getStatus() != Payment.Status.AWAITING_HANDOFF) {
                // Payment isn't in handoff state, reject payment
                throw new HandoffException(HandoffResponse.Status.ERR_ALREADY_PROVIDED);
            }
            if (paymentRepo.existsByHandoffHash(blockHash)) {
                // Block hash has been used for another payment
                throw new HandoffException(HandoffResponse.Status.ERR_BLOCK_ALREADY_ASSOCIATED);
            }
            // Update payment status
            paymentReq.setStatus(Payment.Status.HANDOFF_ACCEPTED);
            paymentReq.setHandoffHash(blockHash);
            paymentReq.setExpiration(paymentProperties.getConfirmationTimeout());
            return save(paymentReq);
        }
    }
    
    /**
     * @throws InvalidPaymentStateException
     * @throws PaymentNotFoundException
     */
    @Transactional
    public void setPaymentState(UUID id, Payment.Status status) {
        synchronized (repoMutex) {
            Payment paymentReq = getPayment(id);
            paymentReq.setStatus(status);
            save(paymentReq);
            if (status == Payment.Status.COMPLETED) {
                log.info("Marking payment #{} as completed.", id);
            } else {
                log.debug("Payment #{} state has been updated to {}.", id, status);
            }
        }
    }
    
    public void updateExpiredPayments() {
        synchronized (repoMutex) {
            List<Payment> payments = getActivePayments();
            log.debug("Checking {} active payments for expiry...", payments.size());
            for (Payment payment : payments) {
                try {
                    updateIfExpired(payment);
                } catch (InvalidPaymentStateException e) {
                    log.warn("Couldn't mark payment as invalid", e);
                }
            }
        }
    }
    
    /**
     * @throws InvalidPaymentStateException
     * @throws PaymentNotFoundException
     */
    @Transactional
    public void updateIfExpired(Payment payment) {
        synchronized (repoMutex) {
            if (!payment.getStatus().isFinalState() && payment.hasExpired()) {
                Payment.Status newStatus = null;
                if (payment.getStatus() == Payment.Status.AWAITING_HANDOFF) {
                    newStatus = Payment.Status.EXPIRED;
                } else if (payment.getStatus() == Payment.Status.HANDOFF_ACCEPTED) {
                    newStatus = Payment.Status.CONFIRMATION_TIMEOUT;
                }
                if (newStatus != null) {
                    payment.setStatus(newStatus);
                    save(payment);
                    log.debug("Payment #{} has been marked expired.", payment.getId());
                }
            }
        }
    }
    
    private Payment save(Payment payment) {
        if (payment.getStatus().isFinalState()) {
            // Stop watching block confirmations
            if (payment.getHandoffHash() != null) {
                blockWatcherService.unwatch(payment.getHandoffHash());
            }
            // Remove redundant parameters
            payment.setExpiration(null);
        }
        synchronized (repoMutex) {
            return paymentRepo.save(payment);
        }
    }
    
}
