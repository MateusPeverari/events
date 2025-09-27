# Performance Tuning Guide

This guide captures a **single configuration** that keeps the Spring Boot 3.x application (running on Tomcat with HikariCP) stable across the three load profiles on an Oracle Cloud Always Free VM (4 OCPU, 24 GB RAM). The same settings apply to all scenarios, so no per-test retuning is required. PostgreSQL runs with its packaged defaults from the official `postgres:16` image to keep operations simple.

## 1. Platform Assumptions

- Ubuntu 24.04 host running Docker and Docker Compose.
- Containers: Spring Boot application, PostgreSQL 16, Prometheus, Grafana.
- Oracle Cloud Always Free VM with 4 OCPU and 24 GB RAM. Roughly 2 GB is reserved for the host OS and Docker overhead, leaving ~22 GB for containers.
- Docker Compose keeps the topology minimal so database upgrades and maintenance remain straightforward.

## 2. JVM Configuration (Java 21)

The application container uses Eclipse Temurin 21 with the options baked into the image and exposed via `JAVA_OPTS` (see `Dockerfile`). Key settings:

| Setting | Value | Rationale |
| --- | --- | --- |
| `-Xms8g -Xmx8g` | Fixed 8 GB heap within a 10 GB container limit, leaving headroom for metaspace, threads, buffers, and the OS. |
| `-Xss512k` | Smaller thread stacks maximize the number of Tomcat and async threads while avoiding stack overflows (validated for typical Spring stack depths). |
| `-XX:+UseG1GC` with G1 tuning (`G1HeapRegionSize=16m`, `MaxGCPauseMillis=200`, `InitiatingHeapOccupancyPercent=30`, `G1ReservePercent=20`, `-XX:+UnlockExperimentalVMOptions`, `G1NewSizePercent=20`, `G1MaxNewSizePercent=60`) | Optimized for large heaps with predictable pause times under sustained traffic. |
| `-XX:+ParallelRefProcEnabled`, `-XX:+UseStringDeduplication` | Reduce reference processing and duplicate string overhead when handling many concurrent requests. |
| `-XX:+AlwaysPreTouch` | Pre-touches pages during startup to eliminate runtime major faults when the heap grows under load. |
| Failure diagnostics (`-XX:+ExitOnOutOfMemoryError`, `-XX:+HeapDumpOnOutOfMemoryError`, `-XX:HeapDumpPath=/app/logs`, `-XX:ErrorFile=/app/logs/hs_err_pid%p.log`) | Guarantees quick fail-fast and captures heap/HS_ERR logs for triage. |
| GC logging (`-Xlog:gc*:file=/app/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=10M`) | Rolling GC logs enable performance triage without filling the disk. |

These options keep GC pause times below 200 ms and maintain consistent throughput during the 2‑hour progressive and 1‑hour stress tests.

## 3. Spring Boot / Tomcat Tuning

Configured via `application.properties`:

- **Threading**: `server.tomcat.max-threads=400` balances CPU saturation on 4 cores with enough concurrency to back the 15k virtual users. `server.tomcat.accept-count=2000` and `server.tomcat.max-connections=10000` provide headroom before new connections are rejected.
- **Connection handling**: `server.connection-timeout=5s`, `server.tomcat.keep-alive-timeout=20000`, and `server.tomcat.max-keep-alive-requests=10000` limit stalled sockets while reusing keep-alive connections effectively for high RPS phases.
- **Async requests**: `spring.mvc.async.request-timeout=30s` prevents runaway async tasks from piling up.
- **Graceful shutdown**: `server.shutdown=graceful` with `spring.lifecycle.timeout-per-shutdown-phase=30s` ensures load balancers can drain connections between test runs.
- **Metrics & observability**: Prometheus export is enabled together with histogram buckets to observe tail latencies during the tests.
- **Logging**: Application logging is reduced to `INFO`, Hibernate SQL logging is disabled (`spring.jpa.show-sql=false`, `logging.level.org.hibernate.SQL=ERROR`), and Logback rolls files under `/app/logs` to avoid I/O contention.

## 4. HikariCP Configuration

HikariCP is tuned for the available CPU and the concurrency level expected during the stress test:

- `maximum-pool-size=80` and `minimum-idle=32` provide enough active sessions to saturate the database without causing context-switch storms (≈20 connections per vCPU with headroom for maintenance tasks).
- `connection-timeout=2000` ms keeps callers from blocking indefinitely during spikes.
- `max-lifetime=1740000` ms (29 minutes) and `keepalive-time=300000` ms avoid the PostgreSQL default 30-minute idle termination, proactively refreshing connections.
- `idle-timeout=300000` ms trims unused connections after 5 minutes, preventing leakage during the baseline test.
- `connection-test-query=SELECT 1` with `validation-timeout=1000` ms makes the pool drop unhealthy connections promptly.
- Autocommit disabled at the pool level (`auto-commit=false`) pairs with Hibernate’s batching tweaks to reduce round-trips under heavy workloads.

## 5. Operational Notes

- Build and run the stack with `docker-compose up -d --build`. The Compose file keeps the PostgreSQL service close to the upstream defaults so upgrades remain predictable.
- Monitor Prometheus and Grafana dashboards during each load test. Track Tomcat thread utilization, Hikari pool usage, JVM GC logs, and application-level error rates.
- Use the captured GC and application logs under `/app/logs` to diagnose regressions between test runs.

This configuration favors sustained throughput with controlled latency. No per-test reconfiguration is needed—redeploying with Docker Compose picks up all JVM and application-level tuning.
