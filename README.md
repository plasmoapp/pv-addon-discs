# pv-addon-discs
Paper only [Plasmo Voice](https://github.com/plasmoapp/plasmo-voice) addon. Play audio from Youtube and other sources in Minecraft using music discs.

The addon can stream audio from various sources instead of saving audio files on the server. It even supports Youtube and Twitch live streams. Thanks to the brilliant [LavaPlayer](https://github.com/sedmelluq/lavaplayer) library.

**Depends on [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)**

Inspired by [SVC](https://github.com/henkelmax/simple-voice-chat)'s [AudioPlayer](https://github.com/henkelmax/audio-player) & [Customdiscs](https://github.com/Navoei/CustomDiscs).

## Commands

`/disc burn <url> [name]` – Create a disc with custom audio from the URL. You need to hold a music disc in your hand.

`/disc erase` – Return a custom disc to a normal one.

## Permissions

Commands are only available to OP by default:

`pv.addon.discs.burn` – Burn command

`pv.addon.discs.erase` – Erase command

Available to everyone by default:

`pv.addon.discs.play` – Use custom music discs

## Supported sources

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
- YouTube search
- Set distance on a jukebox
- Custom goat horn audio
- Make it work with hoppers