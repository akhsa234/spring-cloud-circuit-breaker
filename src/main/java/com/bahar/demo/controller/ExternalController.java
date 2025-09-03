package com.bahar.demo.controller;

import com.bahar.demo.service.ExternalService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ExternalController {

    private static final String CB_NAME = "externalService";

    private final ExternalService externalService;

    public ExternalController(ExternalService externalService) {
        this.externalService = externalService;
    }

    @GetMapping("/test")
    public String testApi() {
        return externalService.callExternalApi();
    }

    @GetMapping("/call")
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackResponse")
    public String callExternal() {
        // شبیه‌سازی خطا با احتمال 60٪
        if (Math.random() < 0.6) {
            throw new RuntimeException("Simulated failure in external service");
        }
        return "✅ External service success!";
    }

    public String fallbackResponse(Exception ex) {
        return "⚠️ Fallback: service unavailable (“" + ex.getMessage() + "”)";
    }
}
