package com.edusphere.course.entity;
 
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
 
@MappedSuperclass
@Data
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {
 
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
 
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
 
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
 
    @Column(name = "is_deleted")
    private boolean isDeleted = false;
}
 