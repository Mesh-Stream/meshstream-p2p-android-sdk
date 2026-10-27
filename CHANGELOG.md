# Changelog

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
