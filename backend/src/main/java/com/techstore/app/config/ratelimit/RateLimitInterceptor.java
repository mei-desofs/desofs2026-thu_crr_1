package com.techstore.app.config.ratelimit;

import java.util.Map;

import com.techstore.app.config.jwt.JWTAuthFilter;
import com.techstore.app.exception.RateLimitException;
import com.techstore.app.helpers.CookiesHelper;
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

        String identityKey = resolver.resolve(rule.getType(), userId, ip);
        String key = ruleName + ":" + identityKey;

        Bucket bucket = bucketManager.resolve(key, rule.getCapacity(), rule.getDuration());

        if (!bucket.tryConsume(1)) {
            throw new RateLimitException();
        }

        return true;
    }

    private String getUserId(HttpServletRequest request) {

        String token = CookiesHelper.getCookieValue(request, "access_token");
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