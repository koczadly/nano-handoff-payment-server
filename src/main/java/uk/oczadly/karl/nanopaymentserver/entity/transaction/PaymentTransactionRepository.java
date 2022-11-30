package uk.oczadly.karl.nanopaymentserver.entity.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {

    boolean existsByBlockHash(String hash);

    List<PaymentTransaction> findByStatusIn(List<PaymentTransaction.Status> statuses);
    
}
