# MeshStream P2P Android SDK

[简体中文](README.zh-CN.md) · [Website](https://meshstream.net/) · [Documentation](https://meshstream.net/docs/sdk/android-setup/) · [Live demo](https://demo.meshstream.net/)

MeshStream Android SDK adds P2P video and audio delivery to Kotlin / Java apps using Media3, ExoPlayer or another URL-capable player. It supports HLS, MPEG-DASH and eligible HTTP Range MP4/MP3 requests through a local playback URL, with HTTP/CDN fallback when peers are unavailable.

## Features

- HLS, MPEG-DASH and HTTP Range MP4/MP3 delivery with origin/CDN fallback.
- Kotlin / Java API with Media3 / ExoPlayer integration examples.
- Session preload, peer events, traffic counters and diagnostics.
- Maven distribution with armeabi-v7a, arm64-v8a, x86 and x86_64 native libraries.

Media decoding and format support depend on your chosen player and platform. P2P availability depends on network conditions and peers with matching content.

## Release

The current release is **1.0.10**.

| Item | Value |
| --- | --- |
| Maven coordinate | `io.meshstream:meshstream-android-sdk:1.0.10` |
| Delivery | MeshStream Maven repository |
| Native ABI in this release | `armeabi-v7a`, `arm64-v8a`, `x86`, and `x86_64` (universal AAR) |
| Native build targets | ARM32/x86: Android 4.2 (API 17)+; ARM64/x86_64: Android 5.0 (API 21)+ |

One AAR serves ARM32/ARM64 devices and ARM64/x86/x86_64 emulators.
The Media3 demo requires API 24; a player compatible with older Android versions
is required on API 17–23.

## Add the dependency

Add the MeshStream Maven repository in `settings.gradle.kts` (Kotlin DSL):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://sdk.meshstream.net/maven")
            content { includeGroup("io.meshstream") }
        }
    }
}
```

Then add the SDK and the required player modules:

```kotlin
dependencies {
    implementation("io.meshstream:meshstream-android-sdk:1.0.10")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
}
```

Do not download or copy the AAR into your application. The Maven coordinate is
the supported distribution path for this release.

## Local playback network policy

The SDK returns an on-device HTTP proxy URL at `127.0.0.1`. Allow loopback cleartext traffic even when your media origin uses HTTPS. The API 24+ example includes `res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false" />
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">127.0.0.1</domain>
        <domain includeSubdomains="false">localhost</domain>
    </domain-config>
</network-security-config>
```

Reference it with `android:networkSecurityConfig="@xml/network_security_config"` on `<application>`. Merge these rules with any existing application policy. For Android 6.0 (API 23), per-domain exceptions are unavailable; set `android:usesCleartextTraffic="true"` if your application policy permits it. API 24+ uses the network security configuration instead. Public HTTP media origins require their own rule.

## Basic integration

Create a client with the SDK key issued in the MeshStream console, create a
playback session for each playback item, then pass the session URL to your
player.

```kotlin
val client = P2pSdk.create(
    applicationContext,
    P2pConfig(
        key = "your_sdk_key",
        logLevel = P2pLogLevel.NONE,
    ),
)

val session = client.createPlaybackSession(originUrl)
player.setMediaItem(MediaItem.fromUri(session.playbackUrl))
player.prepare()
player.play()
```

`createPlaybackSession` returns promptly. Service configuration and P2P
preparation proceed in the background, while the player can begin loading
through the returned URL. When P2P is unavailable, media requests continue via
the configured origin/CDN path.

Close every session at the end of its playback lifecycle. A client may own
multiple sessions concurrently, for example for an active player and a
preloaded player.

```kotlin
session.close()
P2pSdk.shutdown() // Application-wide shutdown when all clients are finished.
```

## Playback formats

HLS and DASH streams are handled through the same session API. Your player must
include the appropriate playback module. For Media3 DASH playback, add
`androidx.media3:media3-exoplayer-dash` and configure the player as usual.

For extensionless manifests, provide a format hint in `P2pConfig`:

```kotlin
P2pConfig(
    key = "your_sdk_key",
    mimeType = "m3u8", // or "mpd"
)
```

## Observability

Register `P2pEventListener` on the client to observe peer count, availability,
CDN fallback, and per-session traffic statistics. Do not use these callbacks to
block player startup; they describe acceleration state rather than playback
success.

```kotlin
client.addEventListener(object : P2pEventListener {
    override fun onPeerCountChanged(sessionId: Long, peerCount: Int) = Unit
    override fun onP2pUnavailable(sessionId: Long, reason: P2pUnavailableReason) = Unit
    override fun onStatistics(sessionId: Long, stats: P2pStats) = Unit
})
```

## Media3 example

[`Examples/Media3Demo`](Examples/Media3Demo) is a standalone Android Studio
project. Set `SDK_KEY` in `MainActivity.kt`, open that directory in Android
Studio, and run it on a supported device or emulator. It resolves the SDK from the public Maven
repository and contains no local SDK binary.

## Release verification

The metadata for each public release is stored under [`releases/`](releases).
For 1.0.10, the published artifact SHA-256 is:

```text
77a2ae287168c9e1eec858a7d51f7a253d6184ce0d9b91edb459d47e6d467614
```

The public Maven endpoint for this release is `https://sdk.meshstream.net/maven`.
Release verification uses the supported public domain and exact Maven version.
Historical Maven artifacts remain unchanged.

## Support

For distribution, ABI, licensing, or integration questions, contact
support@meshstream.net.

## Other MeshStream SDKs

- [Web](https://github.com/Mesh-Stream/meshstream-p2p-web-sdk): JavaScript / TypeScript, WebRTC and browser players.
- [iOS / macOS](https://github.com/Mesh-Stream/meshstream-p2p-ios-sdk): MeshStreamKit, Swift / Objective-C, SPM and CocoaPods.
- [Windows](https://github.com/Mesh-Stream/meshstream-p2p-Windows-sdk): C / C++ API for Windows 10 / 11 x64.
- [uni-app](https://github.com/Mesh-Stream/meshstream-p2p-uniapp-sdk): UTS / JavaScript plugin for iOS and Android apps.
- [Flutter](https://github.com/Mesh-Stream/meshstream-p2p-flutter-sdk): Dart plugin for iOS / Android with video_player examples.
- [React Native](https://github.com/Mesh-Stream/meshstream-p2p-react-native-sdk): TypeScript API for iOS / Android with react-native-video examples.
