package com.regexflow.backend.Repository;

import com.regexflow.backend.Entity.Sms;
import com.regexflow.backend.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SmsRepository extends JpaRepository<Sms, Long> {
    List<Sms> findByUser(Users user);
    
    List<Sms> findByUserOrderByCreatedAtDesc(Users user);
}
