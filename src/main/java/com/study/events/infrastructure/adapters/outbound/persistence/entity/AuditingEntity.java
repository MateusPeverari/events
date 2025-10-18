package com.study.events.infrastructure.adapters.outbound.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditingEntity {

  @CreatedBy
  @Column(name = "created_by", updatable = false)
  private String createdBy;

  @CreatedDate
  @Column(name = "created_date", nullable = false)
  private LocalDateTime createdDate = LocalDateTime.now();

  @LastModifiedBy
  @Column(name = "last_modified_by")
  private String lastModifiedBy;

  @LastModifiedDate
  @Column(name = "last_modified_date", nullable = false)
  private LocalDateTime lastModifiedDate;

  @Version
  @Column(name = "version", nullable = false)
  private long version;
  // The lightweight optimistic lock guards against lost updates when thousands of virtual threads
  // try to change the same row, while still allowing readers to proceed without blocking.
}
