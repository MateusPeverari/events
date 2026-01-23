package com.study.events.infrastructure.adapters.configs;

import java.time.Duration;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;

@Configuration
public class VirtualThreadConfiguration {

  @Bean
  public VirtualThreadTaskExecutor applicationTaskExecutor() {
    var executor = new VirtualThreadTaskExecutor("events-vt-");
    executor.setVirtualThreadFactory(Thread.ofVirtual().name("events-vt-", 0).factory());
    executor.setShutdownTimeout(Duration.ofSeconds(2));
    // Virtual threads allow the servlet container and any @Async tasks to scale to thousands of
    // concurrent requests without blocking platform threads, dramatically improving throughput
    // under load while keeping the programming model synchronous.
    return executor;
  }

  @Bean
  public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutor(
      VirtualThreadTaskExecutor applicationTaskExecutor) {
    return protocolHandler -> {
      // Reuse the same virtual-thread executor for Tomcat so that every connection is handled by a
      // lightweight fiber instead of a heavyweight platform thread, removing a major concurrency
      // bottleneck in the classic request-per-thread model.
      protocolHandler.setExecutor(applicationTaskExecutor);
    };
  }
}
