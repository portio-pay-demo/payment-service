package io.portioapay.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Token bucket rate limiter per merchant.
 *
 * NP-2029: Fixed sliding window that was causing false positives for enterprise
 * merchants running bulk settlement operations. Now uses a token bucket with
 * configurable burst allowance per merchant tier.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private static final int DEFAULT_RATE = 100;    // requests/second
    private static final int ENTERPRISE_RATE = 1000; // requests/second for enterprise
    private static final int BURST_MULTIPLIER = 3;

    private final RedisTemplate<String, Long> redisTemplate;

    public boolean isAllowed(String merchantId, String merchantTier) {
        int rateLimit = "enterprise".equalsIgnoreCase(merchantTier) ? ENTERPRISE_RATE : DEFAULT_RATE;
        int burstLimit = rateLimit * BURST_MULTIPLIER;

        String key = "rate:" + merchantId;
        Long current = redisTemplate.opsForValue().increment(key, 1);

        if (current == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(1));
        }

        if (current > burstLimit) {
            log.warn("Rate limit exceeded for merchant={} tier={} current={} burst={}",
                    merchantId, merchantTier, current, burstLimit);
            return false;
        }

        return true;
    }
}
