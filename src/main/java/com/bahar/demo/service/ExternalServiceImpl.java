package com.bahar.demo.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * پیاده‌سازی ExternalService
 * -------------------------
 * شامل:
 *  - یک متد SYNC ساده (call)
 *  - یک متد SYNC با CircuitBreaker/Retry (callExternalApiSync + fallbackMethod)
 *  - دو مدل Callback: Consumer و CompletableFuture
 *  - هندلرهای onSuccess/onError برای chain در CompletableFuture
 */
@Service
public class ExternalServiceImpl implements ExternalService {

    /** شمارنده‌ی فراخوانی‌ها برای شبیه‌سازی الگوی شکست/موفقیت در حالت sync */
    private int counter = 0;

    /**
     * متد تست ساده (SYNC)
     * -------------------------
     * با احتمال ۶۰٪ استثناء پرتاب می‌کند تا سناریوی شکست را شبیه‌سازی کند.
     *
     * @return پیام موفقیت
     * @throws RuntimeException در صورت شبیه‌سازی خطا
     */
    @Override
    public String call() {
        if (Math.random() < 0.6) {
            throw new RuntimeException("Simulated failure in external service");
        }
        return "✅ External service success!";
    }

    /**
     * متد SYNC با Resilience4j (CircuitBreaker + Retry)
     * -------------------------
     * منطق شبیه‌سازی:
     *  - هر بار فراخوانی، counter++ می‌شود.
     *  - اگر counter زوج باشد: موفقیت
     *  - اگر فرد باشد: استثناء (شکست)
     *
     * رفتار Resilience4j:
     *  - @Retry(name="externalService"): در صورت شکست، چند بار تلاش مجدد می‌کند (طبق yml).
     *  - @CircuitBreaker(name="externalService", fallbackMethod="fallbackMethod"):
     *      اگر نرخ شکست زیاد شود یا مدار باز باشد، به جای متد اصلی، fallbackMethod صدا زده می‌شود.
     *
     * نکته: امضای fallback باید با امضای متد همخوان باشد (نوع خروجی یکسان + پارامتر Exception در انتها).
     *
     * @return پیام موفقیت در تلاش‌های زوج
     * @throws RuntimeException در تلاش‌های فرد (پیش از اعمال مکانیزم‌های resilience)
     */
    @Override
    @CircuitBreaker(name = "externalService", fallbackMethod = "fallbackMethod")
    @Retry(name = "externalService")
    public String callExternalApiSync() {
        counter++;
        if (counter % 2 == 0) {
            return "✅ Success on attempt " + counter;
        } else {
            throw new RuntimeException("❌ External API failed on attempt " + counter);
        }
    }

    /**
     * پاسخ جایگزین (Fallback) برای callExternalApiSync
     * -------------------------
     * توسط Resilience4j هنگام شکست نهایی یا مدار باز فراخوانی می‌شود.
     *
     * @param e استثناء اصلی رخ‌داده
     * @return پیام fallback کاربرپسند
     */
    @Override
    public String fallbackMethod(Exception e) {
        return "⚠️ Fallback response because: " + e.getMessage();
    }

    /**
     * مدل Callback ساده با Consumer
     * -------------------------
     * کار را در یک Thread جدا اجرا می‌کند (شبیه‌سازی async).
     * بعد از ۵۰۰ms:
     *  - در سناریوی موفقیت → callback.accept("✅ ...")
     *  - در سناریوی InterruptedException → callback.accept("❌ ...")
     *
     * توجه: این روش purely callback-style است و Future برنمی‌گرداند.
     *
     * @param callback تابعی که نتیجه/پیام را دریافت می‌کند
     */
    @Override
    public void processWithCallback(Consumer<String> callback) {
        new Thread(() -> {
            try {
                Thread.sleep(500); // شبیه‌سازی تأخیر
                callback.accept("✅ کار با موفقیت انجام شد!");
            } catch (InterruptedException e) {
                callback.accept("❌ خطا در پردازش");
                Thread.currentThread().interrupt(); // رعایت best practice
            }
        }).start();
    }

    /**
     * مدل async با CompletableFuture
     * -------------------------
     * عملیات را به‌صورت غیرهمزمان اجرا می‌کند:
     *  - ۵۰٪ احتمال پرتاب استثناء (شکست)
     *  - ۵۰٪ احتمال موفقیت
     *
     * مدیریت نتیجه:
     *  - در Controller با thenApply(onSuccess) و exceptionally(onError) هندل می‌شود.
     *
     * @return CompletableFuture از رشته‌ی نتیجه (موفقیت) یا استثناء (شکست)
     */
    @Override
    public CompletableFuture<String> asyncProcess() {
        return CompletableFuture.supplyAsync(() -> {
            if (Math.random() < 0.5) {
                throw new RuntimeException("❌ شکست خورد!");
            }
            return "✅ موفق شد!";
        });
    }

    /**
     * هندلر موفقیت برای chain
     * -------------------------
     * در thenApply(...) استفاده می‌شود.
     *
     * @param result خروجی خام عملیات async
     * @return پیام کاربرپسندِ موفقیت
     */
    @Override
    public String onSuccess(String result) {
        return "🎉 SUCCESS callback in Service: " + result;
    }

    /**
     * هندلر خطا برای chain
     * -------------------------
     * در exceptionally(...) استفاده می‌شود.
     *
     * @param ex استثناء رخ‌داده
     * @return پیام کاربرپسندِ خطا
     */
    @Override
    public String onError(Throwable ex) {
        return "💥 ERROR callback in Service: " + ex.getMessage();
    }
}
