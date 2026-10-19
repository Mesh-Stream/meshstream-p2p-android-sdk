package io.meshstream.customerminimal

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import io.meshstream.sdk.P2pConfig
import io.meshstream.sdk.P2pClient
import io.meshstream.sdk.P2pEventListener
import io.meshstream.sdk.P2pLogLevel
import io.meshstream.sdk.P2pSdk
import io.meshstream.sdk.P2pStats
import io.meshstream.sdk.P2pUnavailableReason
import io.meshstream.sdk.PlaybackSession

class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var player: ExoPlayer
    private lateinit var urlInput: EditText
    private lateinit var status: TextView
    private lateinit var stats: TextView
    private var client: P2pClient? = null
    private var session: PlaybackSession? = null

    private val sdkListener = object : P2pEventListener {
        override fun onPeerCountChanged(sessionId: Long, peerCount: Int) {
            setStatus("Peers: $peerCount")
        }

        override fun onP2pUnavailable(sessionId: Long, reason: P2pUnavailableReason) {
            setStatus("P2P unavailable: $reason; CDN fallback continues")
        }

        override fun onStatistics(sessionId: Long, stats: P2pStats) {
            handler.post {
                this@MainActivity.stats.text =
                    "Peers ${stats.connectedPeers}\n" +
                        "P2P down ${format(stats.p2pDownloaded)} / up ${format(stats.p2pUploaded)}\n" +
                        "HTTP down ${format(stats.httpDownloaded)}\n" +
                        "Offload ${(stats.downloadOffloadRatio * 100).toInt()}%"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        setContentView(buildContent())
    }

    private fun buildContent(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)

            urlInput = EditText(this@MainActivity).apply {
                hint = "HLS URL"
                setText(DEFAULT_HLS_URL)
                isSingleLine = true
            }
            addView(urlInput)

            addView(Button(this@MainActivity).apply {
                text = "Play"
                setOnClickListener { createPlaybackSession(urlInput.text.toString().trim()) }
            })

            status = TextView(this@MainActivity).apply {
                text = "Idle"
                setPadding(0, 12, 0, 12)
            }
            addView(status)

            addView(PlayerView(this@MainActivity).apply {
                player = this@MainActivity.player
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

            stats = TextView(this@MainActivity).apply {
                text = "Peers 0\nP2P down 0 B / up 0 B\nHTTP down 0 B\nOffload 0%"
                setPadding(0, 12, 0, 0)
            }
            addView(stats)
        }
    }

    private fun createPlaybackSession(originUrl: String) {
        if (originUrl.isEmpty()) {
            setStatus("HLS URL is empty")
            return
        }

        session?.close()
        session = null
        P2pSdk.shutdown()
        setStatus("Creating SDK client")
        client = P2pSdk.create(
            applicationContext,
            P2pConfig(
                key = SDK_KEY,
                playbackUrl = originUrl,
                logLevel = P2pLogLevel.NONE,
            ),
        )
        runCatching {
            client!!.addEventListener(sdkListener)
            session = client!!.createPlaybackSession(originUrl)
            player.setMediaItem(
                MediaItem.Builder()
                    .setUri(session!!.playbackUrl)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build(),
            )
            player.prepare()
            player.play()
            setStatus("Playing through SDK playbackUrl")
        }.onFailure { setStatus("Start failed: ${it.message ?: it.javaClass.simpleName}") }
    }

    override fun onDestroy() {
        client = null
        session?.close()
        player.release()
        P2pSdk.shutdown()
        super.onDestroy()
    }

    private fun setStatus(message: String) {
        handler.post { status.text = message }
    }

    private fun format(bytes: Long): String = when {
        bytes < 1_024 -> "$bytes B"
        bytes < 1_048_576 -> "%.1f KB".format(bytes / 1_024.0)
        else -> "%.2f MB".format(bytes / 1_048_576.0)
    }

    private companion object {
        const val SDK_KEY = "replace_with_sdk_key"
        const val DEFAULT_HLS_URL = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    }
}
