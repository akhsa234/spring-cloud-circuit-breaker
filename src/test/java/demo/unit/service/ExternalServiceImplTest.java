package com.bahar.demo.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test برای ExternalServiceImpl
 * ----------------------------------
 * این تست‌ها فقط منطق کلاس سرویس را بررسی می‌کنند
 * و به Spring Context یا وب سرور وابسته نیستند.
 */
class ExternalServiceImplTest {

    // نمونه‌ی واقعی از سرویس (بدون Mock)
    private final ExternalServiceImpl service = new ExternalServiceImpl();

    /**
     * call()
     * ----------------------------------
     * هدف: بررسی رفتار متد ساده‌ی sync که ۶۰٪ خطا می‌دهد.
     * - اگر موفق شد → باید پیام موفقیت برگردد.
     * - اگر خطا داد → باید RuntimeException با متن مورد انتظار باشد.
     */
    @Test
    void call_shouldEitherSucceedOrThrow() {
        try {
            String result = service.call();
            assertTrue(result.contains("✅ External service success!"));
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("Simulated failure"));
        }
    }

    /**
     * callExternalApiSync()
     * ----------------------------------
     * هدف: بررسی الگوی "بار اول شکست / بار دوم موفقیت"
     * - تلاش ۱: باید Exception بیندازد.
     * - تلاش ۲: باید پیام موفقیت برگرداند.
     * (این متد در Impl با @CircuitBreaker/@Retry تزئین شده،
     *  اما در Unit Test AOP اجرا نمی‌شود؛ فقط منطق داخلی را تست می‌کنیم.)
     */
    @Test
    void callExternalApiSync_shouldFailThenSucceed() {
        assertThrows(RuntimeException.class, service::callExternalApiSync);
        String result2 = service.callExternalApiSync();
        assertTrue(result2.contains("✅ Success on attempt"));
    }

    /**
     * fallbackMethod()
     * ----------------------------------
     * هدف: اطمینان از اینکه fallback پیام مناسب برمی‌گرداند.
     */
    @Test
    void fallbackMethod_shouldReturnFriendlyMessage() {
        String fb = service.fallbackMethod(new RuntimeException("boom"));
        assertTrue(fb.startsWith("⚠️ Fallback response because: "));
        assertTrue(fb.contains("boom"));
    }

    /**
     * processWithCallback(Consumer)
     * ----------------------------------
     * هدف: بررسی مدل callback ساده.
     * - یک StringBuilder می‌دهیم تا callback نتیجه را در آن بنویسد.
     * - چون async است، مقداری صبر می‌کنیم تا Thread کارش را تمام کند.
     */
    @Test
    void processWithCallback_shouldInvokeConsumerEventually() throws InterruptedException {
        StringBuilder capture = new StringBuilder();
        service.processWithCallback(capture::append);

        // چون Thread.sleep(500) در سرویس داریم، اینجا کمی بیشتر صبر می‌کنیم.
        Thread.sleep(700);

        String out = capture.toString();
        assertFalse(out.isEmpty(), "callback چیزی ننوشته!");
        // در پیاده‌سازی فعلی معمولاً پیام موفقیت می‌آید، اما اگر Interrupted شود پیام خطا می‌آید.
        assertTrue(out.contains("✅") || out.contains("❌"));
    }

    /**
     * asyncProcess()
     * ----------------------------------
     * هدف: بررسی رفتار CompletableFuture (موفق/شکست).
     * - اگر موفق شد → پیام "✅ موفق شد!" برمی‌گردد.
     * - اگر خطا داد → CompletionException/ExecutionException با علت مناسب.
     */
    @Test
    void asyncProcess_shouldEitherCompleteOrFail() throws InterruptedException {
        CompletableFuture<String> future = service.asyncProcess();
        try {
            String result = future.get(); // منتظر تکمیل می‌مانیم
            assertTrue(result.contains("✅ موفق شد!"));
        } catch (ExecutionException ex) {
            // خطای wrap شده؛ علت اصلی را بررسی می‌کنیم
            assertNotNull(ex.getCause());
            assertTrue(ex.getCause().getMessage().contains("❌ شکست خورد!"));
        }
    }

    /**
     * onSuccess / onError
     * ----------------------------------
     * هدف: اطمینان از خروجی‌های کاربرپسند هندلرها.
     */
    @Test
    void successAndErrorHandlers_shouldFormatMessages() {
        assertTrue(service.onSuccess("OK").startsWith("🎉 SUCCESS callback in Service: "));
        assertTrue(service.onError(new RuntimeException("x")).startsWith("💥 ERROR callback in Service: "));
    }
}
