package com.study.events.infrastructure.adapters.configs;

import com.study.events.application.ports.outbound.EventPersistencePort;
import com.study.events.application.ports.outbound.UserPersistencePort;
import com.study.events.domain.service.EventService;
import com.study.events.domain.service.UserService;
import com.study.events.infrastructure.adapters.outbound.persistence.EventPersistenceAdapter;
import com.study.events.infrastructure.adapters.outbound.persistence.UserPersistenceAdapter;
import com.study.events.infrastructure.adapters.outbound.persistence.mappers.EventPersistenceMapper;
import com.study.events.infrastructure.adapters.outbound.persistence.mappers.UserPersistenceMapper;
import com.study.events.infrastructure.adapters.outbound.persistence.repository.EventRepository;
import com.study.events.infrastructure.adapters.outbound.persistence.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

  @Bean
  public UserService userService(UserPersistencePort userPersistencePort) {
    return new UserService(userPersistencePort);
  }

  @Bean
  public EventService eventService(EventPersistencePort eventPersistencePort,
                                   UserPersistencePort userPersistencePort,
                                   UserPersistenceMapper userPersistenceMapper) {
    return new EventService(eventPersistencePort, userPersistencePort, userPersistenceMapper);
  }

  @Bean
  public UserPersistenceAdapter userPersistenceAdapter(UserRepository userRepository,
                                                       UserPersistenceMapper userPersistenceMapper) {
    return new UserPersistenceAdapter(userRepository, userPersistenceMapper);
  }

  @Bean
  public EventPersistenceAdapter eventPersistenceAdapter(EventRepository eventRepository,
                                                         EventPersistenceMapper eventPersistenceMapper) {
    return new EventPersistenceAdapter(eventRepository, eventPersistenceMapper);
  }

}
