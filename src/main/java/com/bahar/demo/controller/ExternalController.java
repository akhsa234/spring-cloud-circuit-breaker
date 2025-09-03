package com.bahar.demo.controller;

import com.bahar.demo.service.ExternalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * ExternalController
 * -------------------------
 * سه endpoint برای نمایش تفاوت سناریوها:
 *  1) /api/callSync  → فراخوانی SYNC با CircuitBreaker/Retry (در لایه سرویس)
 *  2) /api/callAsync → فراخوانی ASYNC با CompletableFuture و callback chain
 *  3) /api/callback  → مدل callback ساده با Consumer (bridge به HTTP)
 *
 * نکته: CircuitBreaker و Retry روی متد سرویس اعمال شده‌اند (نه روی کنترلر)
 * تا منطق resilience در لایه‌ی دامنه متمرکز بماند.
 */
@RestController
@RequestMapping("/api")
public class ExternalController {

    /** وابستگی به سرویس از طریق سازنده تزریق می‌شود (Constructor Injection) */
    private final ExternalService externalService;

    /**
     * سازنده‌ی کنترلر
     *
     * @param externalService سرویس خارجی تزریق‌شده توسط Spring
     */
    public ExternalController(ExternalService externalService) {
        this.externalService = externalService;
    }

    /**
     * فراخوانی SYNC با CircuitBreaker/Retry
     * -------------------------
     * مسیر: GET /api/callSync
     * رفتار:
     *  - کنترلر مستقیم متد sync سرویس را صدا می‌زند.
     *  - شکست/موفقیت و fallback توسط خود سرویس (Resilience4j) مدیریت می‌شود.
     *
     * پاسخ‌های ممکن:
     *  - "✅ Success on attempt X" در صورت موفقیت
     *  - "⚠️ Fallback response because: ..." در صورت رفتن به fallback
     *
     * @return متن نهایی برای کلاینت
     */
    @GetMapping("/callSync")
    public String callExternalSync() {
        return externalService.callExternalApiSync();
    }

    /**
     * فراخوانی ASYNC با CompletableFuture (callback chain)
     * -------------------------
     * مسیر: GET /api/callAsync
     * جریان:
     *  - متد asyncProcess() یک CompletableFuture برمی‌گرداند.
     *  - thenApply(externalService::onSuccess) فقط روی موفقیت اجرا می‌شود.
     *  - exceptionally(externalService::onError) فقط روی خطا اجرا می‌شود.
     *
     * @return CompletableFuture از متن نهایی (Spring به طور خودکار پاسخ را پس از تکمیل برمی‌گرداند)
     */
    @GetMapping("/callAsync")
    public CompletableFuture<String> callExternalAsync() {
        return externalService.asyncProcess()
                .thenApply(externalService::onSuccess)     // ✅ موفقیت
                .exceptionally(externalService::onError);  // ❌ خطا
    }

    /**
     * مدل Callback ساده با Consumer (پل به HTTP)
     * -------------------------
     * مسیر: GET /api/callback
     * ایده:
     *  - متد سرویس processWithCallback(callback) خروجی مستقیمی ندارد.
     *  - برای برگشت پاسخ HTTP، یک CompletableFuture دستی می‌سازیم (promise)
     *  - callback سرویس را به promise::complete وصل می‌کنیم تا هرچه سرویس گفت، پاسخ HTTP شود.
     *
     * @return CompletableFuture از پاسخ تولیدشده توسط callback سرویس
     */
    @GetMapping("/callback")
    public CompletableFuture<String> callWithCallbackBridge() {
        CompletableFuture<String> promise = new CompletableFuture<>();
        externalService.processWithCallback(promise::complete);
        return promise;
    }
}
