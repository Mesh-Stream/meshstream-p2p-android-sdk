# MeshStream P2P Android SDK

[简体中文](README.zh-CN.md) · [Website](https://meshstream.net/) · [Documentation](https://meshstream.net/docs/sdk/android-setup/) · [Live demo](https://demo.meshstream.net/)

MeshStream Android SDK adds P2P video and audio delivery to Kotlin / Java apps using Media3, ExoPlayer or another URL-capable player. It supports HLS, MPEG-DASH and eligible HTTP Range MP4/MP3 requests through a local playback URL, with HTTP/CDN fallback when peers are unavailable.

## How it works

Create a session from your media URL, then give its `playbackUrl` to your existing player:

```text
App → SDK playback session → Local playback URL → Player
                              └─ SDK fetches from peers or origin/CDN
```

Your player handles decoding, rendering and playback controls. The SDK handles media delivery, caching and P2P scheduling. Configuration and P2P preparation run in the background; playback need not wait for a peer connection. Stop using the old playback URL before closing its session when leaving the page or switching content.

## Features

- HLS, MPEG-DASH and HTTP Range MP4/MP3 delivery with origin/CDN fallback.
- Kotlin / Java API with Media3 / ExoPlayer integration examples.
- Session preload, peer events, traffic counters and diagnostics.
- Maven distribution with armeabi-v7a, arm64-v8a, x86 and x86_64 native libraries.

## Before you start

- Create an application and a platform-specific SDK Key in the [MeshStream console](https://console.meshstream.net/). If application identity validation is enabled, register the package name or Bundle ID of the app you run.
- Have a playable HLS `.m3u8`, DASH `.mpd`, or HTTP Range-capable MP4 / MP3 URL. Prefer HTTPS.
- Choose a player that supports your source format. The SDK adds delivery capabilities, not codecs.

To try it first, jump to [Run the demo](#run-the-demo). The online demo linked above is a Web experience; run this repository's example for this platform.

## Release

The current release is **1.1.1**.

| Item | Value |
| --- | --- |
| Maven coordinate | `io.meshstream:meshstream-android-sdk:1.1.1` |
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

In the app module's `build.gradle.kts`, add the SDK and required player modules (HLS with Media3 in this example):

```kotlin
dependencies {
    implementation("io.meshstream:meshstream-android-sdk:1.1.1")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
}
```

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

`player` is an existing Media3 `ExoPlayer`. Retain the client and session in the playback owner.

```kotlin
import androidx.media3.common.MediaItem
import io.meshstream.sdk.P2pConfig
import io.meshstream.sdk.P2pSdk
import io.meshstream.sdk.P2pLogLevel

val originUrl = "https://cdn.example.com/video/master.m3u8"
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
player.release()
session.close()
P2pSdk.shutdown() // Application-wide shutdown when all clients are finished.
```

## Run the demo

```bash
git clone https://github.com/Mesh-Stream/meshstream-p2p-android-sdk.git
cd meshstream-p2p-android-sdk/Examples/Media3Demo
```

Open this directory in Android Studio. Replace `SDK_KEY` in [`MainActivity.kt`](Examples/Media3Demo/app/src/main/java/io/meshstream/customerminimal/MainActivity.kt) with your Android Key. If package validation is enabled, register the example's actual applicationId. Select an API 24+ device or emulator and click Run.

With JDK and Android SDK already configured, you can also install from a terminal:

```bash
./gradlew :app:installDebug
```

Launch the installed app, enter an HLS URL and click **Play**. The UI shows video, peers, P2P download/upload, HTTP download and offload ratio. This example configures HLS explicitly; DASH needs its matching Media3 module and media type.

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

## Release verification

The metadata for each public release is stored under [`releases/`](releases).
For 1.1.1, the published artifact SHA-256 is:

```text
401163248e90d580ad659d330016172c0871cfdcda7e7633f9b2c6f3d3394ca8
```

The public Maven endpoint for this release is `https://sdk.meshstream.net/maven`.
Release verification uses the supported public domain and exact Maven version.
Historical Maven artifacts remain unchanged.

## Verify P2P delivery

1. Play the same content in two devices or independently running instances, each with a valid Key. Use the same rendition and nearby playback positions. For rotating signed URLs, use the same stable content ID for the same content, and different IDs for different content.
2. Watch peers and traffic in the demo. A nonzero peer count means a connection exists; **increasing P2P download or upload bytes** confirms media sharing.
3. Close the other instance and check that HTTP/CDN playback continues. Also check seeking, switching content and cleanup when leaving playback.

HTTP/CDN delivery is normal with one viewer or no shareable segments. Discovery and sharing also depend on account configuration, network connectivity and the content cached by both viewers.

## More examples and support

See [advanced usage](https://meshstream.net/docs/sdk/android-advanced/) for preload, live/VOD scheduling, player feedback, multiple sessions and diagnostics. See [CHANGELOG](CHANGELOG.md) for updates. Contact support@meshstream.net with the SDK version, OS, player and redacted logs.

## Other MeshStream SDKs

- [Web](https://github.com/Mesh-Stream/meshstream-p2p-web-sdk): JavaScript / TypeScript, WebRTC and browser players.
- [iOS / macOS](https://github.com/Mesh-Stream/meshstream-p2p-ios-sdk): MeshStreamKit, Swift / Objective-C, SPM and CocoaPods.
- [Windows](https://github.com/Mesh-Stream/meshstream-p2p-Windows-sdk): C / C++ API for Windows 10 / 11 x64.
- [uni-app](https://github.com/Mesh-Stream/meshstream-p2p-uniapp-sdk): UTS / JavaScript plugin for iOS and Android apps.
- [Flutter](https://github.com/Mesh-Stream/meshstream-p2p-flutter-sdk): Dart plugin for iOS / Android with video_player examples.
- [React Native](https://github.com/Mesh-Stream/meshstream-p2p-react-native-sdk): TypeScript API for iOS / Android with react-native-video examples.
