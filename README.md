<img src="https://i.imgur.com/4o67Wn1.png" width="256"/>

<div>
    <a href="https://modrinth.com/mod/plasmo-voice">Plasmo Voice</a>
    <span> | </span>
    <a href="https://modrinth.com/plugin/pv-addon-discs">Modrinth</a>
    <span> | </span>
    <a href="https://github.com/plasmoapp/pv-addon-discs/">GitHub</a>
    <span> | </span>
    <a href="https://discord.com/invite/uueEqzwCJJ">Discord</a>
     <span> | </span>
    <a href="https://www.patreon.com/plasmomc">Patreon</a>
</div>

# pv-addon-discs

Paper only [Plasmo Voice](https://github.com/plasmoapp/plasmo-voice) addon. Play audio from Youtube and other sources in Minecraft using music discs. Inspired by [SVC](https://github.com/henkelmax/simple-voice-chat)'s [AudioPlayer](https://github.com/henkelmax/audio-player) & [CustomDiscs](https://github.com/Navoei/CustomDiscs).

The addon can stream audio from various sources instead of saving audio files on the server. It even supports Youtube and Twitch live streams. Thanks to the brilliant [LavaPlayer](https://github.com/lavalink-devs/lavaplayer) library.

## Installation

1. Install [Plasmo Voice](https://modrinth.com/mod/plasmo-voice), [PacketEvents](https://modrinth.com/plugin/packetevents) and [pv-addon-lavaplayer-lib](https://modrinth.com/mod/pv-addon-lavaplayer-lib). You also need to install Plasmo Voice on your client.
2. Download this plugin from Modrinth and drop it into the `~/plugins` folder.
3. Restart the server

## Commands

`/disc burn <url> [name]` – Create a disc with custom audio from the URL. You need to hold a music disc in your hand.

`/disc erase` – Return a custom disc to a normal one.

`/disc search <query>` – Search tracks on YouTube.

`/disc cancel [player]` – Cancel user's goat horn playback. You need `pv.addon.discs.cancel.other` permission to cancel for others.

## Permissions

Commands are only available to OP by default:

`pv.addon.discs.burn` – Burn command

`pv.addon.discs.erase` – Erase command

`pv.addon.discs.search` – Search command

`pv.addon.discs.cancel` – Command to cancel goat horn playback

`pv.addon.discs.cancel.other` – Command to cancel goat horn playback for the other player

`pv.addon.discs.burn.burnable_check_bypass` – If enabled in a config, you can only burn special discs. This permission allows you to bypass this check

Available to everyone by default:

`pv.addon.discs.play` – Use custom music discs

# Supported sources

Addon can load audio from:

- YouTube videos and live streams (could require additional setup, see [Common issues](#common-issues))
- SoundCloud
- Bandcamp
- Vimeo
- Twitch live streams
- Direct HTTP URLs

## Supported file formats

If you want to stream audio from direct URLs

- MP3
- FLAC
- WAV
- Matroska/WebM (AAC, Opus or Vorbis codecs)
- MP4/M4A (AAC codec)
- OGG streams (Opus, Vorbis and FLAC codecs)
- AAC streams
- Stream playlists (M3U and PLS)

# API
![Version](https://img.shields.io/badge/dynamic/xml?color=186EF0&label=release&query=/metadata/versioning/versions/version[not(contains(text(),'%2B'))][last()]&url=https://repo.plasmoverse.com/releases/su/plo/voice/addon/discs-paper/maven-metadata.xml)
![Version](https://img.shields.io/badge/dynamic/xml?color=186EF0&label=snapshot&query=/metadata/versioning/versions/version[not(contains(text(),'%2B'))][last()]&url=https://repo.plasmoverse.com/snapshots/su/plo/voice/addon/discs-paper/maven-metadata.xml)

Bukkit events list: https://github.com/plasmoapp/pv-addon-discs/tree/v1/core/src/main/kotlin/su/plo/voice/discs/api.

### Kotlin DSL
```kotlin
repositories {
    maven("https://repo.plasmoverse.com/releases")
}

dependencies {
    compileOnly("su.plo.voice.addon:discs-paper:$VERSION")
}
```

# Common issues

## Problematic YouTube player script
1) Make sure you're using the latest versions of `pv-addon-discs` and `pv-addon-lavaplayer-lib`.
2) Open `plugins/pv-addon-discs/discs.toml`, uncomment and configure `youtube_source.remote_cipher`:
    ```toml
    [youtube_source.remote_cipher]
    url = "https://cipher.kikkia.dev/"
    password = ""
    ```
   *This uses a public instance of [yt-cipher](https://github.com/kikkia/yt-cipher). To host your own, follow their tutorial.*
3) Restart the server and see if it works.
4) If this doesn't work, [try oauth2](#sign-in-to-confirm-youre-not-a-bot--please-sign-in-using-youtube).

## "Sign in to confirm you're not a bot" / "Please sign in" using YouTube
1) Make sure you're using the latest versions of `pv-addon-discs` and `pv-addon-lavaplayer-lib`.
2) Set `youtube_source.use_oauth2` to `true` in `plugins/pv-addon-discs/discs.toml`.
3) Restart the server (`/vreload` will not work).
4) When `pv-addon-discs` initializes, you'll see a message in your console like this (code will be different): https://i.imgur.com/r1o5v8v.png. Follow the instructions in the console message.
5) After successful authorization, you'll see a message in your console: https://i.imgur.com/BD9jCRe.png. You don't need to save token manually, it will be saved when the server is stopped.
6) Done! YouTube tracks should now work as expected.

## Read timed out
This usually means that your server can't reach the server hosting the track.
Check your server's internet connection and contact your hosting provider if the issue persists.

If you have HTTP proxy, you can set it using `http_proxy` config option in `plugins/pv-addon-discs/discs.toml`.
This feature is only available starting from 1.1.x.
