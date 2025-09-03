package com.bahar.demo.controller;

import com.bahar.demo.service.ExternalService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Test برای ExternalController
 * ----------------------------------
 * WebMvcTest فقط لایه‌ی وب را بالا می‌آورد (Controller + MVC)
 * و سرویس را با @MockBean شبیه‌سازی می‌کنیم.
 */
@WebMvcTest(controllers = ExternalController.class)
class ExternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExternalService externalService;

    /**
     * /api/callSync
     * ----------------------------------
     * هدف: اگر سرویس مقدار sync برگرداند، کنترلر همان را برگرداند.
     * (resilience در لایه سرویس است؛ اینجا فقط رفتار Controller را تست می‌کنیم)
     */
    @Test
    void callSync_shouldReturnServiceValue() throws Exception {
        when(externalService.callExternalApiSync()).thenReturn("✅ Success on attempt 2");

        mockMvc.perform(get("/api/callSync"))
                .andExpect(status().isOk())
                .andExpect(content().string("✅ Success on attempt 2"));
    }

    /**
     * /api/callAsync — سناریوی موفق
     * ----------------------------------
     * هدف: اگر Future موفق شود، chain کنترلر (thenApply) باید onSuccess را اعمال کند.
     */
    @Test
    void callAsync_shouldReturnSuccess_whenFutureCompletesNormally() throws Exception {
        // Future موفق
        when(externalService.asyncProcess())
                .thenReturn(CompletableFuture.completedFuture("✅ موفق شد!"));
        // خروجی onSuccess
        when(externalService.onSuccess("✅ موفق شد!"))
                .thenReturn("🎉 SUCCESS callback in Service: ✅ موفق شد!");

        mockMvc.perform(get("/api/callAsync"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("🎉 SUCCESS callback in Service")));
    }

    /**
     * /api/callAsync — سناریوی خطا
     * ----------------------------------
     * هدف: اگر Future با استثناء تکمیل شود، exceptionally باید onError را اعمال کند.
     */
    @Test
    void callAsync_shouldReturnError_whenFutureCompletesExceptionally() throws Exception {
        // ساخت Future ناموفق (بدون استفاده از failedFuture برای سازگاری جاوا 8)
        CompletableFuture<String> failed = new CompletableFuture<>();
        RuntimeException boom = new RuntimeException("شکست async");
        failed.completeExceptionally(boom);

        when(externalService.asyncProcess()).thenReturn(failed);
        when(externalService.onError(Mockito.any(Throwable.class)))
                .thenReturn("💥 ERROR callback in Service: شکست async");

        mockMvc.perform(get("/api/callAsync"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("💥 ERROR callback in Service")));
    }

    /**
     * /api/callback — پل Consumer به HTTP
     * ----------------------------------
     * هدف: کنترلر یک CompletableFuture می‌سازد و callback سرویس را
     * به promise::complete وصل می‌کند؛ با doAnswer شبیه‌سازی می‌کنیم
     * که سرویس، callback را با یک پیام فراخوانی کند.
     */
    @Test
    void callback_shouldBridgeConsumerToHttpResponse() throws Exception {
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<String> cb = (Consumer<String>) invocation.getArgument(0);
            cb.accept("✅ کار با موفقیت انجام شد!");
            return null;
        }).when(externalService).processWithCallback(any());

        mockMvc.perform(get("/api/callback"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("✅ کار با موفقیت")));
    }
}
