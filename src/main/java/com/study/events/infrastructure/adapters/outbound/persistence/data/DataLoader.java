
package com.study.events.infrastructure.adapters.outbound.persistence.data;

import com.study.events.infrastructure.adapters.outbound.persistence.entity.EventEntity;
import com.study.events.infrastructure.adapters.outbound.persistence.entity.UserEntity;
import com.study.events.infrastructure.adapters.outbound.persistence.repository.EventRepository;
import com.study.events.infrastructure.adapters.outbound.persistence.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.sql.Date;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EventRepository eventRepository;

  @PostConstruct
  public void loadData() {
    var user = new UserEntity(UUID.fromString("e2ca143a-f1f5-4d0c-aad0-230170a5474f"), "Mateus", "mateus@gmail.com", "502.914.848-59");

    userRepository.save(user);



    //eventRepository.save(event);
  }

  @PreDestroy
  public void removeData() {
    userRepository.deleteAll();
  }
}

