# MeshStream P2P Android SDK

[English](README.md) · [官网](https://meshstream.net/zh/) · [接入文档](https://meshstream.net/zh/docs/sdk/android-setup/) · [在线 Demo](https://demo.meshstream.net/)

MeshStream Android SDK 为 Kotlin / Java 应用提供 P2P 视频与音频分发，可接入 Media3、ExoPlayer 或其他支持 URL 的播放器。通过本地播放地址接入 HLS、MPEG-DASH 和符合 HTTP Range 要求的 MP4/MP3 请求；Peer 不可用时回退到 HTTP/CDN。

## 工作方式

把原始媒体地址交给 SDK 创建会话，再把会话的 `playbackUrl` 交给现有播放器：

```text
应用 → SDK 创建播放会话 → 本地播放地址 → 播放器
                            └─ SDK 从 Peer 或源站/CDN 获取媒体
```

播放器继续负责解码、画面和播放控制；SDK 负责媒体传输、缓存及 P2P 调度。服务配置与 P2P 准备在后台执行，不需要等到 Peer 连接后才开始播放。离开播放页或切换内容时，先停止使用旧播放地址，再关闭旧会话。

## 产品能力

- 支持 HLS、MPEG-DASH、HTTP Range MP4/MP3 及源站/CDN 回退。
- 提供 Kotlin / Java API 与 Media3 / ExoPlayer 接入示例。
- 提供会话预加载、Peer 事件、流量统计与诊断接口。
- 通过 Maven 安装，原生库覆盖 armeabi-v7a、arm64-v8a、x86、x86_64。

## 接入准备

- 在 [MeshStream 控制台](https://console.meshstream.net/)创建应用和对应平台的 SDK Key；启用身份校验时，登记与运行应用一致的包名或 Bundle ID。
- 准备一个可播放的 HLS `.m3u8`、DASH `.mpd`、支持 Range 的 MP4 或 MP3 地址，建议使用 HTTPS。
- 选择支持该媒体格式的播放器。SDK 提供传输能力，不增加播放器的解码能力。

希望先看效果？直接跳到[运行 Demo](#运行-demo)，再按下方步骤接入自己的应用。顶部的在线 Demo 是 Web 体验页，各端示例在本仓库中运行。

## 当前发布

当前版本为 `1.0.10`，通过 MeshStream Maven 分发。同一个 universal AAR 包含
`armeabi-v7a` / `x86`（API 17）和 `arm64-v8a` / `x86_64`（API 21）原生库，覆盖 ARM 真机及 ARM64、x86、x86_64 模拟器。
Media3 Demo 需要 API 24，旧系统需要匹配的播放器。

## 安装

在项目的 `settings.gradle.kts` 中合并 Maven 仓库配置，保留现有仓库：

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

在应用模块的 `build.gradle.kts` 中添加 SDK 和所需播放器模块，下面以 Media3 播放 HLS 为例：

```kotlin
dependencies {
    implementation("io.meshstream:meshstream-android-sdk:1.0.10")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
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

`player` 为页面持有的 Media3 `ExoPlayer`，客户端和会话也应由页面或播放管理器持有。

```kotlin
import androidx.media3.common.MediaItem
import io.meshstream.sdk.P2pConfig
import io.meshstream.sdk.P2pSdk

val originUrl = "https://cdn.example.com/video/master.m3u8"
val client = P2pSdk.create(applicationContext, P2pConfig(key = "your_sdk_key"))
val session = client.createPlaybackSession(originUrl)
player.setMediaItem(MediaItem.fromUri(session.playbackUrl))
player.prepare()
player.play()
```

服务配置与 P2P 准备在后台执行。加速暂不可用时，返回的播放地址仍会通过源站/CDN
继续请求媒体，不应中断播放器。播放结束后关闭 `session`；所有客户端结束后再调用
`P2pSdk.shutdown()`。

离开播放页时，先停止并释放播放器，再关闭会话：

```kotlin
player.release()
session.close()
// 仅在应用不再使用任何 SDK 客户端时：
P2pSdk.shutdown()
```

## 运行 Demo

```bash
git clone https://github.com/Mesh-Stream/meshstream-p2p-android-sdk.git
cd meshstream-p2p-android-sdk/Examples/Media3Demo
```

用 Android Studio 打开当前目录，将 [`MainActivity.kt`](Examples/Media3Demo/app/src/main/java/io/meshstream/customerminimal/MainActivity.kt) 的 `SDK_KEY` 换成自己的 Android Key；启用包名校验时，登记示例的实际 applicationId。选择 API 24+ 真机或模拟器，点击 Run。

也可使用已配置好 JDK 和 Android SDK 的终端安装：

```bash
./gradlew :app:installDebug
```

打开安装的应用，输入 HLS 地址并点击 **Play**。页面展示画面、Peer 数、P2P 下载/上传、HTTP 下载和卸载比例。此示例固定按 HLS 配置播放器；DASH 需添加对应 Media3 模块和类型。

## 格式与统计

HLS、DASH 使用相同会话接口；DASH 播放需添加 `androidx.media3:media3-exoplayer-dash`。无后缀地址可在 `P2pConfig` 中设置 `mimeType = "m3u8"` 或 `"mpd"`。MP4/MP3 加速要求源站支持 Range 和有效的 `Content-Range` 响应。

使用 `client.addEventListener` 注册 `P2pEventListener`，通过 `onStatistics(sessionId, stats)` 获取会话流量和 Peer 统计。加速状态回调用于展示和诊断，不应阻塞播放器起播。发行版本和校验和见 [`releases/`](releases/)。

## 确认 P2P 生效

1. 在两台设备或两个可同时运行的实例中，用各自有效的 Key 播放同一内容，保持相同清晰度和相近播放进度。签名 URL 会变化时，为同一内容使用相同、稳定的内容 ID；不同内容不要共用 ID。
2. 查看 Demo 的 Peer 数和流量统计。Peer 大于 0 表示已连接节点；**P2P 下载或上传字节持续增加**才说明发生了媒体共享。
3. 关闭另一个实例，确认剩余实例仍能通过 HTTP/CDN 播放；然后测试拖动、切换内容和退出页面的释放流程。

只有一个观看者或没有可共享分片时，主要走 HTTP/CDN 属于正常情况。发现 Peer 及实际共享还取决于账户配置、网络连通性和双方缓存内容。

## 更多用法与支持

预加载、直播/点播调度、播放器状态回传、多会话及诊断，请参阅[高级用法](https://meshstream.net/zh/docs/sdk/android-advanced/)。更新记录见 [CHANGELOG](CHANGELOG.md)。接入问题请联系 support@meshstream.net，并附 SDK 版本、系统、播放器和脱敏日志。

## 其他 MeshStream SDK

- [Web](https://github.com/Mesh-Stream/meshstream-p2p-web-sdk): JavaScript / TypeScript、WebRTC 与浏览器播放器.
- [iOS / macOS](https://github.com/Mesh-Stream/meshstream-p2p-ios-sdk): MeshStreamKit、Swift / Objective-C、SPM 与 CocoaPods.
- [Windows](https://github.com/Mesh-Stream/meshstream-p2p-Windows-sdk): 面向 Windows 10 / 11 x64 的 C / C++ 接口.
- [uni-app](https://github.com/Mesh-Stream/meshstream-p2p-uniapp-sdk): 面向 iOS / Android App 的 UTS / JavaScript 插件.
- [Flutter](https://github.com/Mesh-Stream/meshstream-p2p-flutter-sdk): 面向 iOS / Android 的 Dart 插件及 video_player 示例.
- [React Native](https://github.com/Mesh-Stream/meshstream-p2p-react-native-sdk): 面向 iOS / Android 的 TypeScript API 及 react-native-video 示例.
