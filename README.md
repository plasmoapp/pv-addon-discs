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

The addon can stream audio from various sources instead of saving audio files on the server. It even supports Youtube and Twitch live streams. Thanks to the brilliant [LavaPlayer](https://github.com/sedmelluq/lavaplayer) library.

## Installation

1. Install [Plasmo Voice](https://modrinth.com/mod/plasmo-voice), [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) and [pv-addon-lavaplayer-lib](https://modrinth.com/mod/pv-addon-lavaplayer-lib). You also need to install Plasmo Voice on your client.
2. Download this plugin from Modrinth and drop it into the `~/plugins` folder.
3. Restart the server

## Commands

`/disc burn <url> [name]` – Create a disc with custom audio from the URL. You need to hold a music disc in your hand.

`/disc erase` – Return a custom disc to a normal one.

`/disc search <query>` – Search tracks on YouTube.

## Permissions

Commands are only available to OP by default:

`pv.addon.discs.burn` – Burn command

`pv.addon.discs.erase` – Erase command

`pv.addon.discs.search` – Search command

`pv.addon.discs.burn.burnable_check_bypass` – If enabled in a config, you can only burn special discs. This permission allows you to bypass this check

Available to everyone by default:

`pv.addon.discs.play` – Use custom music discs

# Supported sources

Addon can load audio from:

- YouTube videos and live streams
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

## Planned features

- Copy discs like you can copy maps