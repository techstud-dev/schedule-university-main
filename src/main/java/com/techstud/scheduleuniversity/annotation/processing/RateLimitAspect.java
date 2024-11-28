package com.techstud.scheduleuniversity.annotation.processing;

import com.techstud.scheduleuniversity.annotation.RateLimit;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@SuppressWarnings("deprecation")
@Slf4j
public class RateLimitAspect {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Before("@annotation(rateLimit)")
    public void checkRateLimit(JoinPoint joinPoint, RateLimit rateLimit) {

        String methodKey = generateMethodKey(joinPoint, rateLimit);

        Bucket bucket = buckets.computeIfAbsent(methodKey, key -> {
            Duration refillDuration = getDuration(rateLimit.refillPeriod(), rateLimit.periodUnit());

            Bandwidth limit = Bandwidth.classic(rateLimit.capacity(),
                    Refill.greedy(rateLimit.refillTokens(), refillDuration));
            log.info("Rate limit created for method: {}, capacity: {}, refillPeriod: {}, periodUnit: {}",
                    methodKey, rateLimit.capacity(), rateLimit.refillPeriod(), rateLimit.periodUnit());
            return Bucket4j.builder().addLimit(limit).build();
        });

        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests!");
        }
    }

    private String generateMethodKey(JoinPoint joinPoint, RateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        return className + "#" + methodName + "-" +
                "capacity=" + rateLimit.capacity() +
                ",refillPeriod=" + rateLimit.refillPeriod() +
                ",periodUnit=" + rateLimit.periodUnit();
    }

    private Duration getDuration(long period, String periodUnit) {
        return switch (periodUnit.toUpperCase()) {
            case "SECONDS" -> Duration.ofSeconds(period);
            case "MINUTES" -> Duration.ofMinutes(period);
            case "HOURS" -> Duration.ofHours(period);
            case "DAYS" -> Duration.ofDays(period);
            default -> throw new IllegalArgumentException("Unsupported period unit: " + periodUnit);
        };
    }
}
