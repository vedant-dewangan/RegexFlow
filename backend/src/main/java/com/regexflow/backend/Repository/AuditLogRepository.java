package com.regexflow.backend.Repository;

import com.regexflow.backend.Entity.AuditLog;
import com.regexflow.backend.Entity.RegexTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
    Optional<AuditLog> findByTemplate(RegexTemplate template);
}
