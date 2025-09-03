package integration.service;

import com.bahar.demo.DemoApplication;
import com.bahar.demo.service.ExternalServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DemoApplication.class)
class ExternalServiceIntegrationTest { //تست با Spring Context

    @Autowired
    private ExternalServiceImpl externalService;

    @Test
    void testCircuitBreakerWithFallback() {
        String first = externalService.callExternalApi();
        assertThat(first.contains("Fallback") || first.contains("Success")).isTrue();

        String second = externalService.callExternalApi();
        assertThat(second).contains("Success");
    }
}

/**
 *
 * توضیح:
 *
 * first → اولین بار که صدا می‌زنیم احتمالاً شکست می‌خوره → یا Fallback اجرا میشه یا اگر شانسی Success بشه هم اوکیه.
 *
 * second → چون شمارنده الان زوج شده (۲)، حتماً باید Success باشه.
 */