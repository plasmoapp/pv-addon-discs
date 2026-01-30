package su.plo.voice.discs.config

import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.Android
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.AndroidMusic
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.AndroidVr
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.Ios
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.MWeb
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.Music
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.Tv
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.Web
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.WebEmbedded
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.skeleton.Client
import java.util.function.Supplier

enum class YoutubeClient(
    val client: Supplier<Client>
) {
    MUSIC({ Music() }),
    WEB({ Web() }),
    MWEB({ MWeb() }),
    WEBEMBEDDED({ WebEmbedded() }),
    ANDROID({ Android() }),
    ANDROID_VR({ AndroidVr() }),
    ANDROID_MUSIC({ AndroidMusic() }),
    IOS({ Ios() }),
    TV({ Tv() }),
}
