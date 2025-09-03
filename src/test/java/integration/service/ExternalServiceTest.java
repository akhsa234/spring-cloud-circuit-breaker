package integration.service;

import com.bahar.demo.DemoApplication;
import com.bahar.demo.service.ExternalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DemoApplication.class)
class ExternalServiceTest {//تست با Spring Context

    @Autowired
    private ExternalService externalService;

    @Test
    void testSuccessAndFailure() {
        // بار اول احتمالاً خطا
        String firstCall = externalService.callExternalApi();
        assertThat(firstCall.contains("Fallback") || firstCall.contains("Success")).isTrue();


        // بار دوم موفقیت
        String secondCall = externalService.callExternalApi();
        assertThat(secondCall).contains("Success");
    }

    /**
     * منطق تست:
     *
     * بار اول که متد callExternalApi() صدا زده می‌شه، چون پیاده‌سازی‌اش طوری بود که دفعه‌ی فرد خطا می‌ده → یا مستقیماً Exception پرتاب می‌شه یا CircuitBreaker/Retry می‌ره به fallback.
     *
     * بنابراین تست چک می‌کنه خروجی شامل "Fallback" یا "Success" باشه.
     *
     * بار دوم (counter زوج می‌شه) باید حتماً موفقیت برگردونه → پس تست انتظار داره متن "Success" حتماً توی خروجی باشه.
     */

    @Test
    void testFallback() {
        String response = externalService.fallbackMethod(new RuntimeException("Test Exception"));
        assertThat(response).contains("Fallback response");
    }
    /**
     * اینجا مستقیم fallbackMethod صدا زده می‌شه.
     *
     * بهش یک Exception تستی داده می‌شه (RuntimeException("Test Exception")).
     *
     * انتظار تست اینه که خروجی رشته‌ای باشه که شامل "Fallback response" است.
     *
     * این متد مستقل از CircuitBreaker تست می‌کنه که fallbackMethod درست کار می‌کنه.
     */
}

/**
 *
 * اینجا از @SpringBootTest استفاده شده → کل Spring Context بارگذاری می‌شه (برخلاف @WebMvcTest که فقط لایه وب بود).
 *
 * این یعنی Bean واقعی ExternalServiceImpl ساخته می‌شه و تست‌ها روی نسخه‌ی اصلی اجرا می‌شن.
 *
 * سرویس اصلی (ExternalServiceImpl) توسط Spring تزریق (Inject) می‌شه.
 *
 * تست‌ها روی همین نسخه اجرا می‌شن، نه Mock.
 *
 *
 * خلاصه رفتار تست‌ها
 *
 * testSuccessAndFailure() → بررسی می‌کنه که متد واقعی callExternalApi() در اجرای اول خطا/فالبک برگردونه و در اجرای دوم حتماً موفق باشه.
 *
 * testFallback() → بررسی می‌کنه که متد fallbackMethod به‌درستی پیام جایگزین تولید می‌کنه.
 */