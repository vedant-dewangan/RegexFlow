package com.regexflow.backend.Repository;

import com.regexflow.backend.Entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank,Long> {
}
