package uk.oczadly.karl.nanopaymentserver.entity.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentInvoiceRepository extends JpaRepository<PaymentInvoice, UUID> {


    
}
