package com.darkyellowcat.catvaultbackend.aop;

import com.darkyellowcat.catvaultbackend.annotation.RateLimit;
import com.darkyellowcat.catvaultbackend.exception.BusinessException;
import com.darkyellowcat.catvaultbackend.exception.ErrorCode;
import com.darkyellowcat.catvaultbackend.model.entity.User;
import com.darkyellowcat.catvaultbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserService userService;

    @Around("@annotation(rateLimit)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        int maxRequests = rateLimit.maxRequests();
        int timeWindow = rateLimit.timeWindow();
        String keyType = rateLimit.keyType();

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        String key = buildKey(request, keyType, joinPoint);
        String countStr = stringRedisTemplate.opsForValue().get(key);
        if (countStr != null && Integer.parseInt(countStr) >= maxRequests) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求过于频繁，请稍后再试");
        }
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, timeWindow, TimeUnit.SECONDS);
        }
        return joinPoint.proceed();
    }

    private String buildKey(HttpServletRequest request, String keyType, ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        String identifier;
        if ("user".equals(keyType)) {
            try {
                User loginUser = userService.getLoginUser(request);
                identifier = String.valueOf(loginUser.getId());
            } catch (Exception e) {
                identifier = getClientIp(request);
            }
        } else {
            identifier = getClientIp(request);
        }
        return "ratelimit:" + methodName + ":" + identifier;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
