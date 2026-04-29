package com.edusphere.audit.repository;

import com.edusphere.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    // No extra methods needed for now
    // JpaRepository already provides:
    // findById
    // findAll
    // save
    // deleteById
}