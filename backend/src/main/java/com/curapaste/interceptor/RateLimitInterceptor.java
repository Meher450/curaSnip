package com.curapaste.interceptor;


import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ProxyManager<String> bucketProxyManager;
    private final BucketConfiguration bucketConfiguration;

    @Value("${spring.rate-limit.enabled}")
    private boolean rateLimitEnabled;

    public RateLimitInterceptor(ProxyManager<String> bucketProxyManager, BucketConfiguration bucketConfiguration) {
        this.bucketProxyManager = bucketProxyManager;
        this.bucketConfiguration = bucketConfiguration;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ){

        if(!rateLimitEnabled) return true;

        if(!request.getMethod().equals("POST")) return true;

        String ip = request.getRemoteAddr();

        Bucket bucket = bucketProxyManager.getProxy(
                ip,
                () -> bucketConfiguration
        );

        if(bucket.tryConsume(1)) return true;

        response.setStatus(429);
        return false;
    }
}
