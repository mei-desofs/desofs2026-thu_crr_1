package com.techstore.app.config.ratelimit;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.techstore.app.config.ratelimit.annotation.RateLimit;

import io.github.bucket4j.Bucket;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitProperties properties;

    private final BucketManager bucketManager;

    private final RateLimitResolver resolver;

    private final ObjectMapper mapper;

    public RateLimitInterceptor(BucketManager bucketManager, ObjectMapper mapper, RateLimitProperties properties, RateLimitResolver resolver) {
        this.bucketManager = bucketManager;
        this.mapper = mapper;
        this.properties = properties;
        this.resolver = resolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod method = (HandlerMethod) handler;

        RateLimit annotation = method.getMethod().getAnnotation(RateLimit.class);

        if (annotation == null) return true;

        String ruleName = annotation.value();
        Map<String, RateLimitProperties.Rule> rules = properties.getRules();
        RateLimitProperties.Rule rule = rules != null ? rules.get(ruleName) : null;

        if (rule == null) return true;

        String userId = getUserId(request);
        String ip = request.getRemoteAddr();

        String key = resolver.resolve(rule.getType(), userId, ip);

        Bucket bucket = bucketManager.resolve(key, rule.getCapacity(), rule.getDuration());

        if (!bucket.tryConsume(1)) {
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return false;
        }

        return true;
    }

    private String getUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }

        String token = auth.substring(7);
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payload = parts[1];
            byte[] decoded = java.util.Base64.getUrlDecoder().decode(payload);
            @SuppressWarnings("unchecked") java.util.Map<String, Object> claims = mapper.readValue(decoded, java.util.Map.class);

            Object sub = claims.get("sub");
            if (sub != null) return sub.toString();

            Object userId = claims.get("user_id");
            if (userId != null) return userId.toString();

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}