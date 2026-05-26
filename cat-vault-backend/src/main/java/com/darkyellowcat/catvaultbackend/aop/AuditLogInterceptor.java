package com.darkyellowcat.catvaultbackend.aop;

import com.darkyellowcat.catvaultbackend.annotation.AuditLog;
import com.darkyellowcat.catvaultbackend.model.entity.User;
import com.darkyellowcat.catvaultbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class AuditLogInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(auditLog)")
    public Object doAudit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        String operation = auditLog.value();
        String method = joinPoint.getSignature().toShortString();
        String ip = getClientIp(request);
        String userId = "anonymous";

        try {
            User loginUser = userService.getLoginUser(request);
            userId = String.valueOf(loginUser.getId());
        } catch (Exception ignored) {
        }

        Object result;
        try {
            result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[AUDIT] op={} method={} userId={} ip={} duration={}ms status=SUCCESS",
                    operation, method, userId, ip, duration);
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[AUDIT] op={} method={} userId={} ip={} duration={}ms status=FAILED error={}",
                    operation, method, userId, ip, duration, e.getMessage());
            throw e;
        }
        return result;
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
