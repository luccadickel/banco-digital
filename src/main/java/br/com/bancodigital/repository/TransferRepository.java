package br.com.bancodigital.repository;

import br.com.bancodigital.domain.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT t FROM Transfer t " +
            "WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId " +
            "ORDER BY t.createdAt DESC")
    Page<Transfer> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);
}
