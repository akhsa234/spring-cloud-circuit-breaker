package com.bahar.demo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration Test برای ExternalServiceImpl
 * ------------------------------------------
 * این کلاس کل Spring Context را بالا می‌آورد.
 * بنابراین:
 *   - Bean واقعی ExternalServiceImpl لود می‌شود.
 *   - AOP و انوتیشن‌های Resilience4j (@CircuitBreaker, @Retry) فعال هستند.
 *   - لاجیک واقعی async و callback اجرا می‌شود.
 */
@SpringBootTest
class ExternalServiceIntegrationTest {

    @Autowired
    private ExternalService service;

    /**
     * call()
     * ------------------------------------------
     * متد ساده که با احتمال ۶۰٪ شکست می‌خورد.
     * چون رفتار تصادفی است، تست هر دو حالت را قبول می‌کند:
     *   - موفقیت: شامل "✅ External service success!"
     *   - شکست: RuntimeException با پیام "Simulated failure".
     */
    @Test
    void call_shouldEitherSucceedOrThrow() {
        try {
            String result = service.call();
            assertThat(result, containsString("✅ External service success!"));
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage(), containsString("Simulated failure"));
        }
    }

    /**
     * callExternalApiSync()
     * ------------------------------------------
     * این متد با @CircuitBreaker و @Retry تزئین شده.
     *
     * رفتار داخلی Impl:
     *   - شمارنده فرد → خطا
     *   - شمارنده زوج → موفقیت
     *
     * چون Retry فعال است، احتمال زیاد بعد از یکی دو تلاش نتیجه موفق خواهد شد.
     * پس تست بررسی می‌کند که خروجی یا شامل "✅ Success" باشد
     * یا اگر مدار باز شده باشد، به fallback برود (پیام "⚠️ Fallback").
     */
    @Test
    void callExternalApiSync_shouldEventuallySucceedOrFallback() {
        String result = service.callExternalApiSync();
        assertThat(result, anyOf(
                containsString("✅ Success"),
                containsString("⚠️ Fallback")
        ));
    }

    /**
     * fallbackMethod()
     * ------------------------------------------
     * این متد مستقیماً فراخوانی می‌شود (نه از طریق AOP).
     * بررسی می‌کنیم که پیام fallback شامل متن استثناء باشد.
     */
    @Test
    void fallbackMethod_shouldIncludeExceptionMessage() {
        String fb = service.fallbackMethod(new RuntimeException("boom"));
        assertThat(fb, allOf(
                containsString("⚠️ Fallback response because:"),
                containsString("boom")
        ));
    }

    /**
     * processWithCallback(Consumer)
     * ------------------------------------------
     * متد callback-style.
     * - یک Consumer می‌دهیم که نتیجه را در StringBuilder ذخیره کند.
     * - چون async است، کمی صبر می‌کنیم.
     */
    @Test
    void processWithCallback_shouldEventuallyInvokeConsumer() throws InterruptedException {
        StringBuilder capture = new StringBuilder();

        service.processWithCallback(capture::append);

        // چون در Impl از Thread.sleep(500) استفاده شده، کمی بیشتر صبر می‌کنیم.
        Thread.sleep(700);

        String result = capture.toString();
        assertThat("callback نباید خالی باشد!", result, not(emptyOrNullString()));
        assertThat(result, anyOf(containsString("✅"), containsString("❌")));
    }

    /**
     * asyncProcess()
     * ------------------------------------------
     * متد Async با CompletableFuture.
     * - ممکن است موفق شود ("✅ موفق شد!") → مستقیماً برمی‌گردد.
     * - یا شکست بخورد → Future با استثناء تکمیل می‌شود.
     *
     * در حالت شکست:
     *   - get() یک ExecutionException می‌دهد.
     *   - علت اصلی (cause) باید پیام "❌ شکست خورد!" داشته باشد.
     */
    @Test
    void asyncProcess_shouldEitherCompleteSuccessfullyOrFail() throws Exception {
        CompletableFuture<String> future = service.asyncProcess();
        try {
            String result = future.get();
            assertThat(result, containsString("✅ موفق شد!"));
        } catch (ExecutionException ex) {
            assertThat(ex.getCause().getMessage(), containsString("❌ شکست خورد!"));
        }
    }

    /**
     * onSuccess() و onError()
     * ------------------------------------------
     * این متدها هندلرهایی هستند که در chain (thenApply / exceptionally) استفاده می‌شوند.
     * بررسی می‌کنیم که خروجی درست فرمت شده باشد.
     */
    @Test
    void handlers_shouldReturnFormattedMessages() {
        String ok = service.onSuccess("DATA");
        assertThat(ok, is("🎉 SUCCESS callback in Service: DATA"));

        String err = service.onError(new RuntimeException("X"));
        assertThat(err, is("💥 ERROR callback in Service: X"));
    }
}
