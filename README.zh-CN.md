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

当前版本为 `1.0.9`，通过 MeshStream Maven分发。同一个 universal AAR 包含
`armeabi-v7a` / `x86`（API 17）和 `arm64-v8a` / `x86_64`（API 21）原生库，覆盖 ARM 真机及 ARM64、x86、x86_64 模拟器。
Media3 Demo 需要 API 24，旧系统需要匹配的播放器。

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

dependencies {
    implementation("io.meshstream:meshstream-android-sdk:1.0.9")
}
```

## 本地播放网络策略

SDK 返回设备本地 `127.0.0.1` 的 HTTP 代理地址；即使媒体源为 HTTPS，也需要允许播放器访问本地代理。API 24+ 示例已提供 `res/xml/network_security_config.xml`：

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

在 `<application>` 引用 `android:networkSecurityConfig="@xml/network_security_config"`；已有网络策略请合并本地地址规则。Android 6.0（API 23）不支持按域名放行，需根据应用策略设置 `android:usesCleartextTraffic="true"`；API 24+ 以网络安全配置为准。公网 HTTP 媒体源需要另行配置。

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
[`releases/`](releases)。正式 Maven 地址为
`https://sdk.meshstream.net/maven`；以精确版本及 SHA-256 校验发行包，历史对象不覆盖。
接入、ABI、授权或分发问题请联系 support@meshstream.net。
