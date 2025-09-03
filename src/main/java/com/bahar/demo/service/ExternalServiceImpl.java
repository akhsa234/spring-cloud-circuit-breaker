package com.bahar.demo.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

@Service
public class ExternalServiceImpl implements ExternalService {
    @Override
    public String call() {
        if (Math.random() < 0.6) {
            throw new RuntimeException("Simulated failure in external service");
        }
        return "✅ External service success!";
    }

    private int counter = 0;

    @CircuitBreaker(name = "myService", fallbackMethod = "fallbackMethod")
    @Retry(name = "myService")
    public String callExternalApi() {
        counter++;
        if (counter % 2 == 0) {
            return "✅ Success on attempt " + counter;
        } else {
            throw new RuntimeException("❌ External API failed on attempt " + counter);
        }
    }

    // fallback
    public String fallbackMethod(Exception e) {
        return "⚠️ Fallback response because: " + e.getMessage();
    }
}

/**
 *
 * این کلاس یک سرویس شبیه‌سازی‌شده است که هم متدهای عادی دارد، هم متدی که با Resilience4j (CircuitBreaker و Retry) محافظت شده.
 *
 * یک شبیه‌ساز ساده برای سرویس خارجی.
 *
 * با احتمال ۶۰٪ خطا می‌دهد (throw RuntimeException) و با احتمال ۴۰٪ پیام موفقیت برمی‌گرداند.
 *
 * این بیشتر برای تست سناریوی خطا در متدهای عادی است.
 *
 * شمارنده‌ای است که تعداد دفعات فراخوانی متد callExternalApi() را نگه می‌دارد.
 *
 * برای اینکه الگوی خطا/موفقیت قابل پیش‌بینی شود (یک بار شکست، یک بار موفقیت).
 *
 * callExternalApi()
 *
 * هر بار صدا زده بشه counter زیاد میشه.
 *
 * اگه شمارنده زوج باشه → جواب Success برمی‌گردونه.
 *
 * اگه فرد باشه → Exception می‌ندازه.
 *
 * چون روش @CircuitBreaker و @Retry هست:
 *
 * اگه خطا باشه → Resilience4j سعی می‌کنه دوباره retry کنه.
 *
 * اگه باز هم نشد → میره سراغ متد fallback.
 *
 *
 * فراخوانی سرویس خارجی است.
 *
 * ویژگی‌ها:
 *
 * @CircuitBreaker: اگر خطا زیاد رخ بدهد، مدار قطع می‌شود و دیگر به سرویس اصلی زنگ نمی‌زند؛ مستقیم می‌رود سراغ fallback.
 *
 * @Retry: اگر فراخوانی شکست بخورد، چند بار پشت سر هم دوباره تلاش می‌کند.
 *
 * منطق داخلی:
 *
 * هر بار که صدا زده می‌شود، counter یکی زیاد می‌شود.
 *
 * اگر counter زوج باشد → موفقیت برمی‌گرداند.
 *
 * اگر counter فرد باشد → استثنا پرتاب می‌کند (شکست).
 *
 * یعنی:
 *
 * بار ۱ → شکست
 *
 * بار ۲ → موفقیت
 *
 * بار ۳ → شکست
 *
 * بار ۴ → موفقیت
 * و به همین ترتیب.
 *
 * fallbackMethod(Exception e)
 *
 * وقتی متد اصلی شکست بخوره، این متد اجرا میشه.
 *
 * خروجی: یک پیام جایگزین با توضیح علت خطا.
 *
 * متد جایگزین است که وقتی CircuitBreaker یا Retry نتوانند سرویس را موفق کنند، فراخوانی می‌شود.
 *
 * پیام برمی‌گرداند که چرا سرویس به fallback رفته است (متن خطای اصلی را هم شامل می‌کند)
 *
 * .
 *
 *
 * خلاصه رفتار
 *
 * call() → یه شبیه‌ساز تصادفی موفقیت/شکست (۶۰٪ شکست).
 *
 * callExternalApi() → یک بار خطا، بار بعدی موفقیت (با CircuitBreaker و Retry محافظت شده).
 *
 * fallbackMethod() → وقتی همه‌ی تلاش‌ها شکست بخورد، پیام جایگزین می‌دهد.
 */