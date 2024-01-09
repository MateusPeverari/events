package com.study.events.infrastructure.adapters.outbound.persistence.entity;

import static org.hibernate.annotations.OnDeleteAction.CASCADE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Event")
public class EventEntity extends AuditingEntity {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "eventName")
  private String eventName;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner", nullable = false)
  @OnDelete(action = CASCADE)
  private UserEntity owner;

  @Column(name = "participantsLimit")
  private int participantsLimit;

  @Column(name = "eventDate")
  private Date eventDate;

  @Column(name = "eventTime")
  private String eventTime;

  @Column(name = "attendees")
  private int attendees;

  @Column(name = "attendeesList")
  @OneToMany
  private List<UserEntity> attendeesList;

}
