# ⚡ PicoBeam

**Local peer-to-peer file transfer, at light speed.**

![License](https://img.shields.io/badge/license-GPLv3-blue.svg)
![Downloads](https://img.shields.io/badge/APK-%3C8%20MB-00f0ff.svg)
![Android](https://img.shields.io/badge/Android-8.0%2B-3ddc84.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7f52ff.svg)
![Ktor](https://img.shields.io/badge/Ktor-3.6-0095d5.svg)

---

## Why PicoBeam?

Legacy file-shares are heavy: ads, accounts, telemetry, gigabytes of bloat.
PicoBeam does one job — **transfer files directly between devices over your
local Wi-Fi** — and nothing else. The final APK stays **under 8 MB**.

- 🌐 **100% offline** — no cloud, no middle server, no accounts.
- ⚡ **Up to 80 MB/s** over the local network via resumable HTTP Range.
- 📱 **Pico Web-Share** — your phone becomes a local server; any device
  (iPhone, PC, TV) downloads through its browser with one QR scan.
- 🔒 **Private by design** — files never leave your network.
- 📦 **Featherweight** — a surgical feature set, engineered for speed.
- ♻️ **Resumable transfers** — pick up exactly where you stopped.

## Download

Grab the latest signed APK from the [Releases](https://github.com/Shadow132245/PicoBeam/releases)
page, or hit the download button on the **[PicoBeam website](https://picobeam.vercel.app/)**.

## Feature Roadmap

- [x] Offline device-to-device transfer over Wi-Fi
- [x] HTTP Range resumable downloads (parallel, N=3)
- [x] Browser Web-Share page served by the embedded server
- [x] QR (send side) + URL session pairing
- [x] SAF content picker with persistable permissions
- [x] R8-minified release APK — **3.2 MB**
- [x] 4-language website (English · العربية · Français · Español) with dark/light themes
- [ ] iOS companion (SwiftUI)

## Architecture

```
PicoBeam/
├── android/
│   ├── engine/     # Pure-JVM engine (Ktor CIO server + client) — testable without a device
│   └── app/        # Jetpack Compose UI (AGP 9 built-in Kotlin)
├── web/            # Next.js landing page (4 languages, dark/light)
└── .github/
    └── workflows/  # CI: build → size gate (<8 MB) → sign → release
```

- **Kotlin / Jetpack Compose** for the Android app
- **Ktor 3 (CIO)** for the embedded HTTP server and resumable client
- **Next.js 16 + Tailwind CSS 4 + Motion** for the website
- **GitHub Actions** for the entire release pipeline

> A small detail: the engine lives in its own pure-JVM module so the whole
> transfer stack is covered by integration tests that run anywhere — no emulator.

## Building

```bash
# Android (JDK 21)
cd android
./gradlew :engine:test        # run the 8 integration tests
./gradlew :app:assembleRelease
# → app/build/outputs/apk/release/app-release-unsigned.apk (~3.2 MB)

# Website
cd web
npm install
npm run build
```

The release pipeline is wired as a GitHub Action: it builds, **fails if the APK
exceeds 8 MB**, signs with the repository keystore secrets, and publishes the
artifact to the GitHub Release on version tags (`v*`).

## Releases

| Version | Highlights |
|---|---|
| `v1.0.0` | Initial release — send, web-share, resume, receive-all |

## License

**[GPL v3](LICENSE)** — GNU General Public License, version 3.

Copyright (C) 2026 **Hassan (a.k.a. EuroMoscow)**.

You are free to use, study, share, and modify this software. If you distribute
modified versions, you must do so under the same license and keep it open.
See the [LICENSE](LICENSE) file for the full text.