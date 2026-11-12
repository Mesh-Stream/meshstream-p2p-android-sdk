# Changelog

## 1.1.0 — 2026-11-12

- Uses the Tracker-provided announce interval and resets discovery timing after reconnect.
- Avoids bursts of delayed announces after a busy or suspended runtime.
- Preserves CDN playback and transfer statistics while bootstrap recovers.

## 1.0.10 - 2026-11-07

- Publishes the universal Android release built from native SDK 1.0.10, including the Android JNI configuration refresh and the `bootstrap_refresh_at` initialization fix.
- Keeps ARM32/x86 API 17+ and ARM64/x86_64 API 21+ support with 16 KB aligned 64-bit libraries.
- Updates the Media3 example and exact Maven coordinates to 1.0.10.

## 1.0.9 - 2026-11-01

- Adds x86 (API 17+) and x86_64 (API 21+) to the universal AAR, alongside armeabi-v7a and arm64-v8a.
- Preserves 16 KB ELF LOAD alignment for all packaged 64-bit native libraries.
- Keeps the existing public API and ARM minimum Android versions.
- Updates the Media3 example and exact Maven dependency to 1.0.9.
- Finishes the demo Activity on Back so its player and SDK session are released on Android 12+.
- Allows only loopback HTTP traffic in the Media3 example so Android network policy permits the local playback proxy.

## 1.0.8 - 2026-11-01

- Publishes a universal AAR containing ARM32 (`armeabi-v7a`, API 17 target) and ARM64 (`arm64-v8a`, API 21 target).
- Retains 16 KB ELF LOAD alignment for both ARM64 libraries.
- Builds ARM32 using NDK 23.2.8568313 and ARM64 using NDK 27.2.12479018.
- Updates the Media3 example to Maven 1.0.8. The demo still requires API 24.
- Verifies 36 Debug tests, 36 minified Release tests, and 247 native tests.

## 1.0.7 - 2026-10-31

- Updates the ARM64 release to native SDK 1.0.7.
- Ensures proxy connections are closed during SDK shutdown, including existing keep-alive connections.
- Records the verified AAR checksum and 16 KB ELF alignment result.

Version and artifact verification details: `releases/1.0.7.json`.

## 1.0.6 - 2026-10-29

- Publishes the ARM64 release built and verified from P2PCDN native SDK 1.0.6.
- Includes the preload swarm prewarm and idle cleanup changes.
- Records the verified AAR SHA-256 and Android 16 KB ELF alignment checks.


## 1.0.5 - 2026-10-27

- Publishes the ARM64 release built and verified from P2PCDN native SDK 1.0.5.
- Updates the Media3 example and Maven coordinates to 1.0.5.
- Records the verified AAR SHA-256 and public Maven verification endpoints.
- Confirms both `sdk.meshstream.net/maven` and the R2 `r2.dev` mirror serve the
  same 1.0.5 metadata and artifact.

## 1.0.4 - 2026-10-27

- Publishes the ARM64 release built and verified from P2PCDN native SDK 1.0.4.
- Updates the Media3 example and Maven coordinates to 1.0.4.
- Records the verified AAR SHA-256 and formal Maven repository URL.

All notable changes to the public MeshStream Android SDK distribution are
documented in this file.

## 1.0.2 - 2026-10-20

- Preserves the public diagnostics, status, playback-mode, and playback-options
  types in minified consumer builds.
- Keeps the published Maven artifact compatible with the React Native bridge.

## 1.0.0 - 2026-10-18

- First public Maven release of the Android SDK.
- Provides independent client and playback-session lifecycles for concurrent
  players.
- Includes HLS/DASH acceleration, bootstrap configuration, P2P telemetry, and
  CDN continuity fallback.
- Publishes an ARM64 (`arm64-v8a`) Android artifact and a Media3 integration
  example.
