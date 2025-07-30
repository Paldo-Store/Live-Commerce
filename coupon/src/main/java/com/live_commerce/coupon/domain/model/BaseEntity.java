package com.live_commerce.coupon.domain.model;


import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

  @CreatedBy
  @Column(updatable = false)
  private String createdBy;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedBy
  private String updatedBy;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  private String deletedBy;

  private LocalDateTime deletedAt;

  private Boolean deletedStatus = false;

  public void markAsDeleted(String deletedBy){
    if(this.deletedStatus){
      throw new IllegalStateException("Already deleted");
    }
    this.deletedStatus = true;
    this.deletedBy = deletedBy;
    this.deletedAt = LocalDateTime.now();
  }
}
