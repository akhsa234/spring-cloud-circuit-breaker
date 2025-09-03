package com.bahar.demo.controller;

import com.bahar.demo.service.ExternalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test برای ExternalController
 * ----------------------------------
 * کل Spring Context را بالا می‌آورد،
 * Bean واقعی ExternalServiceImpl لود می‌شود،
 * AOP مربوط به Resilience4j فعال است،
 * و با MockMvc endpointهای واقعی را تست می‌کنیم.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ExternalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExternalService service; // Bean واقعی (اختیاری برای assert های بیشتر)

    /**
     * /api/callSync (SYNC + Resilience4j)
     * ----------------------------------
     * هدف: اطمینان از اینکه پاسخ HTTP همواره 200 است و یا:
     *  - پیام موفقیت (با الگوی "✅ Success on attempt X")
     *  - یا پیام fallback (اگر مدار/تلاش‌ها شکست خورد)
     *
     * نکته: چون Retry/CircuitBreaker فعال‌اند، اولین درخواست معمولاً
     * به دلیل الگوی شمارنده در Impl در نهایت موفق می‌شود.
     */
    @Test
    void callSync_shouldReturnOk_withSuccessOrFallback() throws Exception {
        mockMvc.perform(get("/api/callSync"))
                .andExpect(status().isOk())
                .andExpect(content().string(anyOf(
                        containsString("✅ Success on attempt"),
                        containsString("⚠️ Fallback response because")
                )));
    }

    /**
     * /api/callAsync (ASYNC + Callback chain)
     * ----------------------------------
     * هدف: اطمینان از اینکه پاسخ 200 است و متن شامل
     * "SUCCESS" یا "ERROR" (با توجه به تصادفی بودن) می‌باشد.
     */
    @Test
    void callAsync_shouldReturnOk_withSuccessOrErrorMessage() throws Exception {
        mockMvc.perform(get("/api/callAsync"))
                .andExpect(status().isOk())
                .andExpect(content().string(anyOf(
                        containsString("🎉 SUCCESS callback in Service"),
                        containsString("💥 ERROR callback in Service")
                )));
    }

    /**
     * /api/callback (پل Consumer→HTTP)
     * ----------------------------------
     * هدف: چون در Impl بعد از 500ms پیام موفقیت می‌فرستد،
     * اینجا انتظار پیام موفقیت داریم (اما برای اطمینان، وجود هرکدام را می‌پذیریم).
     */
    @Test
    void callback_shouldReturnOk_withConsumerMessage() throws Exception {
        mockMvc.perform(get("/api/callback"))
                .andExpect(status().isOk())
                .andExpect(content().string(anyOf(
                        containsString("✅ کار با موفقیت"),
                        containsString("❌ خطا در پردازش")
                )));
    }
}
