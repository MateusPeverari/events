# Performance Tuning Guide

This document captures a **single configuration** that keeps the Spring Boot application (running on Tomcat with HikariCP) and the PostgreSQL 16 database stable across the three target load tests on an Oracle Cloud Always Free VM (4 OCPU, 24 GB RAM). The same configuration is applied for all scenarios so no per-test retuning is required.

## 1. Platform Assumptions

- Ubuntu 24.04 host running Docker and Docker Compose.
- Containers: Spring Boot 3.x application, PostgreSQL 16, Prometheus, Grafana.
- Oracle Cloud Always Free VM with 4 OCPU and 24 GB RAM. Roughly 2 GB is reserved for the host OS and Docker overhead, leaving ~22 GB for containers.
- All containers share a dedicated bridge network (`backend`) to keep latency low.

## 2. JVM Configuration (Java 21)

The application container uses Eclipse Temurin 21 with the options baked into the image and exposed via `JAVA_OPTS` (see `Dockerfile` and `docker-compose.yml`). Key settings:

| Setting | Value | Rationale |
| --- | --- | --- |
| `-Xms8g -Xmx8g` | Fixed 8 GB heap within a 10 GB container limit, leaving headroom for metaspace, threads, buffers, and the OS. |
| `-Xss512k` | Smaller thread stacks maximize the number of Tomcat and async threads while avoiding stack overflows (tested safe for typical Spring stack depths). |
| `-XX:+UseG1GC` with G1 tuning (`G1HeapRegionSize=16m`, `MaxGCPauseMillis=200`, `InitiatingHeapOccupancyPercent=30`, `G1ReservePercent=20`, `G1NewSizePercent=20`, `G1MaxNewSizePercent=60`) | Optimized for large heaps with predictable pause times under sustained traffic. |
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

HikariCP is tuned for the PostgreSQL pool and the available CPU:

- `maximum-pool-size=80` and `minimum-idle=32` provide enough active sessions to saturate the database without causing context-switch storms (80 ≈ 20 connections per vCPU with headroom for maintenance tasks).
- `connection-timeout=2000` ms keeps callers from blocking indefinitely during spikes.
- `max-lifetime=1740000` ms (29 minutes) and `keepalive-time=300000` ms avoid the PostgreSQL default 30-minute idle termination, proactively refreshing connections.
- `idle-timeout=300000` ms trims unused connections after 5 minutes, preventing leakage during the baseline test.
- `connection-test-query=SELECT 1` with `validation-timeout=1000` ms makes the pool drop unhealthy connections promptly.
- Autocommit disabled at the pool level (`auto-commit=false`) pairs with Hibernate’s batching tweaks to reduce round-trips under heavy insert/update workloads.

## 5. PostgreSQL 16 Tuning

Applied through the Compose command block:

| Parameter | Value | Reason |
| --- | --- | --- |
| `shared_buffers=3GB` | ~25% of the 12 GB database container memory for efficient caching. |
| `effective_cache_size=8GB` | Represents remaining cacheable memory (shared buffers + OS page cache) for planner cost estimates. |
| `work_mem=16MB` | Supports large sorts/hash joins while keeping 200 connections from exhausting RAM. |
| `maintenance_work_mem=1GB` | Speeds up index maintenance and autovacuum under bulk updates. |
| `max_connections=200` | Aligns with the 80-connection Hikari pool plus maintenance headroom. |
| `wal_buffers=64MB`, `checkpoint_timeout=15min`, `checkpoint_completion_target=0.9`, `max_wal_size=8GB` | Reduce checkpoint frequency and WAL pressure during 10k+ user bursts. |
| `autovacuum_max_workers=6`, `autovacuum_naptime=30s`, `autovacuum_vacuum_cost_limit=2000` | Keep tables clean during the long progressive test and after the stress run. |
| `temp_buffers=16MB`, `effective_io_concurrency=200`, `random_page_cost=1.1` | Improve temp-table performance and favor index usage on fast block devices. |

Additional operational improvements:

- `tmpfs` mounts for `pg_stat_tmp` and `pg_tmp` eliminate slow disk I/O for transient stats/sort spill files. The database datadir
  lives under `/var/lib/postgresql/data/pgdata`, which keeps the named Docker volume root (with its `lost+found` entry) outside of
  PostgreSQL's initialization path.
- `shm_size: '2gb'` lifts the default SHM cap so `shared_buffers` can be satisfied.
- Health checks ensure the application only starts after PostgreSQL is accepting connections.

## 6. Docker Resource Management & Networking

- **Resource limits**: Application container capped at 10 GB RAM / 2 CPU, PostgreSQL at 12 GB RAM / 2 CPU. Reservations provide the scheduler hints while leaving ~2 GB to the host for Docker, Prometheus, Grafana, and OS services.
- **File descriptors**: `nofile` limits raised to 1,048,576 for both app and DB containers to handle the high connection counts imposed by the stress test.
- **Networking**: `net.core.somaxconn=65535`, `net.ipv4.tcp_fin_timeout=15`, and `net.ipv4.tcp_tw_reuse=1` reduce SYN backlog issues and the TIME_WAIT footprint during ramp-ups.
- **Bridge network**: A custom `backend` bridge isolates east-west traffic and allows MTU adjustments if Oracle Cloud networking requires it.
- **Logs and GC files**: Stored on the container filesystem under `/app/logs`; bind-mount or forward to external storage in production to avoid filling the container during extended test campaigns.

## 7. Load Test Guidance

1. **Baseline (1k users / 30 min)**: Validate warm-up, confirm GC logs show steady-state minor collections, and ensure connection pools stabilize at the minimum idle threshold.
2. **Progressive (1k → 10k users / 120 min)**: Watch Prometheus histograms and PostgreSQL `pg_stat_activity` for saturation; with the above settings, CPU utilization should plateau near 75% and queue depth remain below the 2k accept backlog.
3. **Stress (15k users / 60 min)**: Expect Tomcat threads to run near capacity. Monitor rejected connections (should stay near zero thanks to accept-count) and verify PostgreSQL checkpoints remain under 45 seconds.

All tuning choices favor sustained throughput with controlled latency. No per-test reconfiguration is needed—just redeploy the stack with `docker-compose up -d --build` to pick up the settings.
