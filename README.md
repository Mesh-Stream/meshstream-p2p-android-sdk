# MeshStream Android SDK

[简体中文](README.zh-CN.md)

## MeshStream SDKs

- [Online Demo](https://demo.meshstream.net/) for a live Web playback and P2P delivery view.
- [Web Demo](https://github.com/Mesh-Stream/meshstream-p2p-web-sdk) using the published npm SDK.
- [Android SDK](https://github.com/Mesh-Stream/meshstream-p2p-android-sdk) for Media3 / ExoPlayer apps.
- [Apple SDK](https://github.com/Mesh-Stream/meshstream-p2p-ios-sdk) for iOS and macOS apps.
- [Windows SDK](https://github.com/Mesh-Stream/meshstream-p2p-Windows-sdk) for C/C++ desktop apps.

MeshStream Android SDK provides a CDN-compatible playback URL for HLS and DASH
content. It adds P2P delivery opportunistically while preserving normal media
playback continuity. Applications continue to use their existing player; no
tracker, token, or service endpoint is supplied by application code.

## Release

The current release is **1.0.0**.

| Item | Value |
| --- | --- |
| Maven coordinate | `io.meshstream:meshstream-android-sdk:1.0.0` |
| Delivery | Maven repository hosted by MeshStream |
| Native ABI in this release | `arm64-v8a` |
| Minimum Android version | Android 5.0 (API 21) on the published ARM64 artifact |

The public artifact is intended for ARM64 physical devices. Customers needing
32-bit or Android emulator ABI packages should contact support before release
integration.

## Add the dependency

Add the MeshStream Maven repository in `settings.gradle.kts` (Kotlin DSL):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://pub-61830e1369904d19af505ff74fd55ba4.r2.dev/maven")
            content { includeGroup("io.meshstream") }
        }
    }
}
```

Then add the SDK and the required player modules:

```kotlin
dependencies {
    implementation("io.meshstream:meshstream-android-sdk:1.0.0")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
}
```

Do not download or copy the AAR into your application. The Maven coordinate is
the supported distribution path for this release.

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
Studio, and run it on an ARM64 device. It resolves the SDK from the public Maven
repository and contains no local SDK binary.

## Release verification

The metadata for each public release is stored under [`releases/`](releases).
For 1.0.0, the published artifact SHA-256 is:

```text
000621723f70a9a1e94167abdd1100d9f7b4f38373c04d62b82b0c7ae234ab98
```

## Support

For distribution, ABI, licensing, or integration questions, contact
support@meshstream.net.
