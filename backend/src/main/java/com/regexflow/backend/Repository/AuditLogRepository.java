package com.regexflow.backend.Repository;

import com.regexflow.backend.Entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
    
}
