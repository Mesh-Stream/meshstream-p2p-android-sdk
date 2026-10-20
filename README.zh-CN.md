# MeshStream Android SDK

[English](README.md)

MeshStream Android SDK 为 HLS 和 DASH 提供与 CDN 兼容的播放地址，并在可用时启用
P2P 分发。应用继续使用自己的播放器；SDK 只负责播放会话、服务配置和加速处理。

## MeshStream SDKs

- [在线 Demo](https://demo.meshstream.net/)：查看已部署的 Web 播放和 P2P 分发效果。
- [Web Demo](https://github.com/Mesh-Stream/meshstream-p2p-web-sdk)：使用 npm 发布的 Web SDK。
- [Android SDK](https://github.com/Mesh-Stream/meshstream-p2p-android-sdk)：适用于 Media3 / ExoPlayer。
- [Apple SDK](https://github.com/Mesh-Stream/meshstream-p2p-ios-sdk)：适用于 iOS 和 macOS。
- [Windows SDK](https://github.com/Mesh-Stream/meshstream-p2p-Windows-sdk)：适用于 C/C++ 桌面应用。

## 当前发布

当前版本为 `1.0.2`，通过 MeshStream Maven 仓库分发。公开包包含 `arm64-v8a`
原生库，面向 Android 5.0（API 21）及以上 ARM64 真机。32 位或模拟器 ABI 请联系
技术支持获取适配构建包。

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

dependencies {
    implementation("io.meshstream:meshstream-android-sdk:1.0.2")
}
```

## 快速接入

使用 SDK Key 创建客户端，为每个媒体条目创建独立会话，并将会话地址交给播放器：

```kotlin
val client = P2pSdk.create(applicationContext, P2pConfig(key = "your_sdk_key"))
val session = client.createPlaybackSession(originUrl)
player.setMediaItem(MediaItem.fromUri(session.playbackUrl))
player.prepare()
player.play()
```

服务配置与 P2P 准备在后台执行。加速暂不可用时，返回的播放地址仍会通过源站/CDN
继续请求媒体，不应中断播放器。播放结束后关闭 `session`；所有客户端结束后再调用
`P2pSdk.shutdown()`。

## 示例与支持

[`Examples/Media3Demo`](Examples/Media3Demo) 是可独立打开的 Android Studio 工程，
通过 Maven 解析 SDK，不包含本地 AAR。SDK 版本元数据和校验和位于
[`releases/`](releases)。接入、ABI、授权或分发问题请联系 support@meshstream.net。
