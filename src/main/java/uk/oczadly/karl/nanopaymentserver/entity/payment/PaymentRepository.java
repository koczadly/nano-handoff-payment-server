package uk.oczadly.karl.nanopaymentserver.entity.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.oczadly.karl.jnano.model.HexData;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    boolean existsByHandoffHash(HexData hash);
    
    List<Payment> findByExpirationNotNull();
    
}
