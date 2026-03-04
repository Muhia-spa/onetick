# Observability Baseline

This document defines the minimum dashboard for OneTick and the cache metrics we expect to track.

## Metrics Endpoints

- Prometheus scrape: `/actuator/prometheus`
- Health: `/actuator/health`

## Cache Metrics (Micrometer)

Enablement:
- `spring.cache.redis.enable-statistics=true`
- `management.metrics.enable.cache=true`

Key metrics:
- `cache.gets` (tags: `cache`, `result`=hit|miss)
- `cache.puts` (tags: `cache`)
- `cache.evicts` (tags: `cache`)
- `cache.removals` (tags: `cache`, `cause`=expired|explicit|size)

## Dashboard Baseline

1. **API Throughput**: `http.server.requests` rate
2. **API Latency**: `http.server.requests` p95/p99
3. **Cache Hit Ratio**: `sum(rate(cache.gets{result="hit"}[5m])) / sum(rate(cache.gets[5m]))`
4. **Cache Misses**: `sum(rate(cache.gets{result="miss"}[5m]))` by cache name
5. **Cache Evictions**: `sum(rate(cache.evicts[5m]))` by cache name
6. **Notifications**: `notifications.enqueued`, `notifications.sent`, `notifications.failed`, `notifications.retried`, `notifications.dlq`, `notifications.dispatch.duration` p95

## Alerts (Initial)

1. Cache hit ratio below 60% for 15m.
2. Notification DLQ increases > 0 for 10m.
3. API p95 > 1s for 10m.
