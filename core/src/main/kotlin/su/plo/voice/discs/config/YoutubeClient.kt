package su.plo.voice.discs.config

import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.Android
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.AndroidMusic
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.AndroidTestsuite
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.Ios
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.MediaConnect
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.Music
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.TvHtml5Embedded
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.Web
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.WebEmbedded
import su.plo.voice.lavaplayer.libs.dev.lavalink.youtube.clients.skeleton.Client

enum class YoutubeClient(
    val client: Client
) {
    MUSIC(Music()),
    WEB(Web()),
    WEBEMBEDDED(WebEmbedded()),
    ANDROID(Android()),
    ANDROID_TESTSUITE(AndroidTestsuite()),
    ANDROID_MUSIC(AndroidMusic()),
    MEDIA_CONNECT(MediaConnect()),
    IOS(Ios()),
    TVHTML5EMBEDDED(TvHtml5Embedded())
}