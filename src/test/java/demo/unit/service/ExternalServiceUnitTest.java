package demo.unit.service;


import com.bahar.demo.service.ExternalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalServiceUnitTest {
    //تست Unit ساده (بدون Spring Context)

    private ExternalServiceImpl externalService;

    @BeforeEach
    void setUp() {
        externalService = new ExternalServiceImpl();
    }

    /**
     * این متد قبل از هر تست اجرا می‌شه.
     *
     * یک نمونه‌ی جدید از ExternalServiceImpl ساخته می‌شه.
     *
     * باعث می‌شه تست‌ها مستقل از هم باشن و هر بار روی شیء تازه اجرا بشن.
     */

    @Test
    void testSuccessAndFallback() {
        // بار اول → شکست (fallback)
        String first = externalService.callExternalApi();
        assertThat(first.contains("Fallback") || first.contains("Success")).isTrue();

        // بار دوم → موفقیت
        String second = externalService.callExternalApi();
        assertThat(second).contains("Success");
    }

    /**
     * مرحله اول:
     *
     * یک بار callExternalApi() فراخوانی می‌شه.
     *
     * انتظار می‌ره که در اولین بار یا "Fallback" (یعنی حالت شکست و بازگشت به مسیر جایگزین) برگرده یا "Success" (یعنی موفقیت).
     *
     * مرحله دوم:
     *
     * بار دوم دوباره callExternalApi() اجرا می‌شه.
     *
     * این بار طبق منطق پیاده‌سازی انتظار می‌ره حتماً موفق بشه.
     *
     * پس تست بررسی می‌کنه خروجی حتماً شامل "Success" باشه:
     */
}

/**
 * خلاصه:
 *
 * setUp() → آماده‌سازی آبجکت قبل از هر تست.
 *
 * testSuccessAndFallback() → بررسی می‌کنه که:
 *
 * دفعه‌ی اول فراخوانی احتمالاً شکست بخوره و fallback برگرده.
 *
 * دفعه‌ی دوم فراخوانی حتماً موفق بشه.
 */