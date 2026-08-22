# PlanDee ⚡📱

> **Smart Data & Telecom Telemetry Analytics Platform for Android**

PlanDee is an intelligent Android application designed to track, analyze, and optimize mobile and Wi-Fi data consumption for telecom subscribers in emerging markets. Built with Kotlin and Jetpack Compose, PlanDee combines real-time network telemetry, GlassWire-level per-app traffic tracking, automated tariff plan optimization, AdMob reward monetization, RevenueCat subscription entitlements, and instant Virtual Top-Up (VTU) data purchases.

---

## 🚀 Features

- **Real-Time Data Telemetry Tracker**: Monitors active network state changes (Wi-Fi vs. Mobile Data) with immediate session byte tracking and custom push notification alerts upon reaching spend milestones.
- **GlassWire-Level App Ranking**: Captures granular per-app data consumption across both user applications (YouTube, Telegram, WhatsApp, Duolingo, Spotify) and background system services.
- **AI Tariff Plan Matcher**: Analyzes 30-day data burn rates, active SIM carriers (MTN, Airtel, Glo, 9mobile), and budget limits to recommend optimal telecom tariff packages.
- **Instant VTU Data Top-Up Gateway**: Direct virtual top-up data purchase integration alongside instant USSD dial shortcuts for carrier plan activation.
- **AdMob Rewarded Token System**: Earn recommendation tokens by watching rewarded video ads.
- **RevenueCat Pro Subscriptions**: Tiered subscription management (Monthly, Yearly, Lifetime) unlocking unlimited AI recommendations and advanced analytics.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: Jetpack Compose with Material Design 3 and a modern Retro-Tactile theme system.
- **Language**: Kotlin 2.0+ with Coroutines & StateFlow.
- **Network Layer**: Retrofit 2 + OkHttpClient with JWT Authorization Interceptor.
- **Database & Storage**: SQLite (`SQLiteOpenHelper`) for high-speed local telemetry logs, and `EncryptedSharedPreferences` for secure session token storage.
- **Background Tasks**: Android `WorkManager` (`CoroutineWorker`) scheduling periodic 6-hour background server synchronization.
- **Monetization**: Google Mobile Ads SDK (AdMob Rewarded Ads) + RevenueCat Purchases SDK (`Purchases`).

---

## 📁 Project Structure

```
app/src/main/java/com/example/plandee/
├── MainActivity.kt                  # Main entry point & background worker setup
├── PlanDeeApplication.kt            # Application class, AdMob & RevenueCat init
├── data/
│   ├── db/
│   │   └── TrafficDatabaseHelper.kt # SQLite database helper for network and app logs
│   ├── monetization/
│   │   ├── ProRepository.kt         # RevenueCat entitlements & purchase manager
│   │   └── RewardedAdManager.kt     # AdMob rewarded ad loader and display controller
│   ├── network/
│   │   ├── ApiService.kt            # Retrofit REST API interface & DTO definitions
│   │   ├── AuthInterceptor.kt       # JWT Bearer token HTTP header interceptor
│   │   ├── RetrofitClient.kt        # Retrofit singleton client builder
│   │   └── TokenAuthenticator.kt    # 401 Unauthorized handling & session reset
│   ├── repository/
│   │   └── TrafficRepository.kt     # Telemetry data aggregation and calculations
│   ├── security/
│   │   └── SessionManager.kt        # EncryptedSharedPreferences wrapper
│   └── telemetry/
│       ├── DataUsageNotificationService.kt # Push notification alert manager
│       ├── TelemetrySyncWorker.kt   # WorkManager periodic background sync
│       ├── TrafficMonitor.kt        # ConnectivityManager & TrafficStats listener
│       └── UsagePermissionBridge.kt # Usage Access permission checker
├── navigation/
│   └── PlanDeeNavGraph.kt           # Compose Navigation graph routes
├── ui/
│   ├── components/                  # Custom Compose components & dialogs
│   ├── screens/                     # App screens (Splash, Auth, Onboarding, Dashboard)
│   └── theme/                       # Color palettes, Typography, Theme definition
└── viewmodels/                      # Architecture ViewModels & UI States
```

---

## ⚙️ Getting Started

### Prerequisites

- **Android Studio**: Ladybug / Jellyfish or newer
- **JDK**: Java 17 or higher
- **Android SDK**: Min SDK 24 (Android 7.0), Target SDK 35

### Building & Running

1. **Clone the repository**:
   ```bash
   git clone https://github.com/whotterre/plandee-app.git
   cd plandee-app
   ```

2. **Configure API Base URL**:
   Update `BASE_URL` in `data/network/RetrofitClient.kt` to point to your backend server instance.

3. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit & Build Checks**:
   ```bash
   ./gradlew compileDebugSources
   ```

---

## 🔒 Permissions & Security

PlanDee uses Android system permissions strictly for telemetry aggregation:
- `PACKAGE_USAGE_STATS`: For per-app Wi-Fi and Mobile data ranking (requires explicit user grant in Android Settings).
- `INTERNET` & `ACCESS_NETWORK_STATE`: For real-time network state detection and server synchronization.
- `POST_NOTIFICATIONS`: For sending data limit threshold push notifications (Android 13+).

---

## 📄 License

Copyright © 2026 PlanDee. All rights reserved.
