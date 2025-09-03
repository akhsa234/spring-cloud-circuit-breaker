package demo.unit.service;

import com.bahar.demo.service.ExternalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExternalServiceImplTestPure {

    private ExternalServiceImpl externalService;

    @BeforeEach
    void setUp() {
        externalService = new ExternalServiceImpl();
    }

    @Test
    void testCallExternalApi_FirstCall_ShouldFallback() {
        // چون counter = 1 (فرد) ⇒ خطا ⇒ fallbackMethod
        String result = externalService.callExternalApi();

        assertThat(result)
                .contains("Fallback")
                .contains("❌ External API failed on attempt 1");
    }

    @Test
    void testCallExternalApi_SecondCall_ShouldSuccess() {
        // بار اول (counter=1): fallback
        externalService.callExternalApi();

        // بار دوم (counter=2): موفقیت
        String result = externalService.callExternalApi();

        assertThat(result)
                .contains("✅ Success on attempt 2");
    }

    @Test
    void testCallExternalApi_ThirdCall_ShouldFallbackAgain() {
        // بار اول ⇒ fallback
        externalService.callExternalApi();
        // بار دوم ⇒ success
        externalService.callExternalApi();

        // بار سوم ⇒ دوباره fallback
        String result = externalService.callExternalApi();

        assertThat(result)
                .contains("Fallback")
                .contains("❌ External API failed on attempt 3");
    }
}
