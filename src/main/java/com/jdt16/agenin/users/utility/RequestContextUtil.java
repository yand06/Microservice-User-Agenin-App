package com.jdt16.agenin.users.utility;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestContextUtil {

    /**
     * Extract client IP address dari HTTP request
     * Memperhitungkan X-FORWARDED-FOR header untuk load balancer/proxy
     */
    public static String getClientIpAddress() {
        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) {
            return "UNKNOWN";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Ambil IP pertama jika multiple (comma-separated)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "UNKNOWN";
    }

    /**
     * Extract User-Agent dari HTTP request
     */
    public static String getUserAgent() {
        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) {
            return "UNKNOWN";
        }

        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "UNKNOWN";
    }

    /**
     * Get current HTTP request dari RequestContextHolder
     */
    private static HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
