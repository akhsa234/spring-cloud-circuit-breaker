package demo.unit.controller;

import com.bahar.demo.controller.ExternalController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.bahar.demo.service.ExternalService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// فقط controller رو بارگذاری کن
@WebMvcTest(controllers = ExternalController.class)
class ExternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExternalService externalService;

    @Test
    void shouldReturnSuccess_whenServiceRespondsNormally() throws Exception {
        // Mock موفق
        when(externalService.call()).thenReturn("✅ External service success!");

        mockMvc.perform(get("/api/call"))
                .andExpect(status().isOk())
                .andExpect(content().string("✅ External service success!"));
    }

    /**
     * کاری که می‌کنه:
     *
     * می‌گه هر وقت متد call() صدا زده شد، جواب موفقیت "✅ External service success!" بده.
     *
     * بعد با mockMvc.perform(get("/api/call")) یک درخواست GET به آدرس /api/call می‌فرسته (مثل کلاینت واقعی).
     *
     * انتظارش اینه که:
     *
     * وضعیت پاسخ HTTP → 200 OK باشه.
     *
     * محتوای پاسخ → "✅ External service success!" باشه.
     * @throws Exception
     */

    @Test
    void shouldReturnFallback_whenServiceFails() throws Exception {
        // Mock شکست
        when(externalService.call()).thenThrow(new RuntimeException("Simulated failure"));

        mockMvc.perform(get("/api/call"))
                .andExpect(status().isOk())
                .andExpect(content().string("⚠️ Fallback: service unavailable (“Simulated failure”)"));
    }

    /**
     *
     * کاری که می‌کنه:
     *
     * این بار سرویس رو طوری Mock می‌کنه که وقتی call() صدا زده شد، Exception بده.
     *
     * کنترلر باید خطا رو هندل کنه و به جای خطای خام، پیام fallback بده.
     *
     * تست انتظار داره:
     *
     * وضعیت پاسخ HTTP همچنان 200 OK باشه (چون کنترلر خطا رو مدیریت کرده).
     *
     * متن پاسخ → "⚠️ Fallback: service unavailable (“Simulated failure”)” باشه.
     */

}

/**
 *
 * با @WebMvcTest فقط لایه‌ی Controller بارگذاری می‌شه (نه کل Spring Context).
 *
 * این کار باعث می‌شه تست سریع‌تر باشه و فقط رفتار API بررسی بشه.
 *
 * اینجا داریم ExternalController رو تست می‌کنیم.
 *
 * MockMvc → برای شبیه‌سازی درخواست HTTP به کنترلر استفاده می‌شه.
 *
 * @MockitoBean ExternalService → سرویس رو Mock می‌کنیم تا به جای واقعی، نسخه‌ی ساختگی‌اش استفاده بشه.
 * (یعنی کنترلر وقتی externalService.call() صدا می‌زنه، جواب از ما میاد نه از کلاس اصلی).
 *
 *
 *
 * خلاصه رفتار تست‌ها
 *
 * تست اول → وقتی سرویس درست جواب بده، خروجی کنترلر همون موفقیت باشه.
 *
 * تست دوم → وقتی سرویس خطا بده، کنترلر باید خروجی fallback بده و اجازه نده Exception خام به کلاینت برگرده.
 */
