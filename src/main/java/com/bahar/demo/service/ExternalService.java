package com.bahar.demo.service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * اینترفیس ExternalService
 * -------------------------
 * قرارداد سرویس خارجی:
 *  - متدهای همزمان (sync) و غیرهمزمان (async)
 *  - متد fallback برای سناریوی شکست
 *  - دو سبک callback: Consumer و CompletableFuture
 *  - هندلرهای onSuccess و onError برای chain در CompletableFuture
 */
public interface ExternalService {

    /**
     * متد تست ساده (SYNC)
     * -------------------------
     * شبیه‌ساز یک فراخوانی خارجی ساده که ممکن است خطا بدهد.
     *
     * @return رشته‌ی موفقیت در صورت موفقیت
     * @throws RuntimeException در صورت شبیه‌سازی خطا
     */
    String call();

    /**
     * فراخوانی همزمان سرویس خارجی با محافظت Resilience4j
     * -------------------------
     * پیاده‌سازی این متد در Impl دارای CircuitBreaker و Retry است.
     * الگو: یک بار شکست، یک بار موفقیت (بر اساس شمارنده).
     *
     * @return پیام موفقیت در صورت موفق شدن
     * @throws RuntimeException در صورت شکست (قبل از اعمال fallback)
     */
    String callExternalApiSync();

    /**
     * پاسخ جایگزین (Fallback) برای سناریوی شکست
     * -------------------------
     * وقتی CircuitBreaker مدار را قطع کند یا تمام retryها شکست بخورد،
     * Resilience4j این متد را فراخوانی می‌کند.
     *
     * @param e استثناء اصلی رخ‌داده
     * @return پیام جایگزینِ کاربرپسند
     */
    String fallbackMethod(Exception e);

    /**
     * Callback ساده با Consumer
     * -------------------------
     * یک تابع callback می‌گیرد و پس از پایان کار (یا خطا) آن را فراخوانی می‌کند.
     * برای نمایش ایده‌ی "وقتی تموم شد، منو خبر کن".
     *
     * @param callback تابعی که با نتیجه/پیام نهایی فراخوانی می‌شود
     */
    void processWithCallback(Consumer<String> callback);

    /**
     * مدل غیرهمزمان با CompletableFuture
     * -------------------------
     * عملیات async را اجرا کرده و فوراً یک Future برمی‌گرداند.
     * نتیجه‌ی نهایی (موفق/شکست) بعداً آماده می‌شود.
     *
     * @return CompletableFuture از رشته‌ی نتیجه
     */
    CompletableFuture<String> asyncProcess();

    /**
     * هندلر موفقیت برای chain در CompletableFuture
     * -------------------------
     * مناسب برای thenApply(result -> ...).
     *
     * @param result رشته‌ی موفقیت برگردانده‌شده از عملیات async
     * @return متن پردازش‌شده‌ی موفقیت برای کاربر
     */
    String onSuccess(String result);

    /**
     * هندلر خطا برای chain در CompletableFuture
     * -------------------------
     * مناسب برای exceptionally(throwable -> ...).
     *
     * @param ex استثناء رخ‌داده در عملیات async
     * @return متن پردازش‌شده‌ی خطا برای کاربر
     */
    String onError(Throwable ex);
}
