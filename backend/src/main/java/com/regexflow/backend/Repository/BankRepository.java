package com.regexflow.backend.Repository;

import com.regexflow.backend.Entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank,Long> {

    Optional<Bank> findByAddress(String address);
    Optional<Bank> findByName(String name);
    boolean existsByName(String name);
    boolean existsByAddress(String address);
}
