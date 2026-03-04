package com.onetick.notifications;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.notifications.queue", name = "backend", havingValue = "redis", matchIfMissing = true)
public class RedisNotificationQueue implements NotificationQueue {
    private static final String POP_DUE_LUA = """
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local items = redis.call('ZRANGEBYSCORE', key, '-inf', now, 'LIMIT', 0, 1)
            if (#items == 0) then
                return nil
            end
            redis.call('ZREM', key, items[1])
            return items[1]
            """;

    private final StringRedisTemplate redisTemplate;
    private final NotificationQueueProperties properties;
    private final DefaultRedisScript<String> popDueScript;

    public RedisNotificationQueue(StringRedisTemplate redisTemplate, NotificationQueueProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.popDueScript = new DefaultRedisScript<>(POP_DUE_LUA, String.class);
    }

    @Override
    public void enqueue(Long notificationId) {
        enqueueAt(notificationId, Instant.now().toEpochMilli());
    }

    @Override
    public Long dequeue() {
        String value = redisTemplate.execute(popDueScript,
                List.of(properties.getKey()),
                String.valueOf(Instant.now().toEpochMilli()));
        if (value == null) {
            return null;
        }
        return Long.valueOf(value);
    }

    @Override
    public void enqueueAt(Long notificationId, long epochMillis) {
        redisTemplate.opsForZSet().add(properties.getKey(), String.valueOf(notificationId), epochMillis);
    }

    @Override
    public void enqueueDlq(Long notificationId) {
        redisTemplate.opsForList().leftPush(properties.getDlqKey(), String.valueOf(notificationId));
    }
}
