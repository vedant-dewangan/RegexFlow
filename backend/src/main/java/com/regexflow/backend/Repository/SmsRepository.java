package com.regexflow.backend.Repository;

import com.regexflow.backend.Entity.Sms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SmsRepository extends JpaRepository<Sms, Long> {
    List<Sms> findByUser_UId(Long userId);
}
