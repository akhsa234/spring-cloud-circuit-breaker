<<<<<<< HEAD
# spring-cloud-circuit-breaker
=======
"# My Spring Boot Project" 
>>>>>>> 6f6dc17 (Add README.md)# 🚀 Spring Boot + Resilience4j (Circuit Breaker, Retry, Fallback, Callback)

این پروژه یک نمونه ساده از استفاده‌ی **Spring Boot** به همراه
**spring-cloud-starter-circuitbreaker-reactor-resilience4j** هست که در آن:
- **Circuit Breaker** برای جلوگیری از شکست زنجیره‌ای سرویس‌ها
- **Retry** برای تلاش دوباره در صورت شکست
- **Fallback** برای بازگرداندن پاسخ جایگزین
- **Callback** برای واکنش به نتیجه‌ی عملیات

پیاده‌سازی شده.

---

## ⚙️ وابستگی‌ها (pom.xml)
```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- Resilience4j -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
    </dependency>

    <!-- Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>


src/main/java/com/example/demo
 ├─ DemoApplication.java        ← کلاس اصلی
 ├─ controller/
 │    └─ ExternalController.java
 ├─ service/
 │    └─ ExternalService.java
 └─ service/impl/
      └─ ExternalServiceImpl.java

src/test/java/com/example/demo
 ├─ unit/
 │    └─ ExternalServiceImplTest.java        ← تست یونیت (بدون Spring Context)
 └─ integration/
      ├─ ExternalServiceIntegrationTest.java ← تست سرویس
      └─ ExternalServiceControllerTest.java  ← تست کنترلر


mvn spring-boot:run

GET http://localhost:8080/api/external



---

👆 اینو فقط کپی کن به اسم `README.md` بنداز توی ریشه‌ی پروژه‌ت.  
می‌خوای من برات بخش **نمونه‌ی Callback** (با CompletableFuture یا EventListener) هم اضافه کنم تو README تا کامل‌تر بشه؟

demo-resilience4j/
 ├─ src/
 │   ├─ main/
 │   │   └─ java/
 │   │       └─ com/example/demo/
 │   │           ├─ DemoApplication.java   ← کلاس اصلی با @SpringBootApplication
 │   │           ├─ controller/            ← کنترلرها
 │   │           ├─ service/               ← اینترفیس‌ها
 │   │           └─ service/impl/          ← پیاده‌سازی سرویس‌ها
 │   │
 │   └─ test/
 │       └─ java/
 │           └─ com/example/demo/
 │               ├─ unit/                  ← تست‌های یونیت (بدون Spring Context)
 │               │    └─ ExternalServiceImplTest.java
 │               │
 │               └─ integration/           ← تست‌های اینتگریشن (با Spring Context)
 │                    └─ ExternalServiceIntegrationTest.java
 │
 └─ pom.xml

# 🔄 تفاوت **Fallback** و **Callback**

## 📌 تعریف ساده
- ⚠️ **Fallback**: راه‌حل جایگزین وقتی عملیات اصلی شکست بخوره یا سرویس در دسترس نباشه.  
- 📞 **Callback**: تابع یا کدی که بعد از اتمام یک عملیات یا وقوع یک رویداد به‌طور خودکار فراخوانی میشه.  

---

## 🆚 مقایسه Fallback و Callback

| ⭐ ویژگی        | 🛡️ Fallback                                          | 🔔 Callback                                               |
|-----------------|------------------------------------------------------|-----------------------------------------------------------|
| ⏰ **زمان اجرا** | فقط وقتی خطا یا شکست رخ بده                         | هر زمان که عملیات اصلی تموم بشه یا رویدادی بیفته         |
| 🎯 **هدف**      | جلوگیری از شکست کل سیستم و برگردوندن پاسخ جایگزین   | اجرای کدی که بعداً لازمه (مثلاً بعد از Async)             |
| 🛠️ **کاربرد**  | Resilience (پایداری در برابر خطا)                   | Event-driven و Async Programming                          |
| 🌍 **مثال روزمره** | ⚡ وقتی برق میره، ژنراتور روشن میشه (Plan B)       | ☎️ وقتی غذا آماده شد، رستوران بهت زنگ میزنه (اطلاع‌رسانی) |

---

## ✨ جمع‌بندی
- 🛡️ **Fallback** = همیشه یک Plan B برای مدیریت خطا داشته باش.  
- 🔔 **Callback** = کدی که بعداً اجرا میشه وقتی اتفاقی افتاد.  


# 🚀 Spring Boot + Resilience4j (Circuit Breaker, Retry, Fallback, Callback)

...

---

## 🔎 جریان معماری (Mermaid Diagram)

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant Service
    participant CircuitBreaker
    participant Fallback

    Client->>Controller: HTTP GET /api/external
    Controller->>Service: callExternalApi()
    Service->>CircuitBreaker: اجرا با CircuitBreaker + Retry
    alt موفقیت
        CircuitBreaker-->>Service: پاسخ موفق
        Service-->>Controller: ✅ Success
        Controller-->>Client: ✅ Success Response
    else شکست
        CircuitBreaker-->>Fallback: فراخوانی fallbackMethod()
        Fallback-->>Controller: ⚠️ Fallback response
        Controller-->>Client: ⚠️ Fallback Response
    end

| ویژگی       | **Fallback**                                      | **Callback**                             |
| ----------- | ------------------------------------------------- | ---------------------------------------- |
| زمان اجرا   | فقط وقتی خطا رخ بده                               | بعد از اتمام عملیات یا رویداد            |
| هدف         | جلوگیری از شکست کل سیستم و برگرداندن پاسخ جایگزین | اجرای کدی که بعداً لازمه (مثلاً Async)   |
| کاربرد اصلی | Resilience (پایداری در برابر خطا)                 | Event-driven و Async Programming         |
| مثال روزمره | وقتی برق میره، ژنراتور روشن میشه (Plan B)         | وقتی غذا آماده شد، رستوران بهت زنگ میزنه |
